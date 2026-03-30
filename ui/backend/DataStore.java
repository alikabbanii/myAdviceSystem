package ui.backend;

/**
 * Central data store for the myAdvice backend.
 * All data is static and loaded at class-initialization time.
 * BookingModule mutates AppointmentSlot.isBooked and .bookedBy at runtime.
 */
public class DataStore {

    // -------------------------------------------------------------------------
    // Inner model classes
    // -------------------------------------------------------------------------

    public static class Course {
        public final String code;
        public final String name;
        public final int credits;
        public final int year;
        public final String[] prerequisites;
        public final String description;
        public final String term; // "Fall", "Winter", or "Both"

        public Course(String code, String name, int credits, int year,
                      String[] prerequisites, String description, String term) {
            this.code = code;
            this.name = name;
            this.credits = credits;
            this.year = year;
            this.prerequisites = prerequisites;
            this.description = description;
            this.term = term;
        }
    }

    public static class Section {
        public final String sectionId;
        public final String courseCode;
        public final String instructor;
        public final String days;
        public final String startTime;
        public final String endTime;
        public final String room;
        public final int capacity;
        public final int enrolled;

        public Section(String sectionId, String courseCode, String instructor,
                       String days, String startTime, String endTime,
                       String room, int capacity, int enrolled) {
            this.sectionId = sectionId;
            this.courseCode = courseCode;
            this.instructor = instructor;
            this.days = days;
            this.startTime = startTime;
            this.endTime = endTime;
            this.room = room;
            this.capacity = capacity;
            this.enrolled = enrolled;
        }

        public int seatsAvailable() { return capacity - enrolled; }
        public boolean isFull()     { return enrolled >= capacity; }
    }

    public static class Advisor {
        public final String id;
        public final String name;
        public final String title;
        public final String email;
        public final String specialization;

        public Advisor(String id, String name, String title,
                       String email, String specialization) {
            this.id = id;
            this.name = name;
            this.title = title;
            this.email = email;
            this.specialization = specialization;
        }
    }

    public static class AppointmentSlot {
        public final String slotId;
        public final String advisorId;
        public final String day;
        public final String time;
        public boolean isBooked;
        public String bookedBy;

        public AppointmentSlot(String slotId, String advisorId,
                               String day, String time,
                               boolean isBooked, String bookedBy) {
            this.slotId = slotId;
            this.advisorId = advisorId;
            this.day = day;
            this.time = time;
            this.isBooked = isBooked;
            this.bookedBy = bookedBy;
        }
    }

    public static class Student {
        public final String id;
        public final String name;
        public final int year;
        public final String program;
        public final double gpa;

        public Student(String id, String name, int year, String program, double gpa) {
            this.id = id;
            this.name = name;
            this.year = year;
            this.program = program;
            this.gpa = gpa;
        }
    }

    // -------------------------------------------------------------------------
    // Course catalogue — 28 courses, Year 1–4 UWindsor CS
    // -------------------------------------------------------------------------

    public static final Course[] courses = {
            // Year 1 — Fall
            new Course("CS110",   "Problem Solving and Programming I",          3, 1, new String[]{},
                    "Introduction to programming concepts using Java. Variables, control flow, methods, arrays, and basic OOP.", "Fall"),
            new Course("MATH1720", "Differential Calculus",                     3, 1, new String[]{},
                    "Limits, derivatives, rules of differentiation, applications to optimization and related rates.", "Fall"),
            new Course("MATH1250", "Linear Algebra I",                          3, 1, new String[]{},
                    "Vectors, matrices, systems of linear equations, determinants, eigenvalues.", "Fall"),

            // Year 1 — Winter
            new Course("CS114",   "Problem Solving and Programming II",         3, 1, new String[]{"CS110"},
                    "Recursion, linked lists, stacks, queues, trees, sorting algorithms, and intermediate OOP.", "Winter"),
            new Course("MATH1730", "Integral Calculus",                         3, 1, new String[]{"MATH1720"},
                    "Antiderivatives, definite integrals, techniques of integration, applications.", "Winter"),
            new Course("STAT2910", "Statistics for Sciences",                   3, 1, new String[]{"MATH1720"},
                    "Probability, distributions, hypothesis testing, regression. Applications in computing.", "Winter"),
            new Course("MATH2150", "Multivariate Calculus",                     3, 1, new String[]{"MATH1720","MATH1250"},
                    "Partial derivatives, multiple integrals, vector calculus.", "Winter"),

            // Year 2 — Fall
            new Course("CS252",   "Computer Organization and Architecture I",   3, 2, new String[]{"CS114"},
                    "Binary representation, Boolean algebra, digital logic circuits, CPU design, assembly language.", "Fall"),
            new Course("CS253",   "Data Structures and Algorithms",             3, 2, new String[]{"CS114"},
                    "Lists, stacks, queues, trees, hash tables, graphs; searching and sorting algorithms.", "Fall"),
            new Course("CS260",   "Discrete Mathematics for Computer Scientists",3,2, new String[]{"MATH1250"},
                    "Logic, sets, functions, relations, graph theory, combinatorics, proof techniques.", "Fall"),
            new Course("MATH2750","Differential Equations I",                   3, 2, new String[]{"MATH1730"},
                    "First-order ODEs, linear ODEs, Laplace transforms, applications.", "Fall"),

            // Year 2 — Winter
            new Course("CS302",   "Computer Organization and Architecture II",  3, 2, new String[]{"CS252"},
                    "Memory hierarchy, cache design, pipelines, I/O systems, performance analysis.", "Winter"),
            new Course("CS340",   "Software Engineering",                       3, 2, new String[]{"CS253"},
                    "SDLC, Agile, UML, design patterns, testing strategies, version control, documentation.", "Winter"),
            new Course("CS350",   "Programming Languages",                      3, 2, new String[]{"CS253"},
                    "Language paradigms, grammars, parsing, type systems, functional and logic programming.", "Winter"),
            new Course("CS360",   "Formal Languages and Automata Theory",       3, 2, new String[]{"CS260"},
                    "Finite automata, regular expressions, context-free grammars, pushdown automata, Turing machines.", "Winter"),

            // Year 3 — Fall
            new Course("CS440",   "Operating Systems",                          3, 3, new String[]{"CS302","CS253"},
                    "Processes, threads, scheduling, memory management, file systems, concurrency, synchronization.", "Fall"),
            new Course("CS460",   "Analysis of Algorithms",                     3, 3, new String[]{"CS360","CS253"},
                    "Asymptotic analysis, divide-and-conquer, dynamic programming, greedy algorithms, NP-completeness.", "Fall"),
            new Course("CS470",   "Database Management Systems",                3, 3, new String[]{"CS253"},
                    "Relational model, SQL, normalization, transactions, query optimization, NoSQL overview.", "Fall"),
            new Course("CS465",   "Artificial Intelligence",                    3, 3, new String[]{"CS460"},
                    "Search algorithms, knowledge representation, machine learning intro, neural networks, NLP basics.", "Fall"),

            // Year 3 — Winter
            new Course("CS445",   "Computer Networks",                          3, 3, new String[]{"CS440"},
                    "OSI model, TCP/IP, routing, transport protocols, application layer, network security intro.", "Winter"),
            new Course("CS480",   "Compiler Design",                            3, 3, new String[]{"CS350","CS460"},
                    "Lexical analysis, parsing, semantic analysis, intermediate code generation, optimization.", "Winter"),
            new Course("CS490",   "Software Engineering Practicum",             3, 3, new String[]{"CS340"},
                    "Team-based software project applying Agile/SCRUM: requirements, design, implementation, testing.", "Winter"),
            new Course("CS475",   "Distributed Systems",                        3, 3, new String[]{"CS445"},
                    "Distributed architectures, RPC, consistency models, fault tolerance, MapReduce, microservices.", "Winter"),

            // Year 4 — Fall
            new Course("CS455",   "Machine Learning",                           3, 4, new String[]{"CS465","STAT2910"},
                    "Supervised/unsupervised learning, regression, SVM, decision trees, clustering, neural networks.", "Fall"),
            new Course("CS485",   "Advanced Algorithms",                        3, 4, new String[]{"CS460"},
                    "Randomized algorithms, approximation algorithms, network flows, computational geometry.", "Fall"),
            new Course("CS474",   "Cloud Computing",                            3, 4, new String[]{"CS475"},
                    "Cloud infrastructure, virtualization, containers (Docker/K8s), serverless, SLA, cloud security.", "Fall"),
            new Course("CS499",   "Computer Science Seminar",                   1, 4, new String[]{},
                    "Weekly research seminars by faculty and industry speakers. Required for graduation.", "Fall"),

            // Year 4 — Winter
            new Course("COMP400", "Undergraduate Thesis",                       6, 4, new String[]{},
                    "Independent research project supervised by a faculty member. Culminates in written thesis and oral defense.", "Winter"),
            new Course("CS466",   "Deep Learning",                              3, 4, new String[]{"CS455"},
                    "CNNs, RNNs, transformers, GANs, training techniques, applications in vision and NLP.", "Winter"),
            new Course("CS495",   "Cybersecurity",                              3, 4, new String[]{"CS445","CS470"},
                    "Cryptography, network security, web security, threat modeling, penetration testing basics.", "Winter"),
            new Course("CS498",   "Selected Topics in Computer Science",        3, 4, new String[]{},
                    "Advanced seminar on current research topics. Content varies by term.", "Winter"),
    };

    // -------------------------------------------------------------------------
    // Course sections — 10 active sections for Winter 2026
    // -------------------------------------------------------------------------

    public static final Section[] sections = {
            new Section("SEC001","CS253","Dr. A. Ngom",    "Mon/Wed/Fri","09:00","09:50","Essex Hall 108",       30, 27),
            new Section("SEC002","CS253","Dr. A. Ngom",    "Tue/Thu",    "11:30","12:45","Essex Hall 108",       30, 22),
            new Section("SEC003","CS340","Dr. R. Gras",    "Mon/Wed",    "13:00","14:15","Erie Hall 3103",       35, 31),
            new Section("SEC004","CS440","Dr. Z. Kobti",   "Tue/Thu",    "08:30","09:45","Essex Hall 206",       30, 28),
            new Section("SEC005","CS470","Dr. L. Rueda",   "Mon/Wed/Fri","10:00","10:50","Lambton Tower 3104",   35, 19),
            new Section("SEC006","CS460","Dr. S. Saad",    "Tue/Thu",    "14:30","15:45","Erie Hall 3103",       30, 30),
            new Section("SEC007","CS350","Dr. A. Biniaz",  "Mon/Wed",    "16:00","17:15","Essex Hall 108",       30, 12),
            new Section("SEC008","CS465","Dr. I. Ahmad",   "Tue/Thu",    "11:30","12:45","Lambton Tower 3104",   30, 25),
            new Section("SEC009","CS110","Dr. D. Wu",      "Mon/Wed/Fri","09:00","09:50","Essex Hall 112",       60, 54),
            new Section("SEC010","CS114","Dr. D. Wu",      "Tue/Thu",    "13:00","14:15","Essex Hall 112",       60, 47),
    };

    // -------------------------------------------------------------------------
    // Advisors
    // -------------------------------------------------------------------------

    public static final Advisor[] advisors = {
            new Advisor("ADV001","Dr. Alioune Ngom",   "Associate Professor",  "ngom@uwindsor.ca",   "Machine Learning, Bioinformatics, Pattern Recognition"),
            new Advisor("ADV002","Dr. Robin Gras",     "Professor",            "rgras@uwindsor.ca",  "Software Engineering, AI, Evolutionary Computing"),
            new Advisor("ADV003","Dr. Ziad Kobti",     "Associate Professor",  "zkobti@uwindsor.ca", "Operating Systems, Multi-Agent Systems, Swarm Intelligence"),
            new Advisor("ADV004","Dr. Sherif Saad",    "Assistant Professor",  "ssaad@uwindsor.ca",  "Cybersecurity, Data Mining, Network Security"),
            new Advisor("ADV005","Dr. Jessica Chen",   "Academic Advisor",     "jchen@uwindsor.ca",  "General Academic Advising, Transfer Credits, Program Planning"),
    };

    // -------------------------------------------------------------------------
    // Appointment slots — 5 per advisor (25 total), 8 pre-booked
    // -------------------------------------------------------------------------

    public static final AppointmentSlot[] slots = {
            // ADV001 — Dr. Ngom
            new AppointmentSlot("SLOT001","ADV001","Monday",   "10:00 AM", false, null),
            new AppointmentSlot("SLOT002","ADV001","Monday",   "11:00 AM", true,  "John Smith"),
            new AppointmentSlot("SLOT003","ADV001","Wednesday","10:00 AM", false, null),
            new AppointmentSlot("SLOT004","ADV001","Wednesday","11:00 AM", false, null),
            new AppointmentSlot("SLOT005","ADV001","Friday",   "10:00 AM", true,  "Fatima Al-Hassan"),
            // ADV002 — Dr. Gras
            new AppointmentSlot("SLOT006","ADV002","Tuesday",  " 1:00 PM", false, null),
            new AppointmentSlot("SLOT007","ADV002","Tuesday",  " 2:00 PM", true,  "Wei Zhang"),
            new AppointmentSlot("SLOT008","ADV002","Thursday", " 1:00 PM", false, null),
            new AppointmentSlot("SLOT009","ADV002","Thursday", " 2:00 PM", false, null),
            new AppointmentSlot("SLOT010","ADV002","Friday",   " 1:00 PM", false, null),
            // ADV003 — Dr. Kobti
            new AppointmentSlot("SLOT011","ADV003","Monday",   " 2:00 PM", false, null),
            new AppointmentSlot("SLOT012","ADV003","Wednesday"," 2:00 PM", true,  "Ahmed Hassan"),
            new AppointmentSlot("SLOT013","ADV003","Wednesday"," 3:00 PM", false, null),
            new AppointmentSlot("SLOT014","ADV003","Friday",   " 2:00 PM", false, null),
            new AppointmentSlot("SLOT015","ADV003","Friday",   " 3:00 PM", false, null),
            // ADV004 — Dr. Saad
            new AppointmentSlot("SLOT016","ADV004","Tuesday",  " 9:00 AM", true,  "Priya Patel"),
            new AppointmentSlot("SLOT017","ADV004","Tuesday",  "10:00 AM", false, null),
            new AppointmentSlot("SLOT018","ADV004","Thursday", " 9:00 AM", false, null),
            new AppointmentSlot("SLOT019","ADV004","Thursday", "10:00 AM", false, null),
            new AppointmentSlot("SLOT020","ADV004","Friday",   " 9:00 AM", false, null),
            // ADV005 — Dr. Chen
            new AppointmentSlot("SLOT021","ADV005","Monday",   " 9:00 AM", false, null),
            new AppointmentSlot("SLOT022","ADV005","Monday",   "10:00 AM", true,  "Carlos Rivera"),
            new AppointmentSlot("SLOT023","ADV005","Wednesday"," 9:00 AM", false, null),
            new AppointmentSlot("SLOT024","ADV005","Wednesday","10:00 AM", false, null),
            new AppointmentSlot("SLOT025","ADV005","Friday",   " 9:00 AM", true,  "Emily Thompson"),
    };

    // -------------------------------------------------------------------------
    // Students — 15 registered students
    // -------------------------------------------------------------------------

    public static final Student[] students = {
            new Student("STU001","John Smith",       2,"CS",3.70),
            new Student("STU002","Fatima Al-Hassan", 3,"CS",3.90),
            new Student("STU003","Wei Zhang",        1,"CS",3.20),
            new Student("STU004","Ahmed Hassan",     4,"CS",3.50),
            new Student("STU005","Priya Patel",      2,"CS",3.80),
            new Student("STU006","Carlos Rivera",    3,"CS",2.90),
            new Student("STU007","Emily Thompson",   1,"CS",3.60),
            new Student("STU008","Liam O'Brien",     4,"CS",3.10),
            new Student("STU009","Aisha Diallo",     2,"CS",3.40),
            new Student("STU010","Hiroshi Tanaka",   3,"CS",3.70),
            new Student("STU011","Sofia Petrov",     1,"CS",2.80),
            new Student("STU012","Marcus Johnson",   4,"CS",3.30),
            new Student("STU013","Nadia Kowalski",   2,"CS",3.60),
            new Student("STU014","Omar Farouq",      3,"CS",3.00),
            new Student("STU015","Isabella Rossi",   4,"CS",3.80),
    };

    // -------------------------------------------------------------------------
    // Lookup helpers
    // -------------------------------------------------------------------------

    public static Course findCourse(String code) {
        String upper = code.toUpperCase().trim();
        for (Course c : courses) {
            if (c.code.equalsIgnoreCase(upper)) return c;
        }
        return null;
    }

    public static Advisor findAdvisor(String id) {
        for (Advisor a : advisors) {
            if (a.id.equals(id)) return a;
        }
        return null;
    }

    public static AppointmentSlot findSlot(String slotId) {
        String upper = slotId.toUpperCase().trim();
        for (AppointmentSlot s : slots) {
            if (s.slotId.equalsIgnoreCase(upper)) return s;
        }
        return null;
    }
}