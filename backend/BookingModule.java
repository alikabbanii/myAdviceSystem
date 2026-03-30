package ui.backend;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The BookingModule is responsible for handling all student requests related to
 * scheduling, viewing, and canceling appointments with faculty advisors.
 * * IMPORTANT: This is the only stateful module in the application. Unlike the
 * read-only curriculum and scheduling modules, this class actively mutates the
 * DataStore.slots array at runtime to reflect live bookings and cancellations.
 */
public class BookingModule {

    /**
     * Regex pattern to identify specific appointment slots (e.g., "SLOT001").
     * Breakdown: \b (word boundary), "SLOT" (case-insensitive), followed by exactly 3 digits, \b (word boundary).
     */
    private static final Pattern SLOT_PATTERN =
            Pattern.compile("\\bSLOT(\\d{3})\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Main entry point for booking-related queries.
     * Parses the user's raw input string and routes it to the appropriate state-mutation
     * or data-retrieval method based on keyword detection.
     *
     * @param input The raw text input submitted by the user.
     * @return A formatted multi-line string containing confirmations, available slots, or error messages.
     */
    public String handle(String input) {
        // Guard clause: Return the default help menu if the input is null or empty
        if (input == null || input.trim().isEmpty()) {
            return helpMessage();
        }

        // Extract the slot ID early (if present) for use in booking/canceling logic
        String slotId    = extractSlotId(input);

        // Normalize the query to lowercase for case-insensitive keyword matching
        String q         = input.toLowerCase().trim();

        // STATE MUTATION BRANCH: Cancel an existing booking
        if (has(q, "cancel", "remove booking", "delete booking", "unbook")) {
            if (slotId != null) return cancelBooking(slotId);
            return "Please specify a slot ID to cancel (e.g., \"cancel SLOT003\").\n"
                    + "Type \"show available appointments\" to see all slots.";
        }

        // STATE MUTATION BRANCH: Book a new appointment
        if (has(q, "book", "reserve", "schedule appointment", "make appointment")) {
            if (slotId != null) {
                // If a slot ID is provided, attempt to extract the student's name for a personalized booking
                String name = extractName(input);
                return bookSlot(slotId, name);
            }
            // Fallback: If they said "book Dr. Ngom" but forgot the slot ID, show the advisor's available slots
            String advisor = detectAdvisor(q);
            if (advisor != null) {
                return showAdvisorSlots(advisor);
            }
            // Prompt user for correct formatting if neither slot ID nor advisor is detected
            return "Please specify a slot ID (e.g., \"book SLOT001 for [Your Name]\").\n"
                    + "Type \"show available appointments\" to see all slots.";
        }

        // QUERY BRANCH: Thesis / supervisor search matching based on research keywords
        if (has(q, "thesis", "comp400", "supervisor", "research advisor")) {
            return thesisSupervisor(q);
        }

        // QUERY BRANCH: View a specific advisor's schedule
        String advisor = detectAdvisor(q);
        if (advisor != null) {
            return showAdvisorSlots(advisor);
        }

        // QUERY BRANCH: Show all open/unbooked slots globally
        if (has(q, "show", "available", "list", "view", "appointments", "slots", "open")) {
            return showAllAvailable();
        }

        // QUERY BRANCH: Show all currently booked slots across the system
        if (has(q, "my booking", "my appointment", "booked slot", "all booked")) {
            return showAllBooked();
        }

        // Default Fallback: If no keywords match, return the instruction menu
        return helpMessage();
    }

    // -------------------------------------------------------------------------
    // Response builders
    // -------------------------------------------------------------------------

    /**
     * Aggregates and returns a formatted list of all appointment slots across all
     * advisors that currently have 'isBooked' set to false.
     */
    private String showAllAvailable() {
        StringBuilder sb = new StringBuilder();
        sb.append("AVAILABLE ADVISOR APPOINTMENTS — Winter 2026\n");
        sb.append("=".repeat(50)).append("\n");

        for (DataStore.Advisor adv : DataStore.advisors) {
            boolean hasSlot = false;
            StringBuilder advSlots = new StringBuilder();

            // Iterate through the global slots array to find unbooked slots for this specific advisor
            for (DataStore.AppointmentSlot slot : DataStore.slots) {
                if (slot.advisorId.equals(adv.id) && !slot.isBooked) {
                    advSlots.append(String.format("  %-8s %-12s %s%n",
                            slot.slotId, slot.day, slot.time));
                    hasSlot = true;
                }
            }

            // Only append the advisor's header if they have at least one open slot
            if (hasSlot) {
                sb.append(String.format("%n%s  (%s)%n", adv.name, adv.title));
                sb.append(String.format("  Specialization: %s%n", adv.specialization));
                sb.append(advSlots);
            }
        }

        // Calculate global availability metrics
        int available = 0;
        for (DataStore.AppointmentSlot s : DataStore.slots) if (!s.isBooked) available++;

        if (available == 0) {
            sb.append("\nAll appointment slots are currently booked.\n");
            sb.append("Please check back later or contact the department directly.\n");
        } else {
            sb.append(String.format("%n%d slot(s) available.%n", available));
            sb.append("\nTo book: type \"book SLOTXXX for [Your Name]\"\n");
            sb.append("Example: book SLOT003 for Maria Santos\n");
        }
        return sb.toString();
    }

    /**
     * Retrieves and formats all appointment slots (both booked and unbooked)
     * for a specific advisor matched by name or ID.
     */
    private String showAdvisorSlots(String advisorNamePart) {
        DataStore.Advisor matched = null;

        // Find the specific advisor object based on the parsed string
        for (DataStore.Advisor adv : DataStore.advisors) {
            if (adv.name.toLowerCase().contains(advisorNamePart) ||
                    adv.id.toLowerCase().contains(advisorNamePart)) {
                matched = adv;
                break;
            }
        }

        if (matched == null) {
            return "Advisor not found matching \"" + advisorNamePart + "\".\n"
                    + "Available advisors: ngom, gras, kobti, saad, chen";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Appointments — %s%n", matched.name));
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("Title:          %s%n", matched.title));
        sb.append(String.format("Email:          %s%n", matched.email));
        sb.append(String.format("Specialization: %s%n", matched.specialization));
        sb.append("\nAppointment Slots:\n");
        sb.append(String.format("  %-8s %-12s %-10s %s%n", "Slot ID", "Day", "Time", "Status"));
        sb.append("-".repeat(45)).append("\n");

        // Display the booking status of every slot assigned to this advisor
        for (DataStore.AppointmentSlot slot : DataStore.slots) {
            if (slot.advisorId.equals(matched.id)) {
                String status = slot.isBooked
                        ? "BOOKED (" + slot.bookedBy + ")"
                        : "AVAILABLE";
                sb.append(String.format("  %-8s %-12s %-10s %s%n",
                        slot.slotId, slot.day, slot.time, status));
            }
        }
        sb.append("\nTo book an available slot: \"book SLOTXXX for [Your Name]\"\n");
        return sb.toString();
    }

    /**
     * Executes the state mutation to reserve an appointment slot.
     * Validates that the slot exists and is not already booked before modifying DataStore.
     */
    private String bookSlot(String slotId, String studentName) {
        DataStore.AppointmentSlot slot = DataStore.findSlot(slotId);

        // Validation check 1: Does the slot exist?
        if (slot == null) {
            return String.format("Slot %s does not exist.%nType \"show available appointments\" to see valid slot IDs.%n", slotId);
        }
        // Validation check 2: Is the slot already taken?
        if (slot.isBooked) {
            return String.format("Slot %s is already booked (by %s).%n%n"
                            + "Type \"show available appointments\" to find an open slot.%n",
                    slotId, slot.bookedBy);
        }

        // Apply a default name if the regex failed to extract one
        String name = (studentName != null && !studentName.isEmpty()) ? studentName : "Student";

        // *** CRITICAL STATE MUTATION ***
        // These two lines alter the static DataStore array, "saving" the appointment in memory
        slot.isBooked  = true;
        slot.bookedBy  = name;

        DataStore.Advisor adv = DataStore.findAdvisor(slot.advisorId);
        String advName = adv != null ? adv.name : slot.advisorId;

        // Construct the booking confirmation receipt
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment Confirmed!\n");
        sb.append("=".repeat(30)).append("\n");
        sb.append(String.format("Slot ID:     %s%n", slot.slotId));
        sb.append(String.format("Advisor:     %s%n", advName));
        sb.append(String.format("Day / Time:  %s at %s%n", slot.day, slot.time.trim()));
        sb.append(String.format("Booked for:  %s%n", name));
        sb.append("\nPlease arrive 5 minutes early.\n");
        sb.append("Location:    Essex Hall, Room 201 (Advising Office)\n");
        sb.append("A confirmation has been sent to your UWindsor email.\n\n");
        sb.append("To cancel: type \"cancel " + slotId + "\"\n");
        return sb.toString();
    }

    /**
     * Executes the state mutation to free up a previously booked appointment slot.
     * Validates that the slot exists and is currently booked before modifying DataStore.
     */
    private String cancelBooking(String slotId) {
        DataStore.AppointmentSlot slot = DataStore.findSlot(slotId);

        // Validation check 1: Does the slot exist?
        if (slot == null) {
            return String.format("Slot %s does not exist.%n", slotId);
        }
        // Validation check 2: Is the slot actually booked?
        if (!slot.isBooked) {
            return String.format("Slot %s is not currently booked — nothing to cancel.%n", slotId);
        }

        // Store the previous name for the cancellation receipt before overwriting it
        String prevName = slot.bookedBy;
        DataStore.Advisor adv = DataStore.findAdvisor(slot.advisorId);
        String advName = adv != null ? adv.name : slot.advisorId;

        // *** CRITICAL STATE MUTATION ***
        // These two lines free the slot in the static DataStore array
        slot.isBooked = false;
        slot.bookedBy = null;

        return String.format("Appointment Cancelled%n"
                        + "=====================%n"
                        + "Slot %s (%s at %s) has been cancelled.%n"
                        + "Advisor: %s%n"
                        + "Previously booked by: %s%n%n"
                        + "The slot is now AVAILABLE for other students.%n"
                        + "To rebook: type \"show available appointments\"%n",
                slotId, slot.day, slot.time.trim(), advName, prevName);
    }

    /**
     * Iterates through the global DataStore and returns a list of all currently reserved slots.
     */
    private String showAllBooked() {
        StringBuilder sb = new StringBuilder();
        sb.append("BOOKED APPOINTMENTS — Winter 2026\n");
        sb.append("=".repeat(40)).append("\n");
        sb.append(String.format("  %-8s %-22s %-12s %-10s %s%n",
                "Slot", "Advisor", "Day", "Time", "Booked By"));
        sb.append("-".repeat(70)).append("\n");

        int count = 0;
        for (DataStore.AppointmentSlot slot : DataStore.slots) {
            if (slot.isBooked) {
                DataStore.Advisor adv = DataStore.findAdvisor(slot.advisorId);
                String advName = adv != null ? adv.name : slot.advisorId;
                sb.append(String.format("  %-8s %-22s %-12s %-10s %s%n",
                        slot.slotId,
                        advName.substring(0, Math.min(advName.length(), 22)),
                        slot.day, slot.time.trim(), slot.bookedBy));
                count++;
            }
        }
        sb.append(String.format("%n%d appointment(s) currently booked.%n", count));
        return sb.toString();
    }

    /**
     * Cross-references user keywords with faculty research specializations to suggest
     * potential thesis (COMP400) supervisors.
     */
    private String thesisSupervisor(String q) {
        StringBuilder sb = new StringBuilder();
        sb.append("COMP400 / Thesis Supervisor Search\n");
        sb.append("=".repeat(40)).append("\n\n");

        // Define sets of research keywords associated with specific faculty members
        String[] mlKeywords   = {"machine learning", "ml", "ai", "artificial intelligence",
                "bioinformatics", "data science"};
        String[] seKeywords   = {"software engineering", "software", "agile"};
        String[] osKeywords   = {"operating system", "distributed", "agent", "swarm"};
        String[] secKeywords  = {"security", "cybersecurity", "network", "data mining"};

        DataStore.Advisor suggested = null;

        // Check user input against the keyword arrays to find a research match
        if (containsAny(q, mlKeywords))  suggested = DataStore.findAdvisor("ADV001");
        else if (containsAny(q, seKeywords))  suggested = DataStore.findAdvisor("ADV002");
        else if (containsAny(q, osKeywords))  suggested = DataStore.findAdvisor("ADV003");
        else if (containsAny(q, secKeywords)) suggested = DataStore.findAdvisor("ADV004");

        if (suggested != null) {
            // Display specifically matched faculty member and their open slots
            sb.append(String.format("Based on your interest, we suggest:%n%n"));
            sb.append(String.format("  %s (%s)%n", suggested.name, suggested.title));
            sb.append(String.format("  Research: %s%n", suggested.specialization));
            sb.append(String.format("  Email:    %s%n%n", suggested.email));
            sb.append("  Available slots:\n");
            for (DataStore.AppointmentSlot slot : DataStore.slots) {
                if (slot.advisorId.equals(suggested.id) && !slot.isBooked) {
                    sb.append(String.format("    %-8s %-12s %s%n",
                            slot.slotId, slot.day, slot.time));
                }
            }
        } else {
            // Display a global directory if no specific keywords were matched
            sb.append("All research supervisors:\n\n");
            for (DataStore.Advisor adv : DataStore.advisors) {
                if (!adv.id.equals("ADV005")) { // skip general academic advisor (not a research supervisor)
                    sb.append(String.format("  %s — %s%n", adv.name, adv.specialization));
                    sb.append(String.format("  Email: %s%n%n", adv.email));
                }
            }
        }

        sb.append("\nNext steps:\n");
        sb.append("  1. Book a slot: \"book SLOTXXX for [Your Name]\"\n");
        sb.append("  2. Prepare a 1-page research interest statement\n");
        sb.append("  3. Reference the COMP400 course outline from the SCS website\n");
        return sb.toString();
    }

    /**
     * Default fallback response providing the user with valid command examples.
     */
    private String helpMessage() {
        return "Bookings — myAdvice\n"
                + "====================\n\n"
                + "Try one of these queries:\n\n"
                + "  \"show available appointments\"     — All open slots\n"
                + "  \"Dr. Ngom\"                        — One advisor's slots\n"
                + "  \"book SLOT003 for Maria Santos\"   — Book a slot\n"
                + "  \"cancel SLOT003\"                  — Cancel a booking\n"
                + "  \"thesis supervisor\"               — Find a COMP400 supervisor\n"
                + "  \"all booked appointments\"         — See all booked slots\n\n"
                + "Advisors: Dr. Ngom, Dr. Gras, Dr. Kobti, Dr. Saad, Dr. Chen\n";
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    /**
     * Uses the defined SLOT_PATTERN regex to safely extract an appointment slot ID
     * from the user's raw input. Returns null if no match is found.
     */
    private String extractSlotId(String input) {
        Matcher m = SLOT_PATTERN.matcher(input);
        if (m.find()) return "SLOT" + m.group(1);
        return null;
    }

    /**
     * Uses Regex to extract the student's name from a booking command.
     * Looks for the specific pattern "for [Name]" (e.g., "book SLOT001 for John Doe").
     */
    private String extractName(String input) {
        // Looks for the word "for" followed by one or more capital/lowercase letters, spaces, or hyphens
        Pattern namePattern = Pattern.compile("\\bfor\\s+([A-Z][a-zA-Z '-]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = namePattern.matcher(input);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }

    /**
     * Detects if an advisor's last name is explicitly mentioned in the query string.
     */
    private String detectAdvisor(String q) {
        String[] names = {"ngom", "gras", "kobti", "saad", "chen"};
        for (String n : names) if (q.contains(n)) return n;
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
     * Helper to check arrays of keywords (primarily used for the thesis research matching).
     */
    private boolean containsAny(String q, String[] terms) {
        for (String t : terms) if (q.contains(t)) return true;
        return false;
    }
}