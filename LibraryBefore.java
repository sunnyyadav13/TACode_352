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
        String isbn;
        String title;
        Member borrower;   // null when the book is on the shelf

        Book(String isbn, String title) {
            this.isbn = isbn;
            this.title = title;
        }
    }

    static class Member {
        String id;
        String name;
        String type;    // "BASIC", "GOLD" or "STUDENT"
        String email;   // optional - can be null
        String phone;
        String city;

        Member(String id, String name, String type, String email, String phone, String city) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.email = email;
            this.phone = phone;
            this.city = city;
        }
    }

    // Plain arrays + a counter of how many slots are used
    private Book[] books = new Book[20];
    private int bookCount = 0;
    private Member[] members = new Member[20];
    private int memberCount = 0;

    public void addBook(String isbn, String title) {
        books[bookCount] = new Book(isbn, title);
        bookCount++;
    }

    // PROBLEM 1: Too many parameters.
    // Six Strings in a row - it is very easy to pass them in the wrong order
    // (for example phone and city swapped) and the compiler cannot help you.
    public void addMember(String id, String name, String type, String email, String phone, String city) {
        members[memberCount] = new Member(id, name, type, email, phone, city);
        memberCount++;
    }


//     private Book findBook(String isbn) {
//     for (int i = 0; i < bookCount; i++) {
//         if (books[i].isbn.equals(isbn)) {
//             return books[i];
//         }
//     }
//     return null;
// }

// private Member findMember(String memberId) {
//     for (int i = 0; i < memberCount; i++) {
//         if (members[i].id.equals(memberId)) {
//             return members[i];
//         }
//     }
//     return null;
// }

// if (books[i] != null && books[i].isbn != null && books[i].isbn.equals(isbn))

    // PROBLEM 3: The same search loops are copy-pasted in many methods.
    public String borrowBook(String isbn, String memberId) {
        Book book = null;
        for (int i = 0; i < bookCount; i++) {
            if (books[i].isbn.equals(isbn)) {
                book = books[i];
            }
        }
        Member member = null;
        for (int i = 0; i < memberCount; i++) {
            if (members[i].id.equals(memberId)) {
                member = members[i];
            }
        }

        if (book != null) {
            if (member != null) {
                if (book.borrower == null) {
                    if (countBorrowed(member) < borrowLimit(member.type)) {
                        book.borrower = member;
                        return "OK";
                    } else {
                        return "LIMIT REACHED";
                    }
                } else {
                    return "ALREADY BORROWED";
                }
            } else {
                return "NO SUCH MEMBER";
            }
        } else {
            return "NO SUCH BOOK";
        }
    }

    // Counts how many books this member has borrowed.
    public int countBorrowed(Member member) {
        int count = 0;
        for (int i = 0; i < bookCount; i++) {
            if (books[i].borrower == member) {
                count++;
            }
        }
        return count;
    }

    // PROBLEM 4: Long if-else chain on a String "type code", full of magic numbers.
    //            The same chain is repeated in lateFee(). A new member type = edit both places.
    public int borrowLimit(String type) {
        if (type.equals("BASIC")) {
            return 3;
        } else if (type.equals("GOLD")) {
            return 10;
        } else if (type.equals("STUDENT")) {
            return 5;
        } else {
            return 0;
        }
    }

    public double lateFee(String memberId, int daysLate) {
        Member member = null;
        for (int i = 0; i < memberCount; i++) {        // PROBLEM 3: copy-pasted search loop
            if (members[i].id.equals(memberId)) {
                member = members[i];
            }
        }

        double fee = 0;
        if (member.type.equals("BASIC")) {             // PROBLEM 4: the same if-else chain again
            fee = daysLate * 0.25;
        } else if (member.type.equals("GOLD")) {
            fee = daysLate * 0.10;
        } else if (member.type.equals("STUDENT")) {
            fee = daysLate * 0.15;
        }
        if (fee > 10) {
            fee = 10;
        }
        return fee;
    }

    // PROBLEM 5: A NullPointerException waiting to happen.
    //            Email is optional, but this code assumes every member has one.
    public String emailDomain(String memberId) {
        Member member = null;
        for (int i = 0; i < memberCount; i++) {        // PROBLEM 3: copy-pasted search loop
            if (members[i].id.equals(memberId)) {
                member = members[i];
            }
        }
        return member.email.substring(member.email.indexOf("@") + 1);
    }

    // PROBLEM 6: Comparing Strings with == instead of equals().
    //            == checks "same object in memory", not "same text".
    //            The test passes only by luck, because Java reuses identical string literals.
    public boolean isGoldMember(String memberId) {
        Member member = null;
        for (int i = 0; i < memberCount; i++) {        // PROBLEM 3: copy-pasted search loop
            if (members[i].id.equals(memberId)) {
                member = members[i];
            }
        }
        return member.type == "GOLD";
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

        // uncomment these lines to show the hidden problems.
        // System.out.println(lib.emailDomain("M3"));        // PROBLEM 5 -> NullPointerException (Li has no email)
        lib.addMember("M4", "Kim", new String("GOLD"), "kim@mail.com", "514-555-0104", "Montreal");
        System.out.println(lib.isGoldMember("M4"));       // PROBLEM 6 -> prints false, but Kim IS gold
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
