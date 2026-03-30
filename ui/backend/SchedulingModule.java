package ui.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SchedulingModule handles student requests related to course timetables,
 * instructor searches, and section availability. It filters the static DataStore
 * to find and display sections matching the user's criteria (time, day, instructor, or code).
 */
public class SchedulingModule {

    /**
     * Regex pattern to match standard university course codes (e.g., "CS470" or "MATH 2150").
     * Breakdown: \b (word boundary), 2-4 letters, optional space, 3-4 digits, \b (word boundary).
     */
    private static final Pattern CODE_PATTERN =
            Pattern.compile("\\b([A-Za-z]{2,4}\\s?\\d{3,4})\\b");

    /**
     * Main entry point for scheduling queries.
     * Routes the user's raw text request to the appropriate scheduling filter
     * by detecting specific keywords or course codes.
     *
     * @param input The raw text input from the user.
     * @return A formatted multi-line string displaying the requested schedule data.
     */
    public String handle(String input) {
        // Guard clause: Return the default help menu if the input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return helpMessage();
        }

        // Attempt to extract a course code from the raw string
        String detectedCode = extractCode(input);

        // Normalize the query to lowercase for reliable keyword matching
        String q = input.toLowerCase().trim();

        // LOGIC BRANCH 1: Search by specific course code
        // If the user entered a valid code (e.g., "CS253"), return all sections for that course
        if (detectedCode != null) {
            return sectionsByCourse(detectedCode);
        }

        // LOGIC BRANCH 2: View all sections globally
        if (has(q, "all section", "full schedule", "show schedule", "show all",
                "list section", "every section", "schedule")) {
            return allSectionsTable(false);
        }

        // LOGIC BRANCH 3: Filter by seat availability
        // Shows only sections that currently have open seats
        if (has(q, "available", "open seat", "seats available", "has seats", "not full")) {
            return allSectionsTable(true);
        }

        // LOGIC BRANCH 4: Filter for full/waitlisted sections
        if (has(q, "full section", "no seat", "capacity", "waitlist")) {
            return fullSections();
        }

        // LOGIC BRANCH 5: Filter by instructor name
        String instructor = detectInstructor(q);
        if (instructor != null) {
            return sectionsByInstructor(instructor);
        }

        // LOGIC BRANCH 6: Filter by day of the week
        if (has(q, "monday"))    return sectionsByDay("Mon");
        if (has(q, "tuesday"))   return sectionsByDay("Tue");
        if (has(q, "wednesday")) return sectionsByDay("Wed");
        if (has(q, "thursday"))  return sectionsByDay("Thu");
        if (has(q, "friday"))    return sectionsByDay("Fri");

        // LOGIC BRANCH 7: Filter by time of day
        if (has(q, "morning"))   return sectionsByTime(true);
        if (has(q, "afternoon")) return sectionsByTime(false);

        // Default Fallback: If no keywords match, return the instruction menu
        return helpMessage();
    }

    // -------------------------------------------------------------------------
    // Response builders
    // -------------------------------------------------------------------------

    /**
     * Iterates through the entire DataStore to build a comprehensive table of all sections.
     * * @param availableOnly If true, filters out sections that have reached maximum capacity.
     */
    private String allSectionsTable(boolean availableOnly) {
        StringBuilder sb = new StringBuilder();
        if (availableOnly) {
            sb.append("AVAILABLE COURSE SECTIONS — Winter 2026\n");
        } else {
            sb.append("ALL COURSE SECTIONS — Winter 2026\n");
        }
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("%-6s %-6s %-16s %-12s %-11s %-22s %s%n",
                "Sec", "Course", "Instructor", "Days", "Time", "Room", "Seats"));
        sb.append("-".repeat(90)).append("\n");

        int shown = 0;
        for (DataStore.Section s : DataStore.sections) {
            // Skip this section if the user only wants available seats and this one is full
            if (availableOnly && s.isFull()) continue;
            appendSectionRow(sb, s);
            shown++;
        }

        if (shown == 0) {
            sb.append("No sections match your criteria.\n");
        }

        // If filtering by availability, append a convenient summary of the full sections at the bottom
        if (availableOnly) {
            int fullCount = 0;
            StringBuilder fullList = new StringBuilder();
            for (DataStore.Section s : DataStore.sections) {
                if (s.isFull()) {
                    fullCount++;
                    DataStore.Course c = DataStore.findCourse(s.courseCode);
                    String cname = c != null ? c.name : s.courseCode;
                    fullList.append(String.format("  %s (%s — %s)%n", s.sectionId, s.courseCode, cname));
                }
            }
            if (fullCount > 0) {
                sb.append("\nFULL sections (contact department for waitlist):\n");
                sb.append(fullList);
            }
        }

        sb.append("\nTo enroll, contact the Registrar's Office or use UWinsite Student.\n");
        return sb.toString();
    }

    /**
     * Searches the DataStore for all active sections belonging to a specific course code.
     */
    private String sectionsByCourse(String code) {
        DataStore.Course course = DataStore.findCourse(code);
        String courseName = course != null ? course.name : code;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Sections for %s — %s%n", code, courseName));
        sb.append("=".repeat(50)).append("\n");

        boolean found = false;
        for (DataStore.Section s : DataStore.sections) {
            if (s.courseCode.equalsIgnoreCase(code)) {
                found = true;
                sb.append(String.format("%n%s%n", s.sectionId));
                sb.append(String.format("  Instructor: %s%n", s.instructor));
                sb.append(String.format("  Days:       %s%n", s.days));
                sb.append(String.format("  Time:       %s AM/PM – %s%n", s.startTime, s.endTime));
                sb.append(String.format("  Room:       %s%n", s.room));
                sb.append(String.format("  Enrollment: %d enrolled / %d capacity%n",
                        s.enrolled, s.capacity));
                if (s.isFull()) {
                    sb.append("  Status:     FULL — contact department for waitlist\n");
                } else {
                    sb.append(String.format("  Status:     OPEN (%d seat(s) available)%n",
                            s.seatsAvailable()));
                }
            }
        }

        if (!found) {
            sb.append("\nNo active sections found for ").append(code).append(" this term.\n");
            sb.append("This course may not be offered in Winter 2026, or check the course code.\n");
        } else {
            sb.append("\nTo enroll, contact the Registrar's Office or use UWinsite Student.\n");
        }

        return sb.toString();
    }

    /**
     * Iterates through all sections and returns only those that have 0 open seats.
     */
    private String fullSections() {
        StringBuilder sb = new StringBuilder();
        sb.append("FULL SECTIONS — Winter 2026\n");
        sb.append("=".repeat(40)).append("\n");

        boolean found = false;
        for (DataStore.Section s : DataStore.sections) {
            if (s.isFull()) {
                DataStore.Course c = DataStore.findCourse(s.courseCode);
                String cname = c != null ? c.name : s.courseCode;
                sb.append(String.format("%n%s — %s (%s)%n", s.sectionId, s.courseCode, cname));
                sb.append(String.format("  Instructor: %s%n", s.instructor));
                sb.append(String.format("  Days/Time:  %s  %s-%s%n", s.days, s.startTime, s.endTime));
                sb.append(String.format("  Room:       %s%n", s.room));
                sb.append(String.format("  Capacity:   %d / %d (FULL)%n", s.enrolled, s.capacity));
                sb.append("  Action:     Contact department for waitlist options.\n");
                found = true;
            }
        }
        if (!found) {
            sb.append("No sections are currently full. Plenty of seats available!\n");
        }
        return sb.toString();
    }

    /**
     * Filters sections based on a substring match with the instructor's name.
     */
    private String sectionsByInstructor(String namePart) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Sections by Instructor (matching \"%s\")\n", namePart));
        sb.append("=".repeat(50)).append("\n");

        boolean found = false;
        for (DataStore.Section s : DataStore.sections) {
            // Case-insensitive substring match
            if (s.instructor.toLowerCase().contains(namePart.toLowerCase())) {
                found = true;
                DataStore.Course c = DataStore.findCourse(s.courseCode);
                String cname = c != null ? c.name : s.courseCode;
                sb.append(String.format("%n%s — %s: %s%n", s.sectionId, s.courseCode, cname));
                sb.append(String.format("  %s  |  %s  %s-%s  |  %s%n",
                        s.instructor, s.days, s.startTime, s.endTime, s.room));
                sb.append(String.format("  Seats: %d/%d  (%s)%n",
                        s.enrolled, s.capacity,
                        s.isFull() ? "FULL" : s.seatsAvailable() + " open"));
            }
        }
        if (!found) {
            sb.append("No instructor matching that name was found.\n");
            sb.append("Try: ngom, gras, kobti, saad, biniaz, ahmad, rueda, wu\n");
        }
        return sb.toString();
    }

    /**
     * Filters sections to show only classes scheduled on a specific day prefix.
     */
    private String sectionsByDay(String dayPrefix) {
        // Map the abbreviated prefix back to a full string for the header
        String dayLabel = dayPrefix.equals("Mon") ? "MONDAY/WEDNESDAY" :
                dayPrefix.equals("Tue") ? "TUESDAY/THURSDAY" :
                        dayPrefix.equals("Wed") ? "WEDNESDAY" :
                                dayPrefix.equals("Thu") ? "THURSDAY" : "FRIDAY";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SECTIONS ON %s — Winter 2026%n", dayLabel));
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("%-6s %-6s %-16s %-12s %-11s %s%n",
                "Sec", "Course", "Instructor", "Days", "Time", "Seats"));
        sb.append("-".repeat(70)).append("\n");

        boolean found = false;
        for (DataStore.Section s : DataStore.sections) {
            // Check if the section's day string contains the requested prefix
            if (s.days.contains(dayPrefix)) {
                appendSectionRow(sb, s);
                found = true;
            }
        }
        if (!found) {
            sb.append("No sections found on that day.\n");
        } else {
            sb.append("\nNote: Mon/Wed and Tue/Thu sections meet both days.\n");
        }
        return sb.toString();
    }

    /**
     * Filters sections based on start time (Morning vs. Afternoon/Evening).
     * * @param morning If true, returns classes starting before 12:00 PM.
     */
    private String sectionsByTime(boolean morning) {
        StringBuilder sb = new StringBuilder();
        sb.append(morning ? "MORNING SECTIONS (before 12:00)\n" : "AFTERNOON SECTIONS (12:00+)\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("%-6s %-6s %-16s %-12s %-11s %s%n",
                "Sec", "Course", "Instructor", "Days", "Time", "Seats"));
        sb.append("-".repeat(70)).append("\n");

        boolean found = false;
        for (DataStore.Section s : DataStore.sections) {
            // Parse the hour portion of the start time (e.g., "09" from "09:00")
            int hour = Integer.parseInt(s.startTime.split(":")[0]);
            boolean isMorning = hour < 12;

            // If the section's time state matches the requested time state, append it
            if (isMorning == morning) {
                appendSectionRow(sb, s);
                found = true;
            }
        }
        if (!found) sb.append("No sections found.\n");
        return sb.toString();
    }

    /**
     * Helper method to append a uniformly formatted row to a StringBuilder table.
     * Keeps code DRY (Don't Repeat Yourself) when constructing schedule lists.
     */
    private void appendSectionRow(StringBuilder sb, DataStore.Section s) {
        String seats = s.isFull() ? "FULL" : s.seatsAvailable() + "/" + s.capacity;
        sb.append(String.format("%-6s %-6s %-16s %-12s %s-%s  %-22s %s%n",
                s.sectionId, s.courseCode,
                // Truncate instructor and room names to preserve column alignment
                s.instructor.replace("Dr. ", "Dr.").substring(0, Math.min(s.instructor.length(), 16)),
                s.days,
                s.startTime, s.endTime,
                s.room.substring(0, Math.min(s.room.length(), 22)),
                seats));
    }

    /**
     * Default fallback response providing the user with valid command examples.
     */
    private String helpMessage() {
        return "Scheduling — myAdvice\n"
                + "======================\n\n"
                + "Try one of these queries:\n\n"
                + "  \"show all sections\"         — Full schedule table\n"
                + "  \"available sections\"        — Sections with open seats\n"
                + "  \"full sections\"             — Sections at capacity\n"
                + "  \"CS470\"                     — All sections for a course\n"
                + "  \"sections by ngom\"          — Sections by instructor\n"
                + "  \"sections on Tuesday\"       — Sections on a specific day\n"
                + "  \"morning sections\"          — Before 12:00 PM\n"
                + "  \"afternoon sections\"        — 12:00 PM and later\n";
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
     * Detects if any recognized faculty instructor's last name is explicitly
     * mentioned in the query string.
     */
    private String detectInstructor(String q) {
        String[] names = {"ngom", "gras", "kobti", "saad", "biniaz", "ahmad", "rueda", "wu"};
        for (String n : names) {
            if (q.contains(n)) return n;
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
}