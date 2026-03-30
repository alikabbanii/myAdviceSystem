package ui.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The AdminModule simulates a privileged administrative console.
 * It handles system-level requests such as checking global system health,
 * auditing student rosters, tracking enrollment fill rates, and simulating
 * the modification of underlying course and prerequisite data.
 */
public class AdminModule {

    /**
     * Regex pattern to identify standard university course codes (e.g., "CS466" or "MATH 1000").
     * Breakdown: \b (word boundary), 2-4 letters, optional space, 3-4 digits, \b (word boundary).
     */
    private static final Pattern CODE_PATTERN =
            Pattern.compile("\\b([A-Za-z]{2,4}\\s?\\d{3,4})\\b");

    /**
     * Main entry point for administrative commands.
     * Parses the raw input string and routes it to the corresponding
     * reporting or simulated data mutation method.
     *
     * @param input The raw text command submitted by the admin user.
     * @return A formatted multi-line string containing the system report or action confirmation.
     */
    public String handle(String input) {
        // Guard clause: Return the default help menu if the input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return helpMessage();
        }

        // Normalize the query to lowercase for case-insensitive keyword matching
        String q = input.toLowerCase().trim();

        // QUERY BRANCH: Global system health and active data metrics
        if (has(q, "system status", "status", "health", "system info", "version")) {
            return systemStatus();
        }

        // QUERY BRANCH: Print the entire course catalogue
        if (has(q, "show all course", "list course", "all course", "course list", "course catalogue")) {
            return listAllCourses();
        }

        // QUERY BRANCH: Print the global student roster
        if (has(q, "list student", "all student", "show student", "student list", "student roster")) {
            return listAllStudents();
        }

        // SIMULATED ACTION BRANCH: Prerequisite Management
        if (has(q, "add prereq", "add prerequisite")) {
            // Attempts to extract exactly two course codes to link them together
            String[] codes = extractTwoCodes(input);
            if (codes != null) return addPrerequisite(codes[0], codes[1]);
            return addPrerequisite(null, null); // Falls back to instruction prompt if missing codes
        }
        if (has(q, "remove prereq", "remove prerequisite", "delete prereq")) {
            String[] codes = extractTwoCodes(input);
            if (codes != null) return removePrerequisite(codes[0], codes[1]);
            return removePrerequisite(null, null);
        }

        // SIMULATED ACTION BRANCH: Section Management
        if (has(q, "update section", "edit section", "modify section")) {
            return updateSection(input);
        }
        if (has(q, "add section", "create section", "new section")) {
            return addSection(input);
        }

        // QUERY BRANCH: Global enrollment fill-rate statistics and capacity warnings
        if (has(q, "enrollment stat", "enrollment number", "enrollment report", "show enrollment")) {
            return enrollmentStats();
        }

        // SIMULATED ACTION BRANCH: Register a new student into the system
        if (has(q, "add student", "register student", "new student")) {
            return addStudent(input);
        }

        // SIMULATED ACTION BRANCH: System wipe / data reset warning
        if (has(q, "reset", "clear data", "wipe")) {
            return resetWarning();
        }

        // Default Fallback: Return the instruction menu if no keywords match
        return helpMessage();
    }

    // -------------------------------------------------------------------------
    // Response builders
    // -------------------------------------------------------------------------

    /**
     * Aggregates global system metrics including loaded modules, active data counts,
     * and booking capacities to simulate a server health dashboard.
     */
    private String systemStatus() {
        int available = 0, booked = 0;

        // Tally up total booked vs available slots across the entire system
        for (DataStore.AppointmentSlot s : DataStore.slots) {
            if (s.isBooked) booked++; else available++;
        }

        return "myAdvice System Status — Winter 2026\n"
                + "=".repeat(40) + "\n"
                + "Backend Version:   1.0.0\n"
                + "Institution:       University of Windsor — SCS\n"
                + "Current Term:      Winter 2026 (Jan 6 – Apr 14, 2026)\n"
                + "Data Mode:         In-Memory (demo)\n\n"
                + "MODULE STATUS:\n"
                + "  [OK] Curriculum Module    — " + DataStore.courses.length + " courses loaded\n"
                + "  [OK] Scheduling Module    — " + DataStore.sections.length + " sections loaded\n"
                + "  [OK] Booking Module       — " + DataStore.slots.length + " slots ("
                + available + " available, " + booked + " booked)\n"
                + "  [OK] Administration Module\n"
                + "  [OK] Reports Module\n\n"
                + "STATISTICS:\n"
                + "  Registered Students:  " + DataStore.students.length + "\n"
                + "  Active Sections:      " + DataStore.sections.length + "\n"
                + "  Total Advisors:       " + DataStore.advisors.length + "\n"
                + "  Open Booking Slots:   " + available + "\n\n"
                + "System: Running normally. No errors detected.\n";
    }

    /**
     * Iterates through the DataStore to print every registered course, categorized by academic year.
     */
    private String listAllCourses() {
        StringBuilder sb = new StringBuilder();
        sb.append("COURSE CATALOGUE — Computer Science, UWindsor\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("%-10s %-46s %-4s %-5s %s%n",
                "Code", "Name", "Cr", "Year", "Term"));
        sb.append("-".repeat(72)).append("\n");

        int currentYear = 0;
        for (DataStore.Course c : DataStore.courses) {
            // Print a sub-header whenever the loop transitions to a new academic year
            if (c.year != currentYear) {
                sb.append(String.format("%n  -- Year %d --%n", c.year));
                currentYear = c.year;
            }
            sb.append(String.format("  %-10s %-46s %-4d %-5d %s%n",
                    c.code, c.name.substring(0, Math.min(c.name.length(), 46)),
                    c.credits, c.year, c.term));
        }
        sb.append("\nTotal courses: ").append(DataStore.courses.length).append("\n");
        return sb.toString();
    }

    /**
     * Prints a global roster of all registered students alongside their current GPA.
     */
    private String listAllStudents() {
        StringBuilder sb = new StringBuilder();
        sb.append("REGISTERED STUDENTS — Winter 2026\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("%-8s %-22s %-5s %-8s %s%n",
                "ID", "Name", "Year", "Program", "GPA"));
        sb.append("-".repeat(50)).append("\n");

        double totalGpa = 0;
        for (DataStore.Student s : DataStore.students) {
            sb.append(String.format("%-8s %-22s %-5d %-8s %.2f%n",
                    s.id, s.name, s.year, s.program, s.gpa));
            totalGpa += s.gpa;
        }

        // Calculate the global average GPA across the entire student body
        double avgGpa = totalGpa / DataStore.students.length;
        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("Total Students: %d  |  Avg GPA: %.2f%n",
                DataStore.students.length, avgGpa));
        return sb.toString();
    }

    /**
     * Simulates the administrative action of linking a new prerequisite to a course.
     * Validates if the courses exist and if the relationship is already present.
     */
    private String addPrerequisite(String course, String prereq) {
        if (course == null || prereq == null) {
            return "Administrative Action: Add Prerequisite\n"
                    + "=".repeat(40) + "\n\n"
                    + "Usage: \"add prerequisite [COURSE] requires [PREREQ]\"\n"
                    + "Example: \"add prerequisite CS466 requires CS455\"\n";
        }

        DataStore.Course c = DataStore.findCourse(course);
        DataStore.Course p = DataStore.findCourse(prereq);
        String cname = c != null ? c.name : course;
        String pname = p != null ? p.name : prereq;

        // Validation: Check if the prerequisite is already linked to prevent duplicates
        boolean alreadyExists = false;
        if (c != null) {
            for (String existing : c.prerequisites) {
                if (existing.equalsIgnoreCase(prereq)) { alreadyExists = true; break; }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Administrative Action: Add Prerequisite\n");
        sb.append("=".repeat(40)).append("\n");
        sb.append(String.format("Course:       %s (%s)%n", course, cname));
        sb.append(String.format("New Prereq:   %s (%s)%n%n", prereq, pname));

        if (alreadyExists) {
            sb.append("Note: ").append(prereq).append(" is already a prerequisite for ")
                    .append(course).append(". No change was necessary.\n");
        } else if (c == null) {
            sb.append("Warning: Course ").append(course).append(" not found in catalogue.\n");
            sb.append("In a production system, this action would be validated before saving.\n");
        } else {
            sb.append("Action logged. In a production system backed by a database,\n");
            sb.append("this would persist and notify enrolled students.\n");
        }
        return sb.toString();
    }

    /**
     * Simulates the administrative action of unlinking a prerequisite from a course.
     */
    private String removePrerequisite(String course, String prereq) {
        if (course == null || prereq == null) {
            return "Usage: \"remove prerequisite [COURSE] [PREREQ]\"\n"
                    + "Example: \"remove prerequisite CS466 CS455\"\n";
        }
        return String.format("Administrative Action: Remove Prerequisite%n"
                        + "=".repeat(40) + "%n"
                        + "Course: %s  |  Prereq to remove: %s%n%n"
                        + "Action logged. In a production system this would update%n"
                        + "the course catalogue and notify affected students.%n",
                course, prereq);
    }

    /**
     * Simulates the workflow for editing an existing course section (e.g., changing instructors/rooms).
     */
    private String updateSection(String input) {
        String code = extractCode(input);
        return "Administrative Action: Update Section\n"
                + "=".repeat(40) + "\n"
                + (code != null ? "Course: " + code + "\n\n" : "\n")
                + "Action logged. In a production system this would allow you to\n"
                + "modify: instructor, days, time, room, or capacity.\n\n"
                + "Current sections can be viewed in the Scheduling tab.\n";
    }

    /**
     * Simulates the workflow for creating a brand new section in the schedule.
     */
    private String addSection(String input) {
        return "Administrative Action: Add New Section\n"
                + "=".repeat(40) + "\n\n"
                + "To add a section in production, provide:\n"
                + "  - Course code\n"
                + "  - Instructor name\n"
                + "  - Days and time\n"
                + "  - Room and capacity\n\n"
                + "Action would be validated against room availability\n"
                + "and instructor schedule before saving.\n";
    }

    /**
     * Iterates through all active sections to calculate global enrollment fill percentages.
     * Useful for admins to determine if more sections need to be opened.
     */
    private String enrollmentStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("ENROLLMENT STATISTICS — Winter 2026\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("%-8s %-36s %-10s %-8s %s%n",
                "Section", "Course", "Enrolled", "Capacity", "Fill%"));
        sb.append("-".repeat(70)).append("\n");

        int totalEnrolled = 0, totalCapacity = 0;
        for (DataStore.Section s : DataStore.sections) {
            DataStore.Course c = DataStore.findCourse(s.courseCode);
            String cname = c != null ? c.name : s.courseCode;

            // Calculate fill percentage for the specific section
            double fill = (double) s.enrolled / s.capacity * 100;
            sb.append(String.format("%-8s %-36s %-10d %-8d %.1f%%%s%n",
                    s.sectionId,
                    cname.substring(0, Math.min(cname.length(), 36)),
                    s.enrolled, s.capacity, fill,
                    s.isFull() ? " **FULL**" : "")); // Flag sections at 100% capacity

            // Aggregate totals for the global average calculation
            totalEnrolled  += s.enrolled;
            totalCapacity  += s.capacity;
        }

        double avgFill = (double) totalEnrolled / totalCapacity * 100;
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Total Enrolled: %d / %d  |  Avg Fill: %.1f%%%n",
                totalEnrolled, totalCapacity, avgFill));
        return sb.toString();
    }

    /**
     * Simulates the registration of a new student entity.
     */
    private String addStudent(String input) {
        return "Administrative Action: Register Student\n"
                + "=".repeat(40) + "\n\n"
                + "In a production system, this would create a new student record\n"
                + "with: name, student ID, program, year, and initial GPA.\n\n"
                + "Action logged for audit trail.\n";
    }

    /**
     * Outputs a simulated warning for destructive database operations.
     */
    private String resetWarning() {
        return "Administrative Action: Reset / Clear Data\n"
                + "=".repeat(40) + "\n\n"
                + "WARNING: This action would permanently clear all in-memory data.\n"
                + "In production, this is a restricted operation requiring:\n"
                + "  - Administrator authentication\n"
                + "  - Two-factor confirmation\n"
                + "  - Audit log entry\n\n"
                + "This action has NOT been executed in the demo environment.\n";
    }

    /**
     * Default fallback response providing the admin with valid command examples.
     */
    private String helpMessage() {
        return "Administration — myAdvice\n"
                + "==========================\n\n"
                + "Available commands:\n\n"
                + "  \"system status\"                         — System health\n"
                + "  \"list all courses\"                      — Full course catalogue\n"
                + "  \"list all students\"                     — Student roster\n"
                + "  \"enrollment stats\"                      — Section fill rates\n"
                + "  \"add prerequisite CS466 requires CS455\" — Add a prereq\n"
                + "  \"remove prerequisite CS466 CS455\"       — Remove a prereq\n"
                + "  \"update section CS470\"                  — Edit a section\n"
                + "  \"add student\"                           — Register new student\n";
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * Uses the defined CODE_PATTERN regex to extract a single course code.
     * Normalizes the result by converting to uppercase and stripping whitespaces.
     */
    private String extractCode(String input) {
        Matcher m = CODE_PATTERN.matcher(input);
        if (m.find()) return m.group(1).toUpperCase().replaceAll("\\s", "");
        return null;
    }

    /**
     * Uses the CODE_PATTERN regex to iteratively extract exactly two course codes
     * from a single string. Required for prerequisite management (Target Course + Prerequisite Course).
     */
    private String[] extractTwoCodes(String input) {
        Matcher m = CODE_PATTERN.matcher(input);
        String first = null, second = null;
        while (m.find()) {
            String code = m.group(1).toUpperCase().replaceAll("\\s", "");
            if (first == null) first = code;
            else { second = code; break; }
        }
        if (first != null && second != null) return new String[]{first, second};
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
}