package ui;

/**
 * Main interface for the myAdvice Student Advising System.
 * Backend developers should implement this interface to connect
 * their logic to the frontend.
 */
public interface AdvisingSystemInterface {
    String submitCurriculumAdvising(String input);
    String submitScheduling(String input);
    String submitBooking(String input);
    String submitAdministration(String input);
    String submitReport(String input);
    String getWelcomeMessage();
}