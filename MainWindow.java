package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainWindow extends JFrame {

    private static final String CARD_HOME           = "HOME";
    private static final String CARD_CURRICULUM     = "CURRICULUM";
    private static final String CARD_SCHEDULING     = "SCHEDULING";
    private static final String CARD_BOOKINGS       = "BOOKINGS";
    private static final String CARD_ADMINISTRATION = "ADMINISTRATION";
    private static final String CARD_REPORTS        = "REPORTS";

    private static final String DEFAULT_RESPONSE_TEXT =
            "Responses will appear here after you submit a request.";
    private static final String EMPTY_REQUEST_WARNING =
            "⚠  Please enter a request before submitting.";

    private static final Color COLOR_PRIMARY    = new Color(0, 60, 113);
    private static final Color COLOR_ACCENT     = new Color(255, 215, 0);
    private static final Color COLOR_BG         = new Color(245, 247, 250);
    private static final Color COLOR_CARD_BG    = Color.WHITE;
    private static final Color COLOR_TEXT_DARK  = new Color(30, 30, 30);
    private static final Color COLOR_TEXT_LIGHT = Color.WHITE;
    private static final Color COLOR_BTN_HOVER  = new Color(0, 85, 160);

    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_TEXT     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_EMOJI_13 = new Font("Segoe UI Emoji", Font.PLAIN, 13);

    private final AdvisingSystemInterface backend;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    public MainWindow(AdvisingSystemInterface backend) {
        this.backend = backend;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        initFrame();
        buildCards();
        cardLayout.show(cardPanel, CARD_HOME);
    }

    private void initFrame() {
        setTitle("myAdvice — Student Advising System | University of Windsor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setPreferredSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        add(buildTopBar(), BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);
        pack();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(COLOR_PRIMARY);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel appName = new JLabel("myAdvice");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appName.setForeground(COLOR_ACCENT);

        JLabel tagLine = new JLabel("  |  Student Advising System — University of Windsor SCS");
        tagLine.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagLine.setForeground(new Color(200, 210, 225));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(appName);
        left.add(tagLine);

        JButton homeBtn = makeNavButton("🏠  Home");
        homeBtn.addActionListener(e -> showCard(CARD_HOME));

        bar.add(left, BorderLayout.WEST);
        bar.add(homeBtn, BorderLayout.EAST);
        return bar;
    }

    private JButton makeNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI_13);
        btn.setForeground(COLOR_TEXT_LIGHT);
        btn.setBackground(COLOR_PRIMARY);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addHoverColor(btn, COLOR_PRIMARY, COLOR_BTN_HOVER);
        return btn;
    }

    private void addHoverColor(JButton button, Color normal, Color hover) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { button.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { button.setBackground(normal); }
        });
    }

    private void showCard(String cardId) {
        cardLayout.show(cardPanel, cardId);
    }

    private void buildCards() {
        cardPanel.setBackground(COLOR_BG);
        cardPanel.add(buildHomeCard(), CARD_HOME);

        addSectionCard(CARD_CURRICULUM, "📚  Curriculum Advising",
                "Plan your future courses and get advice on your degree requirements.",
                "Describe your academic goals or questions about required courses...",
                this::submitCurriculumAdvising);

        addSectionCard(CARD_SCHEDULING, "🗓  Scheduling",
                "Plan your course schedule — choose sections, instructors, and times.",
                "Describe the courses or time preferences you'd like to schedule...",
                this::submitScheduling);

        addSectionCard(CARD_BOOKINGS, "📅  Bookings",
                "Book an appointment with a faculty advisor or find a COMP 400/405 supervisor.",
                "Describe who you'd like to meet with or what you need help with...",
                this::submitBooking);

        addSectionCard(CARD_ADMINISTRATION, "⚙️  System Administration",
                "Manage prerequisite structures, timetables, profiles, and transcripts.",
                "Enter your administrative request or data update...",
                this::submitAdministration);

        addSectionCard(CARD_REPORTS, "📊  Reports",
                "Generate reports and visualizations on students, faculty, courses, and terms.",
                "Specify the report or filter criteria you need...",
                this::submitReport);
    }

    private void addSectionCard(String cardId, String title, String description,
                                String inputPlaceholder, java.util.function.Function<String, String> submitHandler) {
        cardPanel.add(buildSectionCard(title, description, inputPlaceholder, submitHandler), cardId);
    }

    private JPanel buildHomeCard() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(COLOR_BG);
        outer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(COLOR_PRIMARY);
        banner.setBorder(new EmptyBorder(24, 30, 24, 30));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel welcome = new JLabel(backend.getWelcomeMessage());
        welcome.setFont(FONT_TITLE);
        welcome.setForeground(COLOR_TEXT_LIGHT);

        JLabel sub = new JLabel("Select a section below to get started.");
        sub.setFont(FONT_SUBTITLE);
        sub.setForeground(new Color(190, 205, 225));

        JPanel bannerText = new JPanel();
        bannerText.setOpaque(false);
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.add(welcome);
        bannerText.add(Box.createVerticalStrut(6));
        bannerText.add(sub);

        banner.add(bannerText, BorderLayout.CENTER);
        outer.add(banner, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBackground(COLOR_BG);
        grid.setBorder(new EmptyBorder(30, 0, 0, 0));

        grid.add(buildDashboardButton("📚", "Curriculum\nAdvising", "Plan your degree path", CARD_CURRICULUM));
        grid.add(buildDashboardButton("🗓", "Scheduling", "Pick courses & sections", CARD_SCHEDULING));
        grid.add(buildDashboardButton("📅", "Bookings", "Meet with an advisor", CARD_BOOKINGS));
        grid.add(buildDashboardButton("⚙", "Administration", "Manage system data", CARD_ADMINISTRATION));
        grid.add(buildDashboardButton("📊", "Reports", "View reports & dashboards", CARD_REPORTS));

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        grid.add(filler);

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildDashboardButton(String icon, String title, String description, String cardId) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        iconLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel titleLabel = new JLabel(title.replace("\n", " "));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(COLOR_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(FONT_TEXT);
        descLabel.setForeground(new Color(100, 110, 125));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(235, 241, 250));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_PRIMARY, 2, true),
                        new EmptyBorder(19, 19, 19, 19)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(COLOR_CARD_BG);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 218, 230), 1, true),
                        new EmptyBorder(20, 20, 20, 20)
                ));
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCard(cardId);
            }
        });

        return panel;
    }

    private JPanel buildSectionCard(String title, String description, String inputPlaceholder,
                                    java.util.function.Function<String, String> submitHandler) {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(COLOR_BG);
        outer.setBorder(new EmptyBorder(30, 40, 30, 40));

        outer.add(buildSectionHeader(title, description), BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel inputLabel = new JLabel("Your Request:");
        inputLabel.setFont(FONT_LABEL);
        inputLabel.setForeground(COLOR_TEXT_DARK);
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea inputArea = buildInputArea(inputPlaceholder);
        JScrollPane inputScroll = buildScroll(inputArea, 160);

        JLabel responseLabel = new JLabel("Response:");
        responseLabel.setFont(FONT_LABEL);
        responseLabel.setForeground(COLOR_TEXT_DARK);
        responseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea responseArea = buildResponseArea();
        JScrollPane responseScroll = buildScroll(responseArea, 220);

        center.add(inputLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(inputScroll);
        center.add(Box.createVerticalStrut(18));
        center.add(responseLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(responseScroll);

        outer.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton backBtn = buildActionButton("← Back", false);
        backBtn.addActionListener(e -> showCard(CARD_HOME));

        JButton clearBtn = buildActionButton("Clear", false);
        clearBtn.addActionListener(e -> resetSection(inputArea, responseArea, inputPlaceholder));

        JButton submitBtn = buildActionButton("Submit  →", true);
        submitBtn.addActionListener(e -> handleSubmit(inputArea, responseArea, inputPlaceholder, submitHandler));

        footer.add(backBtn);
        footer.add(clearBtn);
        footer.add(submitBtn);

        outer.add(footer, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildSectionHeader(String title, String description) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        titleLabel.setForeground(COLOR_PRIMARY);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(FONT_SUBTITLE);
        descLabel.setForeground(new Color(90, 100, 115));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 210, 225));

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.add(titleLabel);
        headerText.add(Box.createVerticalStrut(6));
        headerText.add(descLabel);
        headerText.add(Box.createVerticalStrut(14));
        headerText.add(sep);

        header.add(headerText, BorderLayout.CENTER);
        return header;
    }

    private JTextArea buildInputArea(String inputPlaceholder) {
        JTextArea inputArea = new JTextArea(6, 60);
        inputArea.setFont(FONT_TEXT);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        inputArea.setText(inputPlaceholder);
        inputArea.setForeground(Color.GRAY);

        inputArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (inputArea.getText().equals(inputPlaceholder)) {
                    inputArea.setText("");
                    inputArea.setForeground(COLOR_TEXT_DARK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (inputArea.getText().isBlank()) {
                    inputArea.setText(inputPlaceholder);
                    inputArea.setForeground(Color.GRAY);
                }
            }
        });
        return inputArea;
    }

    private JTextArea buildResponseArea() {
        JTextArea responseArea = new JTextArea(8, 60);
        responseArea.setFont(FONT_TEXT);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setBackground(new Color(248, 250, 253));
        responseArea.setForeground(new Color(50, 60, 80));
        responseArea.setText(DEFAULT_RESPONSE_TEXT);
        responseArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return responseArea;
    }

    private JScrollPane buildScroll(JTextArea area, int maxHeight) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        return scroll;
    }

    private void handleSubmit(JTextArea inputArea, JTextArea responseArea, String inputPlaceholder,
                              java.util.function.Function<String, String> submitHandler) {
        String userInput = inputArea.getText().trim();
        if (userInput.isEmpty() || userInput.equals(inputPlaceholder)) {
            responseArea.setText(EMPTY_REQUEST_WARNING);
            return;
        }
        responseArea.setText(submitHandler.apply(userInput));
    }

    private void resetSection(JTextArea inputArea, JTextArea responseArea, String placeholder) {
        inputArea.setText(placeholder);
        inputArea.setForeground(Color.GRAY);
        responseArea.setText(DEFAULT_RESPONSE_TEXT);
    }

    private JButton buildActionButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI_13);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));

        if (isPrimary) {
            btn.setBackground(COLOR_PRIMARY);
            btn.setForeground(COLOR_TEXT_LIGHT);
            btn.setBorderPainted(false);
            addHoverColor(btn, COLOR_PRIMARY, COLOR_BTN_HOVER);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(COLOR_PRIMARY);
            btn.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));
            addHoverColor(btn, Color.WHITE, new Color(235, 241, 250));
        }
        return btn;
    }

    private String submitCurriculumAdvising(String input) { return backend.submitCurriculumAdvising(input); }
    private String submitScheduling(String input)         { return backend.submitScheduling(input); }
    private String submitBooking(String input)            { return backend.submitBooking(input); }
    private String submitAdministration(String input)     { return backend.submitAdministration(input); }
    private String submitReport(String input)             { return backend.submitReport(input); }
}