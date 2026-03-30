package ui.backend;

/**
 * The ReportModule is responsible for generating comprehensive text-based
 * analytics and data summaries. It aggregates raw arrays from the DataStore
 * to calculate complex metrics such as GPAs, enrollment fill rates, advisor
 * loads, and demographic distributions.
 */
public class ReportModule {

    /**
     * Main entry point for reporting queries.
     * Parses the user's raw input string and routes it to the specific
     * data aggregation method based on keyword matching.
     *
     * @param input The raw text request submitted by the user.
     * @return A formatted, multi-line statistical report string.
     */
    public String handle(String input) {
        // Guard clause: Return the default help menu if the input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return helpMessage();
        }

        // Normalize the query to lowercase for case-insensitive keyword matching
        String q = input.toLowerCase().trim();

        // REPORT BRANCH: Master summary containing all sub-reports chained together
        if (has(q, "full report", "summary report", "all report", "complete report")) {
            return fullSummaryReport();
        }

        // REPORT BRANCH: Section-by-section enrollment capacities and warnings
        if (has(q, "enrollment report", "enrollment by course", "enrollment stat")) {
            return enrollmentReport();
        }

        // REPORT BRANCH: Simulated grade curves based on historical UWindsor CS data
        if (has(q, "grade distribution", "grade report", "grades")) {
            return gradeDistributionReport();
        }

        // REPORT BRANCH: Advisor booking percentages to identify bottlenecks
        if (has(q, "advisor load", "advising load", "advisor report", "booking report")) {
            return advisorLoadReport();
        }

        // REPORT BRANCH: Demographic breakdown of registered students by academic year
        if (has(q, "year distribution", "students by year", "year breakdown")) {
            return yearDistributionReport();
        }

        // REPORT BRANCH: Student performance tracking (averages, maximums, and standing tiers)
        if (has(q, "gpa report", "gpa stat", "gpa summary", "grade point")) {
            return gpaReport();
        }

        // REPORT BRANCH: Core catalogue metrics (credits per year, total active sections)
        if (has(q, "course stat", "course report", "course summary")) {
            return courseStatsReport();
        }

        // Default Fallback: Return the instruction menu if no keywords match
        return helpMessage();
    }

    // -------------------------------------------------------------------------
    // Report builders
    // -------------------------------------------------------------------------

    /**
     * Calculates and formats a detailed report on course enrollment.
     * Flags sections that are completely full or dangerously under-enrolled (< 60%).
     */
    private String enrollmentReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("ENROLLMENT REPORT — Winter 2026\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append(String.format("%-8s %-38s %-10s %-9s %s%n",
                "Section", "Course Name", "Enrolled", "Capacity", "Fill%"));
        sb.append("-".repeat(70)).append("\n");

        int totalEnrolled = 0, totalCapacity = 0;
        int fullCount = 0;
        int underCount = 0;

        for (DataStore.Section s : DataStore.sections) {
            DataStore.Course c = DataStore.findCourse(s.courseCode);
            String cname = c != null ? c.name : s.courseCode;

            // Calculate fill percentage for the current section
            double fill = (double) s.enrolled / s.capacity * 100;

            // Flag specific capacity thresholds for administrative review
            String flag = s.isFull() ? " ** FULL **" : (fill < 60 ? " (low)" : "");

            sb.append(String.format("%-8s %-38s %-10d %-9d %.1f%%%s%n",
                    s.sectionId,
                    cname.substring(0, Math.min(cname.length(), 38)),
                    s.enrolled, s.capacity, fill, flag));

            // Aggregate totals for the master footer
            totalEnrolled += s.enrolled;
            totalCapacity += s.capacity;
            if (s.isFull()) fullCount++;
            if (fill < 60) underCount++;
        }

        double avgFill = (double) totalEnrolled / totalCapacity * 100;
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Total Enrollment:   %d students across %d active sections%n",
                totalEnrolled, DataStore.sections.length));
        sb.append(String.format("Average Fill Rate:  %.1f%%%n", avgFill));
        sb.append(String.format("Full Sections:      %d%n", fullCount));
        sb.append(String.format("Under-enrolled (<60%%): %d%n", underCount));
        return sb.toString();
    }

    /**
     * Generates a simulated historical grade distribution report.
     * Provides passing rates and high-achievement (A/A+) rates to help students gauge course difficulty.
     */
    private String gradeDistributionReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIMULATED GRADE DISTRIBUTION — Fall 2025\n");
        sb.append("(Based on historical data for UWindsor CS cohort)\n");
        sb.append("=".repeat(62)).append("\n");
        sb.append(String.format("%-8s %5s %5s %5s %5s %5s %5s %5s %5s%n",
                "Course", "A+", "A", "B+", "B", "C+", "C", "D", "F"));
        sb.append("-".repeat(62)).append("\n");

        // Simulated distributions per course representing realistic bell curves
        String[][] dists = {
                {"CS110", "12", "21", "18", "20", "12",  "9", "5", "3"},
                {"CS114", "10", "19", "20", "22", "14", "10", "3", "2"},
                {"CS253", "11", "18", "22", "21", "13",  "9", "4", "2"},
                {"CS340",  "9", "20", "23", "20", "14",  "8", "4", "2"},
                {"CS440",  "8", "17", "21", "23", "15", "10", "4", "2"},
                {"CS460",  "7", "15", "20", "24", "16", "11", "5", "2"},
                {"CS465",  "9", "18", "21", "22", "14",  "9", "5", "2"},
                {"CS470", "10", "19", "22", "20", "14",  "9", "4", "2"},
                {"CS350", "12", "22", "20", "19", "13",  "8", "4", "2"},
                {"CS475",  "8", "16", "20", "24", "17", "10", "3", "2"},
        };

        double totalPassRate = 0;
        double totalHighRate = 0;
        for (String[] d : dists) {
            sb.append(String.format("%-8s %5s%% %4s%% %4s%% %4s%% %4s%% %4s%% %4s%% %4s%%%n",
                    d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8]));

            // Calculate pass rate (100% minus the F column)
            totalPassRate += 100 - Integer.parseInt(d[8]);

            // Calculate high-achiever rate (A+ column plus A column)
            totalHighRate += Integer.parseInt(d[1]) + Integer.parseInt(d[2]);
        }
        sb.append("-".repeat(62)).append("\n");
        sb.append(String.format("Avg Pass Rate (no F): %.1f%%   |   Avg A/A+ Rate: %.1f%%%n",
                totalPassRate / dists.length,
                totalHighRate / dists.length));
        sb.append("\nNote: Distributions are representative historical estimates.\n");
        sb.append("Most Challenging: CS460 — only 22% A/A+\n");
        sb.append("Most Accessible:  CS110 — 33% A/A+\n");
        return sb.toString();
    }

    /**
     * Calculates the booking capacity percentage for every faculty advisor.
     * Helps administration identify if certain advisors are overwhelmed with appointments.
     */
    private String advisorLoadReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("ADVISOR BOOKING LOAD — Winter 2026\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append(String.format("%-24s %-22s %6s %6s %6s %s%n",
                "Advisor", "Role", "Total", "Booked", "Avail", "Load%"));
        sb.append("-".repeat(70)).append("\n");

        int grandTotal = 0, grandBooked = 0;
        for (DataStore.Advisor adv : DataStore.advisors) {
            int total = 0, booked = 0;
            // Scan all slots to tally totals specific to this advisor
            for (DataStore.AppointmentSlot slot : DataStore.slots) {
                if (slot.advisorId.equals(adv.id)) {
                    total++;
                    if (slot.isBooked) booked++;
                }
            }
            int avail = total - booked;
            double load = (double) booked / total * 100;

            sb.append(String.format("%-24s %-22s %6d %6d %6d %.0f%%%n",
                    adv.name.substring(0, Math.min(adv.name.length(), 24)),
                    adv.title.substring(0, Math.min(adv.title.length(), 22)),
                    total, booked, avail, load));

            grandTotal += total;
            grandBooked += booked;
        }

        double overallLoad = (double) grandBooked / grandTotal * 100;
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Total Slots: %d  |  Booked: %d  |  Available: %d  |  Overall Load: %.0f%%%n",
                grandTotal, grandBooked, grandTotal - grandBooked, overallLoad));
        sb.append("\nRecommendation: Dr. Jessica Chen handles general advising —\n");
        sb.append("direct non-research students to her for faster turnaround.\n");
        return sb.toString();
    }

    /**
     * Aggregates the student body into cohorts based on their current academic year.
     * Renders a rudimentary ASCII bar chart for quick visual comprehension.
     */
    private String yearDistributionReport() {
        int[] counts = new int[5]; // index 1-4 mapped to Year 1-Year 4
        for (DataStore.Student s : DataStore.students) {
            if (s.year >= 1 && s.year <= 4) counts[s.year]++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT YEAR DISTRIBUTION — Winter 2026\n");
        sb.append("=".repeat(40)).append("\n");

        String[] labels = {"", "Year 1 (Freshman)", "Year 2 (Sophomore)",
                "Year 3 (Junior)", "Year 4 (Senior)"};

        // Loop through the tallied array to build the ASCII chart
        for (int y = 1; y <= 4; y++) {
            int pct = (int) Math.round((double) counts[y] / DataStore.students.length * 100);
            String bar = "#".repeat(pct / 5); // 1 hashtag symbol per 5%
            sb.append(String.format("  %-20s: %2d students (%2d%%)  %s%n",
                    labels[y], counts[y], pct, bar));
        }
        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format("Total: %d students%n", DataStore.students.length));
        return sb.toString();
    }

    /**
     * Aggregates GPA data across the student body to determine academic standings.
     * Calculates averages, maximums, minimums, and percentage breakdowns for Dean's List vs Academic Probation.
     */
    private String gpaReport() {
        double total = 0, min = Double.MAX_VALUE, max = 0;
        int above35 = 0, between3and35 = 0, below3 = 0;

        for (DataStore.Student s : DataStore.students) {
            total += s.gpa;
            if (s.gpa < min) min = s.gpa;
            if (s.gpa > max) max = s.gpa;

            // Bucket students into academic standing tiers
            if (s.gpa >= 3.5) above35++;
            else if (s.gpa >= 3.0) between3and35++;
            else below3++;
        }
        double avg = total / DataStore.students.length;

        StringBuilder sb = new StringBuilder();
        sb.append("GPA SUMMARY REPORT — Winter 2026\n");
        sb.append("=".repeat(40)).append("\n");
        sb.append(String.format("Students Analyzed: %d%n", DataStore.students.length));
        sb.append(String.format("Average GPA:       %.2f%n", avg));
        sb.append(String.format("Highest GPA:       %.2f%n", max));
        sb.append(String.format("Lowest GPA:        %.2f%n", min));
        sb.append("\nDistribution:\n");
        sb.append(String.format("  GPA >= 3.5  (Dean's List):  %d students (%.0f%%)%n",
                above35, (double) above35 / DataStore.students.length * 100));
        sb.append(String.format("  GPA 3.0-3.4 (Good Standing): %d students (%.0f%%)%n",
                between3and35, (double) between3and35 / DataStore.students.length * 100));
        sb.append(String.format("  GPA < 3.0   (Needs Review):  %d students (%.0f%%)%n",
                below3, (double) below3 / DataStore.students.length * 100));
        sb.append("\nNote: Minimum GPA for graduation is 2.0.\n");
        sb.append("Students below 3.0 are encouraged to meet with their advisor.\n");
        return sb.toString();
    }

    /**
     * Calculates catalogue-level metrics, showing how the degree's required credits
     * are distributed across the 4-year plan.
     */
    private String courseStatsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("COURSE STATISTICS REPORT — Winter 2026\n");
        sb.append("=".repeat(50)).append("\n");

        int totalCredits = 0;
        int[] yearCounts = new int[5];
        int[] yearCredits = new int[5];

        for (DataStore.Course c : DataStore.courses) {
            totalCredits += c.credits;
            if (c.year >= 1 && c.year <= 4) {
                yearCounts[c.year]++;
                yearCredits[c.year] += c.credits;
            }
        }

        sb.append(String.format("Total Courses in Catalogue: %d%n", DataStore.courses.length));
        sb.append(String.format("Total Core Credit Hours:    %d%n%n", totalCredits));
        sb.append(String.format("%-8s %-10s %-10s%n", "Year", "Courses", "Credits"));
        sb.append("-".repeat(30)).append("\n");
        for (int y = 1; y <= 4; y++) {
            sb.append(String.format("Year %-4d %-10d %d%n", y, yearCounts[y], yearCredits[y]));
        }
        sb.append("-".repeat(30)).append("\n");

        int activeSections = DataStore.sections.length;
        int totalEnrolled = 0;
        for (DataStore.Section s : DataStore.sections) totalEnrolled += s.enrolled;

        sb.append(String.format("%nActive Sections This Term: %d%n", activeSections));
        sb.append(String.format("Total Students Enrolled:   %d%n", totalEnrolled));

        // Relies on a helper method to remove duplicate sections of the same course
        sb.append(String.format("Courses with sections:     %d%n",
                countUniqueCourses()));

        return sb.toString();
    }

    /**
     * A master function that calls and concatenates several smaller reports into
     * one massive text block for system administrators.
     */
    private String fullSummaryReport() {
        return "FULL SYSTEM REPORT — myAdvice, Winter 2026\n"
                + "=".repeat(50) + "\n\n"
                + "[ 1 ] ENROLLMENT\n" + "-".repeat(30) + "\n"
                + enrollmentReport() + "\n\n"
                + "[ 2 ] YEAR DISTRIBUTION\n" + "-".repeat(30) + "\n"
                + yearDistributionReport() + "\n\n"
                + "[ 3 ] GPA SUMMARY\n" + "-".repeat(30) + "\n"
                + gpaReport() + "\n\n"
                + "[ 4 ] ADVISOR LOAD\n" + "-".repeat(30) + "\n"
                + advisorLoadReport();
    }

    /**
     * Default fallback response providing the user with valid reporting commands.
     */
    private String helpMessage() {
        return "Reports — myAdvice\n"
                + "===================\n\n"
                + "Available reports:\n\n"
                + "  \"enrollment report\"     — Enrollment by section\n"
                + "  \"grade distribution\"    — Simulated grade breakdowns\n"
                + "  \"advisor load report\"   — Booking load per advisor\n"
                + "  \"year distribution\"     — Students per academic year\n"
                + "  \"gpa report\"            — GPA statistics and distribution\n"
                + "  \"course stats\"          — Course catalogue statistics\n"
                + "  \"full report\"           — All of the above combined\n";
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * A utility method to check if the user's query contains ANY of the provided keyword strings.
     * Simplifies the logic branching in the main handle() method.
     */
    private boolean has(String q, String... terms) {
        for (String t : terms) if (q.contains(t)) return true;
        return false;
    }

    /**
     * Iterates through the active sections and uses a HashSet to determine
     * exactly how many unique courses are currently being taught this term.
     */
    private int countUniqueCourses() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (DataStore.Section s : DataStore.sections) seen.add(s.courseCode);
        return seen.size();
    }
}