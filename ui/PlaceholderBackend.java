package ui;
/**
 * Placeholder implementation of AdvisingSystemInterface.
 * Replace this class (or swap it out via dependency injection)
 * once the backend modules are implemented.
 */
public class PlaceholderBackend implements AdvisingSystemInterface {

    @Override
    public String getWelcomeMessage() {
        return "Welcome to myAdvice";
    }

    @Override
    public String submitCurriculumAdvising(String input) {
        return "⚠  Curriculum Advising has not been implemented yet.\n\n"
                + "Your request was:\n\"" + input + "\"\n\n"
                + "Once the backend team connects this module, your advising "
                + "results will appear here.";
    }

    @Override
    public String submitScheduling(String input) {
        return "⚠  Scheduling has not been implemented yet.\n\n"
                + "Your request was:\n\"" + input + "\"\n\n"
                + "Once the backend team connects this module, your schedule "
                + "options will appear here.";
    }

    @Override
    public String submitBooking(String input) {
        return "⚠  Bookings has not been implemented yet.\n\n"
                + "Your request was:\n\"" + input + "\"\n\n"
                + "Once the backend team connects this module, available "
                + "appointment slots will appear here.";
    }

    @Override
    public String submitAdministration(String input) {
        return "⚠  System Administration has not been implemented yet.\n\n"
                + "Your request was:\n\"" + input + "\"\n\n"
                + "Once the backend team connects this module, administrative "
                + "actions will be processed here.";
    }

    @Override
    public String submitReport(String input) {
        return "⚠  Reports has not been implemented yet.\n\n"
                + "Your request was:\n\"" + input + "\"\n\n"
                + "Once the backend team connects this module, generated "
                + "reports and visualizations will appear here.";
    }
}