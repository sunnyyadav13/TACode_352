/**
 * LibraryAfter.java  -  the "AFTER" version for the refactoring demo.
 *
 * Same library rules, same test results as LibraryBefore.java,
 * but each problem is fixed. Every "FIX n" matches "PROBLEM n" in the before file.
 *
 * Run:  java LibraryAfter.java
 */
public class LibraryAfter {

    // FIX 4: An enum replaces the repeated if-else chains and the magic numbers.
    //        Each member type carries its own rules. A new type = one new line here.
    enum MembershipType {
        BASIC(3, 0.25),
        GOLD(10, 0.10),
        STUDENT(5, 0.15);

        private final int borrowLimit;
        private final double dailyLateFee;

        MembershipType(int borrowLimit, double dailyLateFee) {
            this.borrowLimit = borrowLimit;
            this.dailyLateFee = dailyLateFee;
        }

        int getBorrowLimit() {
            return borrowLimit;
        }

        double getDailyLateFee() {
            return dailyLateFee;
        }
    }

    // FIX 1: Contact details always travel together, so they become one small class
    //        (refactoring name: "Introduce Parameter Object").
    static class Contact {
        private final String email;   // optional - can be null
        private final String phone;
        private final String city;

        Contact(String email, String phone, String city) {
            this.email = email;
            this.phone = phone;
            this.city = city;
        }

        String getEmail() {
            return email;
        }

        boolean hasEmail() {
            return email != null;
        }
    }

    static class Member {
        private final String id;
        private final String name;
        private final MembershipType type;
        private final Contact contact;

        Member(String id, String name, MembershipType type, Contact contact) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.contact = contact;
        }

        String getId() {
            return id;
        }

        MembershipType getType() {
            return type;
        }

        Contact getContact() {
            return contact;
        }
    }

    static class Book {
        private final String isbn;
        private final String title;
        private Member borrower;   // null when the book is on the shelf

        Book(String isbn, String title) {
            this.isbn = isbn;
            this.title = title;
        }

        String getIsbn() {
            return isbn;
        }

        boolean isAvailable() {
            return borrower == null;
        }

        boolean isBorrowedBy(Member member) {
            return borrower == member;   // == is right here: we mean "this exact member object"
        }

        void lendTo(Member member) {
            borrower = member;
        }
    }

    private static final double MAX_LATE_FEE = 10.00;

    // Plain arrays + a counter of how many slots are used
    private final Book[] books = new Book[20];
    private int bookCount = 0;
    private final Member[] members = new Member[20];
    private int memberCount = 0;

    public void addBook(String isbn, String title) {
        books[bookCount] = new Book(isbn, title);
        bookCount++;
    }

    // FIX 1 (continued): 4 clear parameters instead of 6 Strings in a row.
    public void addMember(String id, String name, MembershipType type, Contact contact) {
        members[memberCount] = new Member(id, name, type, contact);
        memberCount++;
    }

    // FIX 3: Each search loop is written ONCE, in its own method ("Extract Method").
    //        Every other method just calls findBook() or findMember().
    //        (Like the old code, the last match wins if an id is added twice.)
    private Book findBook(String isbn) {
        Book found = null;
        for (int i = 0; i < bookCount; i++) {
            if(books[i] != null && books[i].isbn != null && books[i].isbn.equals(isbn)) {
                found = books[i];
            }
        }
        return found;
    }

    private Member findMember(String memberId) {
        Member found = null;
        for (int i = 0; i < memberCount; i++) {
            if (members[i] != null && members[i].getId() != null && members[i].getId().equals(memberId)) {
                found = members[i];
            }
        }
        return found;
    }

    // FIX 2: Guard clauses. Each rule is checked once, top to bottom, and returns early.
    //        The "happy path" is the last two lines - no nesting at all.
    public String borrowBook(String isbn, String memberId) {
        Book book = findBook(isbn);
        Member member = findMember(memberId);

        if (book == null) {
            return "NO SUCH BOOK";
        }
        if (member == null) {
            return "NO SUCH MEMBER";
        }
        if (!book.isAvailable()) {
            return "ALREADY BORROWED";
        }
        if (countBorrowed(member) >= member.getType().getBorrowLimit()) {
            return "LIMIT REACHED";
        }

        book.lendTo(member);
        return "OK";
    }

    public int countBorrowed(Member member) {
        int count = 0;
        for (int i = 0; i < bookCount; i++) {
            if (books[i].isBorrowedBy(member)) {
                count++;
            }
        }
        return count;
    }

    public double lateFee(String memberId, int daysLate) {
        Member member = findMember(memberId);                          // FIX 3
        double fee = daysLate * member.getType().getDailyLateFee();     // FIX 4: no if-else chain
        return Math.min(fee, MAX_LATE_FEE);                             // the cap, in one readable line
    }

    // FIX 5: Check for the missing email instead of crashing.
    //        (This is a bug fix, so it changes behavior on purpose: a new test covers it.)
    public String emailDomain(String memberId) {
        Contact contact = findMember(memberId).getContact();
        if (!contact.hasEmail()) {
            return "NO EMAIL";
        }
        String email = contact.getEmail();
        return email.substring(email.indexOf("@") + 1);
    }

    // FIX 6: The type is now an enum. Each enum value exists exactly once in memory,
    //        so == is correct and safe for enums (unlike for Strings).
    public boolean isGoldMember(String memberId) {
        return findMember(memberId).getType() == MembershipType.GOLD;
    }

    // ================================================================
    // TESTS - same expected results as LibraryBefore.java.
    // Only the setup lines changed, because addMember now takes a Contact and an enum.
    // Run:  java LibraryAfter.java
    // ================================================================
    public static void main(String[] args) {
        LibraryAfter lib = new LibraryAfter();
        lib.addMember("M1", "Ana",  MembershipType.BASIC,   new Contact("ana@gmail.com",     "514-555-0101", "Montreal"));
        lib.addMember("M2", "Ravi", MembershipType.GOLD,    new Contact("ravi@concordia.ca", "514-555-0102", "Montreal"));
        lib.addMember("M3", "Li",   MembershipType.STUDENT, new Contact(null,                "514-555-0103", "Laval"));
        for (int i = 1; i <= 5; i++) {
            lib.addBook("B" + i, "Book " + i);
        }

        check("Borrow a free book",        "OK",               lib.borrowBook("B1", "M1"));
        check("Book already borrowed",     "ALREADY BORROWED", lib.borrowBook("B1", "M2"));
        lib.borrowBook("B2", "M1");
        lib.borrowBook("B3", "M1");
        check("Basic member limit is 3",   "LIMIT REACHED",    lib.borrowBook("B4", "M1"));
        check("Unknown book",              "NO SUCH BOOK",     lib.borrowBook("X9", "M1"));
        check("Unknown member",            "NO SUCH MEMBER",   lib.borrowBook("B4", "M9"));

        check("Basic late fee, 4 days",    1.00,  lib.lateFee("M1", 4));
        check("Gold late fee, 4 days",     0.40,  lib.lateFee("M2", 4));
        check("Student late fee, 4 days",  0.60,  lib.lateFee("M3", 4));
        check("Late fee is capped at $10", 10.00, lib.lateFee("M1", 100));

        check("Email domain",              "concordia.ca", lib.emailDomain("M2"));
        check("Gold member is gold",       true,  lib.isGoldMember("M2"));
        check("Basic member is not gold",  false, lib.isGoldMember("M1"));

        // NEW tests for the two bugs that the old tests could not see:
        check("FIX 5: member without email", "NO EMAIL", lib.emailDomain("M3"));
        MembershipType typedByUser = MembershipType.valueOf(new String("GOLD"));   // e.g. text from a form
        lib.addMember("M4", "Kim", typedByUser, new Contact("kim@mail.com", "514-555-0104", "Montreal"));
        check("FIX 6: gold typed by a user", true, lib.isGoldMember("M4"));
    }

    // ---- tiny test helpers (one for each kind of value) ----
    static void check(String testName, String expected, String actual) {
        report(testName, expected.equals(actual), expected, actual);
    }

    static void check(String testName, double expected, double actual) {
        report(testName, Math.abs(expected - actual) < 0.001, expected, actual);
    }

    static void check(String testName, boolean expected, boolean actual) {
        report(testName, expected == actual, expected, actual);
    }

    static void report(String testName, boolean pass, Object expected, Object actual) {
        System.out.println((pass ? "PASS  " : "FAIL  ") + testName
                + (pass ? "" : "   (expected " + expected + ", got " + actual + ")"));
    }
}