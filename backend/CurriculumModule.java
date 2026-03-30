package ui.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CurriculumModule is responsible for handling all student queries related to
 * program requirements, course details, and graduation tracking. It parses unstructured
 * text input using regular expressions and keyword matching to dynamically generate
 * formatted textual responses based on the static DataStore.
 */
public class CurriculumModule {

    /** * Regex pattern to identify standard university course codes (e.g., "CS 253", "MATH1720").
     * Breakdown: \b (word boundary), 2-4 letters, optional space, 3-4 digits, \b (word boundary).
     */
    private static final Pattern CODE_PATTERN =
            Pattern.compile("\\b([A-Za-z]{2,4}\\s?\\d{3,4})\\b");

    /**
     * Main entry point for curriculum-related queries.
     * Routes the user's raw input string to the appropriate data aggregation
     * method by matching specific keywords and patterns.
     *
     * @param input The raw text input submitted by the user.
     * @return A formatted multi-line string containing the requested curriculum data.
     */
    public String handle(String input) {
        // Guard clause: Return the default help menu if the input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return helpMessage();
        }

        // Extract course code from input (before lowercasing) to maintain strict formatting
        String detectedCode = extractCode(input);
        // Normalize the query to lowercase to ensure case-insensitive keyword matching
        String q = input.toLowerCase().trim();

        // LOGIC BRANCH 1: Specific Course Query
        // Full course detail when a specific code (e.g., "CS253") is successfully extracted
        if (detectedCode != null) {
            DataStore.Course c = DataStore.findCourse(detectedCode);
            if (c != null) {
                // Determine if the user wants specific prerequisite info or general details
                if (has(q, "prereq", "prerequisite", "require", "need for")) {
                    return prerequisiteDetail(c);
                }
                return courseDetail(c);
            } else {
                return "Course \"" + detectedCode.toUpperCase() + "\" was not found in the catalogue.\n\n"
                        + "Type \"show all courses\" for the full course list.";
            }
        }

        // LOGIC BRANCH 2: Year-Specific Listings
        // Matches queries asking for required courses in a specific academic year
        if (has(q, "year 1", "first year", "1st year")) return yearListing(1);
        if (has(q, "year 2", "second year", "2nd year")) return yearListing(2);
        if (has(q, "year 3", "third year", "3rd year"))  return yearListing(3);
        if (has(q, "year 4", "fourth year", "final year","4th year")) return yearListing(4);

        // LOGIC BRANCH 3: Full Program Overview
        // Matches broad queries requesting the entire 4-year degree plan
        if (has(q, "all courses", "full list", "full program", "program requirements",
                "show all", "entire program", "complete program")) {
            return fullProgramListing();
        }

        // LOGIC BRANCH 4: Graduation & Credit Requirements
        // Credits / graduation check
        if (has(q, "how many credit", "total credit", "graduation credit", "credits to graduate",
                "credits needed", "credit requirement")) {
            return creditSummary();
        }
        if (has(q, "graduat", "finish", "complete my degree", "degree requirement")) {
            return graduationRequirements();
        }

        // LOGIC BRANCH 5: Electives
        // Matches queries asking for free or technical elective options
        if (has(q, "elective")) {
            return electivesInfo();
        }

        // Default Fallback: If no keywords match, return the instruction menu
        return helpMessage();
    }

    // -------------------------------------------------------------------------
    // Response builders
    // -------------------------------------------------------------------------

    /**
     * Constructs a detailed view of a single course, including credits, description,
     * prerequisites, and currently active sections for the term.
     */
    private String courseDetail(DataStore.Course c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Course Details\n");
        sb.append("==============\n");
        sb.append(String.format("Code:         %s%n", c.code));
        sb.append(String.format("Name:         %s%n", c.name));
        sb.append(String.format("Credits:      %d%n", c.credits));
        sb.append(String.format("Year:         %d  (%s)%n", c.year, c.term));
        sb.append("\n");

        // Format and append prerequisite list
        if (c.prerequisites.length == 0) {
            sb.append("Prerequisites: None\n");
        } else {
            sb.append("Prerequisites:\n");
            for (String pre : c.prerequisites) {
                DataStore.Course pc = DataStore.findCourse(pre);
                String pname = (pc != null) ? pc.name : "N/A";
                sb.append(String.format("  -> %-10s %s%n", pre, pname));
            }
        }

        sb.append("\nDescription:\n  ").append(c.description).append("\n");

        // Calculate and append reverse-prerequisites (courses that list this as a prerequisite)
        List<String> leadsTo = leadsTo(c.code);
        if (!leadsTo.isEmpty()) {
            sb.append("\nLeads to:\n");
            for (String next : leadsTo) {
                DataStore.Course nc = DataStore.findCourse(next);
                if (nc != null) sb.append(String.format("  -> %-10s %s%n", nc.code, nc.name));
            }
        }

        // Search the active schedule to append live section availability
        for (DataStore.Section sec : DataStore.sections) {
            if (sec.courseCode.equalsIgnoreCase(c.code)) {
                sb.append(String.format("%nCurrent Section (%s):%n", sec.sectionId));
                sb.append(String.format("  Instructor: %s%n", sec.instructor));
                sb.append(String.format("  Days/Time:  %s  %s - %s%n", sec.days, sec.startTime, sec.endTime));
                sb.append(String.format("  Room:       %s%n", sec.room));
                sb.append(String.format("  Enrollment: %d / %d  (%s)%n",
                        sec.enrolled, sec.capacity,
                        sec.isFull() ? "FULL" : sec.seatsAvailable() + " seats available"));
            }
        }

        return sb.toString();
    }

    /**
     * Constructs a specialized response detailing only the prerequisite chain
     * for a specific requested course.
     */
    private String prerequisiteDetail(DataStore.Course c) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Prerequisites for %s — %s%n", c.code, c.name));
        sb.append("=".repeat(50)).append("\n\n");

        if (c.prerequisites.length == 0) {
            sb.append("This course has no prerequisites. Anyone may enroll.\n");
        } else {
            sb.append("Direct prerequisites:\n");
            for (String pre : c.prerequisites) {
                DataStore.Course pc = DataStore.findCourse(pre);
                if (pc != null) {
                    // Show nested prerequisites if they exist to help student planning
                    sb.append(String.format("  -> %-10s %s (Year %d, %s)%n",
                            pc.code, pc.name, pc.year, pc.term));
                    if (pc.prerequisites.length > 0) {
                        sb.append("       (which requires: ");
                        sb.append(String.join(", ", pc.prerequisites));
                        sb.append(")\n");
                    }
                } else {
                    sb.append(String.format("  -> %s%n", pre));
                }
            }
        }

        sb.append(String.format("%nTip: %s is a %d-credit course offered in %s, Year %d.%n",
                c.code, c.credits, c.term, c.year));

        // Display courses unlocked by taking this class
        List<String> leadsTo = leadsTo(c.code);
        if (!leadsTo.isEmpty()) {
            sb.append(String.format("%s is a prerequisite for: %s%n",
                    c.code, String.join(", ", leadsTo)));
        }

        return sb.toString();
    }

    /**
     * Aggregates and returns a formatted list of all mandatory courses for a given academic year.
     */
    private String yearListing(int year) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("YEAR %d — Required Courses (Computer Science, UWindsor)%n", year));
        sb.append("=".repeat(54)).append("\n");

        String[] terms = {"Fall", "Winter"};
        int yearTotal = 0;

        // Iterate through both semesters to organize the output chronologically
        for (String term : terms) {
            sb.append(String.format("%n%s SEMESTER%n", term.toUpperCase()));
            int termCredits = 0;
            for (DataStore.Course c : DataStore.courses) {
                if (c.year == year && c.term.equals(term)) {
                    sb.append(String.format("  %-10s %-48s %d cr%n", c.code, c.name, c.credits));
                    termCredits += c.credits;
                }
            }
            sb.append(String.format("             Subtotal: %d credits%n", termCredits));
            yearTotal += termCredits;
        }

        sb.append(String.format("%nYear %d Total:   %d credits%n", year, yearTotal));

        // Calculate cumulative credits earned by the end of this year
        int cumulative = 0;
        for (int y = 1; y <= year; y++) {
            for (DataStore.Course c : DataStore.courses) {
                if (c.year == y) cumulative += c.credits;
            }
        }
        sb.append(String.format("Cumulative after Year %d: ~%d credits%n", year, cumulative));

        return sb.toString();
    }

    /**
     * Generates a high-level overview of the entire 4-year degree timeline.
     */
    private String fullProgramListing() {
        StringBuilder sb = new StringBuilder();
        sb.append("FULL PROGRAM — Computer Science, University of Windsor\n");
        sb.append("=".repeat(54)).append("\n");

        for (int year = 1; year <= 4; year++) {
            sb.append(String.format("%n--- YEAR %d ---%n", year));
            for (String term : new String[]{"Fall", "Winter"}) {
                boolean printed = false;
                for (DataStore.Course c : DataStore.courses) {
                    if (c.year == year && c.term.equals(term)) {
                        if (!printed) {
                            sb.append(String.format("  %s:%n", term));
                            printed = true; // Ensures the term header is only printed once
                        }
                        sb.append(String.format("    %-10s %-44s %d cr%n",
                                c.code, c.name, c.credits));
                    }
                }
            }
        }

        // Sum total program credits to assist with degree audits
        int total = 0;
        for (DataStore.Course c : DataStore.courses) total += c.credits;
        sb.append(String.format("%n%sTOTAL PROGRAM CREDITS: ~%d cr (plus electives to reach 120)%n",
                "-".repeat(54) + "\n", total));

        return sb.toString();
    }

    /**
     * Summarizes the math breakdown of required core credits vs. free elective credits.
     */
    private String creditSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("UWindsor CS Program — Credit Requirements\n");
        sb.append("=".repeat(42)).append("\n");

        int grand = 0;
        for (int y = 1; y <= 4; y++) {
            int yc = 0;
            for (DataStore.Course c : DataStore.courses) if (c.year == y) yc += c.credits;
            sb.append(String.format("Year %d:  %d credits%n", y, yc));
            grand += yc;
        }
        sb.append("-".repeat(42)).append("\n");
        sb.append(String.format("Required Core Credits:   ~%d credits%n", grand));
        sb.append(String.format("Free Elective Credits:   ~%d credits%n", 120 - grand));
        sb.append("-".repeat(42)).append("\n");
        sb.append("TOTAL TO GRADUATE:       120 credits\n\n");
        sb.append("Tip: Speak with your academic advisor to review\n");
        sb.append("your transcript and confirm remaining credits.\n");
        return sb.toString();
    }

    /**
     * Returns a static checklist of graduation requirements specific to the CS program.
     */
    private String graduationRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graduation Requirements — BSc Computer Science\n");
        sb.append("University of Windsor, School of Computer Science\n");
        sb.append("=".repeat(50)).append("\n\n");
        sb.append("1. Complete all required core courses (Years 1-4)\n");
        sb.append("2. Minimum 120 total credit hours\n");
        sb.append("3. Minimum cumulative GPA of 2.0\n");
        sb.append("4. Complete COMP400 (Undergraduate Thesis) OR\n");
        sb.append("   approved alternative capstone project\n");
        sb.append("5. Attend CS499 (Computer Science Seminar) in Year 4\n");
        sb.append("6. Satisfy free elective requirements (~26 credits)\n\n");
        sb.append("Current Term: Winter 2026\n");
        sb.append("Deadline to apply for graduation: March 1, 2026\n\n");
        sb.append("For a personalized graduation audit, book an\n");
        sb.append("appointment with Dr. Jessica Chen (Academic Advisor).\n");
        return sb.toString();
    }

    /**
     * Returns general advice and popular selections for fulfilling elective credits.
     */
    private String electivesInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Elective Options — BSc Computer Science\n");
        sb.append("=".repeat(40)).append("\n\n");
        sb.append("You need approximately 26 elective credits.\n");
        sb.append("Electives can be chosen from:\n\n");
        sb.append("  - Any CS400+ course not already taken\n");
        sb.append("  - CS498  Selected Topics in Computer Science  3 cr\n");
        sb.append("  - MATH courses (beyond required calculus)\n");
        sb.append("  - Cross-listed courses approved by your advisor\n");
        sb.append("  - Up to 6 credits from other faculties (with advisor approval)\n\n");
        sb.append("Popular elective choices:\n");
        sb.append("  CS455  Machine Learning          3 cr  (Year 4, Fall)\n");
        sb.append("  CS466  Deep Learning              3 cr  (Year 4, Winter)\n");
        sb.append("  CS474  Cloud Computing            3 cr  (Year 4, Fall)\n");
        sb.append("  CS495  Cybersecurity              3 cr  (Year 4, Winter)\n");
        sb.append("  CS485  Advanced Algorithms        3 cr  (Year 4, Fall)\n\n");
        sb.append("Type a course code (e.g. \"CS455\") for full details.\n");
        return sb.toString();
    }

    /**
     * Default fallback response providing the user with valid command examples.
     */
    private String helpMessage() {
        return "Curriculum Advising — myAdvice\n"
                + "================================\n\n"
                + "Try one of these queries:\n\n"
                + "  \"year 1 courses\"            — Year 1 required courses\n"
                + "  \"year 2 courses\"            — Year 2 required courses\n"
                + "  \"year 3 courses\"            — Year 3 required courses\n"
                + "  \"year 4 courses\"            — Year 4 required courses\n"
                + "  \"full program\"              — All 4 years at a glance\n"
                + "  \"CS253\"                     — Detail card for a course\n"
                + "  \"prerequisites for CS460\"  — Prereq chain for a course\n"
                + "  \"how many credits to graduate\" — Credit requirements\n"
                + "  \"graduation requirements\"  — Graduation checklist\n"
                + "  \"electives\"                — Available elective options\n";
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * Uses the defined CODE_PATTERN regex to safely extract a potential course code
     * from the user's raw input. It normalizes the output by removing whitespace
     * and converting to uppercase so it can accurately match the DataStore.
     */
    private String extractCode(String input) {
        Matcher m = CODE_PATTERN.matcher(input);
        if (m.find()) {
            return m.group(1).toUpperCase().replaceAll("\\s", "");
        }
        return null;
    }

    /**
     * A utility method to check if the user's query contains ANY of the provided keyword strings.
     * Simplifies the logic branching in the main handle() method.
     */
    private boolean has(String q, String... terms) {
        for (String t : terms) if (q.contains(t)) return true;
        return false;
    }

    /**
     * Performs a reverse-lookup in the DataStore to find all advanced courses
     * that require the target course code as a prerequisite.
     * Useful for showing students what doors a specific class opens.
     */
    private List<String> leadsTo(String code) {
        List<String> result = new ArrayList<>();
        for (DataStore.Course c : DataStore.courses) {
            for (String pre : c.prerequisites) {
                if (pre.equalsIgnoreCase(code)) {
                    result.add(c.code);
                    break;
                }
            }
        }
        return result;
    }
}