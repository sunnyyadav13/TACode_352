import java.util.ArrayList;
import java.util.List;

/**
 * Code refactoring is restructuring existing code to improve its internal design 
 * without changing what it does. 
 * The inputs, outputs and behavior stay exactly the same; only the structure gets cleaner.
 * 
 * LibraryBefore.java  -  the "BEFORE" version for the refactoring demo.
 *
 * This code WORKS: every test in main() prints PASS.
 * But it is hard to read and hard to change. Each method shows ONE common problem,
 * marked with a "PROBLEM" comment. LibraryAfter.java shows the matching "FIX".
 *
 * Library rules:
 *   Member type | Borrow limit | Late fee per day
 *   BASIC       |      3       |     $0.25
 *   GOLD        |     10       |     $0.10
 *   STUDENT     |      5       |     $0.15
 *   The late fee is never more than $10.00.
 *
 * Run:  java LibraryBefore.java
 */
public class LibraryBefore {

    // ------------------------------------------------------------
    // Simple data classes
    // ------------------------------------------------------------

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

        Member getBorrower() {
            return borrower;
        }

        void setBorrower(Member borrower) {
            this.borrower = borrower;
        }
    }

    private static final class MemberDetails {
        private final String id;
        private final String name;
        private final String type;
        private final String email;
        private final String phone;
        private final String city;

        MemberDetails(String id, String name, String type, String email, String phone, String city) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.email = email;
            this.phone = phone;
            this.city = city;
        }
    }

    private enum MemberType {
        BASIC(3, 0.25),
        GOLD(10, 0.10),
        STUDENT(5, 0.15);

        private final int borrowLimit;
        private final double lateFeePerDay;

        MemberType(int borrowLimit, double lateFeePerDay) {
            this.borrowLimit = borrowLimit;
            this.lateFeePerDay = lateFeePerDay;
        }

        static MemberType from(String type) {
            for (MemberType memberType : values()) {
                if (memberType.name().equals(type)) {
                    return memberType;
                }
            }
            return null;
        }
    }

    static class Member {
        private final String id;
        private final String name;
        private final String type;    // "BASIC", "GOLD" or "STUDENT"
        private final String email;   // optional - can be null
        private final String phone;
        private final String city;

        Member(MemberDetails details) {
            this.id = details.id;
            this.name = details.name;
            this.type = details.type;
            this.email = details.email;
            this.phone = details.phone;
            this.city = details.city;
        }

        String getId() {
            return id;
        }

        String getType() {
            return type;
        }

        String getEmail() {
            return email;
        }
    }

    private static final double MAX_LATE_FEE = 10.00;

    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();

    public void addBook(String isbn, String title) {
        books.add(new Book(isbn, title));
    }

    // PROBLEM 1: Too many parameters.
    // Six Strings in a row - it is very easy to pass them in the wrong order
    // (for example phone and city swapped) and the compiler cannot help you.
    public void addMember(String id, String name, String type, String email, String phone, String city) {
        members.add(new Member(new MemberDetails(id, name, type, email, phone, city)));
    }

    private Book findBook(String isbn) {
        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                return book;
            }
        }
        return null;
    }

    private Member findMember(String memberId) {
        for (Member member : members) {
            if (member.getId().equals(memberId)) {
                return member;
            }
        }
        return null;
    }

    // Shared lookup helpers keep search behavior in one place.
    public String borrowBook(String isbn, String memberId) {
        Book book = findBook(isbn);
        if (book == null) {
            return "NO SUCH BOOK";
        }
        Member member = findMember(memberId);
        if (member == null) {
            return "NO SUCH MEMBER";
        }
        if (book.getBorrower() != null) {
            return "ALREADY BORROWED";
        }
        if (countBorrowed(member) >= borrowLimit(member.getType())) {
            return "LIMIT REACHED";
        }
        book.setBorrower(member);
        return "OK";
    }

    // Counts how many books this member has borrowed.
    public int countBorrowed(Member member) {
        int count = 0;
        for (Book book : books) {
            if (book.getBorrower() == member) {
                count++;
            }
        }
        return count;
    }

    // MemberType centralizes the type-specific borrowing rules.
    public int borrowLimit(String type) {
        MemberType memberType = MemberType.from(type);
        return memberType == null ? 0 : memberType.borrowLimit;
    }

    public double lateFee(String memberId, int daysLate) {
        Member member = findMember(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Unknown member: " + memberId);
        }

        double fee = daysLate * lateFeePerDay(member.getType());
        fee = Math.min(fee, MAX_LATE_FEE);
        return fee;
    }

    // Email is optional, so missing email is reported explicitly.
    public String emailDomain(String memberId) {
        Member member = findMember(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Unknown member: " + memberId);
        }
        if (member.getEmail() == null) {
            throw new IllegalStateException("Member has no email: " + memberId);
        }
        return member.getEmail().substring(member.getEmail().indexOf("@") + 1);
    }

    // Compare member type by value rather than object identity.
    public boolean isGoldMember(String memberId) {
        Member member = findMember(memberId);
        if (member == null) {
            throw new IllegalArgumentException("Unknown member: " + memberId);
        }
        return "GOLD".equals(member.getType());
    }

    private double lateFeePerDay(String type) {
        MemberType memberType = MemberType.from(type);
        return memberType == null ? 0 : memberType.lateFeePerDay;
    }

    // ================================================================
    // TESTS - they describe today's behavior. They must still PASS
    // after refactoring. Run:  java LibraryBefore.java
    // ================================================================
    public static void main(String[] args) {
        LibraryBefore lib = new LibraryBefore();
        lib.addMember("M1", "Ana",  "BASIC",   "ana@gmail.com",     "514-555-0101", "Montreal");
        lib.addMember("M2", "Ravi", "GOLD",    "ravi@concordia.ca", "514-555-0102", "Montreal");
        lib.addMember("M3", "Li",   "STUDENT", null,                "514-555-0103", "Laval");
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

        // Uncomment this line to verify the explicit missing-email failure.
        // System.out.println(lib.emailDomain("M3"));
        lib.addMember("M4", "Kim", new String("GOLD"), "kim@mail.com", "514-555-0104", "Montreal");
        System.out.println(lib.isGoldMember("M4"));
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