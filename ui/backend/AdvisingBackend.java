package ui.backend;

import ui.AdvisingSystemInterface;

/**
 * Real backend implementation for the myAdvice Student Advising System.
 * Implements AdvisingSystemInterface and delegates each domain to a dedicated module.
 */
public class AdvisingBackend implements AdvisingSystemInterface {

    private final CurriculumModule curriculum;
    private final SchedulingModule scheduling;
    private final BookingModule    booking;
    private final AdminModule      admin;
    private final ReportModule     report;

    public AdvisingBackend() {
        this.curriculum = new CurriculumModule();
        this.scheduling = new SchedulingModule();
        this.booking    = new BookingModule();
        this.admin      = new AdminModule();
        this.report     = new ReportModule();
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to myAdvice\n"
                + "University of Windsor — School of Computer Science\n"
                + "Winter 2026 Term  |  Student Portal\n\n"
                + "Select a section below to get started.";
    }

    @Override
    public String submitCurriculumAdvising(String input) {
        return curriculum.handle(input);
    }

    @Override
    public String submitScheduling(String input) {
        return scheduling.handle(input);
    }

    @Override
    public String submitBooking(String input) {
        return booking.handle(input);
    }

    @Override
    public String submitAdministration(String input) {
        return admin.handle(input);
    }

    @Override
    public String submitReport(String input) {
        return report.handle(input);
    }
}