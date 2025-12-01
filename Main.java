import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Book implements Comparable<Book> {
    private Integer bookId;
    private String title;
    private String author;
    private String category;
    private boolean isIssued;

    public Book(Integer bookId, String title, String author, String category, boolean isIssued) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isIssued = isIssued;
    }

    public Integer getBookId() { return bookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isIssued() { return isIssued; }

    public void markAsIssued() { isIssued = true; }
    public void markAsReturned() { isIssued = false; }

    public void displayBookDetails() {
        System.out.printf("ID: %d | Title: %s | Author: %s | Category: %s | Issued: %s%n",
                bookId, title, author, category, isIssued ? "Yes" : "No");
    }

    @Override
    public int compareTo(Book other) {
        return this.title.compareToIgnoreCase(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    public String toCSVLine() {
        return bookId + "," + escape(title) + "," + escape(author) + "," + escape(category) + "," + isIssued;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace(",", "&#44;");
    }

    public static String unescape(String s) {
        if (s == null) return "";
        return s.replace("&#44;", ",");
    }

    public static Book fromCSVLine(String line) {
        String[] parts = line.split(",", 5);
        if (parts.length < 5) return null;
        try {
            Integer id = Integer.parseInt(parts[0]);
            String title = unescape(parts[1]);
            String author = unescape(parts[2]);
            String category = unescape(parts[3]);
            boolean isIssued = Boolean.parseBoolean(parts[4]);
            return new Book(id, title, author, category, isIssued);
        } catch (Exception e) {
            return null;
        }
    }
}

class Member {
    private Integer memberId;
    private String name;
    private String email;
    private List<Integer> issuedBooks;

    public Member(Integer memberId, String name, String email, List<Integer> issuedBooks) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.issuedBooks = issuedBooks == null ? new ArrayList<>() : issuedBooks;
    }

    public Integer getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public List<Integer> getIssuedBooks() { return issuedBooks; }

    public void displayMemberDetails() {
        System.out.printf("ID: %d | Name: %s | Email: %s | IssuedBooks: %s%n",
                memberId, name, email, issuedBooks.toString());
    }

    public void addIssuedBook(int bookId) {
        if (!issuedBooks.contains(bookId)) issuedBooks.add(bookId);
    }

    public void returnIssuedBook(int bookId) {
        issuedBooks.remove(Integer.valueOf(bookId));
    }

    public String toCSVLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(memberId).append(",")
          .append(escape(name)).append(",")
          .append(escape(email)).append(",");
        if (issuedBooks != null && !issuedBooks.isEmpty()) {
            for (int i = 0; i < issuedBooks.size(); i++) {
                if (i > 0) sb.append("|");
                sb.append(issuedBooks.get(i));
            }
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(",", "&#44;");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("&#44;", ",");
    }

    public static Member fromCSVLine(String line) {
        String[] parts = line.split(",", 4);
        if (parts.length < 3) return null;
        try {
            Integer id = Integer.parseInt(parts[0]);
            String name = unescape(parts[1]);
            String email = unescape(parts[2]);
            List<Integer> issued = new ArrayList<>();
            if (parts.length >= 4 && parts[3].trim().length() > 0) {
                String[] ids = parts[3].split("\\|");
                for (String s : ids) {
                    if (!s.isBlank()) issued.add(Integer.parseInt(s));
                }
            }
            return new Member(id, name, email, issued);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(memberId, member.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }
}

class LibraryManager {
    private Map<Integer, Book> books = new HashMap<>();
    private Map<Integer, Member> members = new HashMap<>();
    private Set<String> categories = new HashSet<>();
    private final Path booksFile = Paths.get("books.txt");
    private final Path membersFile = Paths.get("members.txt");

    public LibraryManager() {
        try {
            loadFromFile();
        } catch (Exception e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    private int nextBookId() {
        return books.keySet().stream().max(Integer::compareTo).orElse(100) + 1;
    }

    private int nextMemberId() {
        return members.keySet().stream().max(Integer::compareTo).orElse(200) + 1;
    }

    public Book addBook(String title, String author, String category) throws IOException {
        int id = nextBookId();
        Book b = new Book(id, title, author, category, false);
        books.put(id, b);
        categories.add(category);
        saveBooksToFile();
        return b;
    }

    public Member addMember(String name, String email) throws IOException {
        if (!isValidEmail(email)) throw new IllegalArgumentException("Invalid email format.");
        int id = nextMemberId();
        Member m = new Member(id, name, email, new ArrayList<>());
        members.put(id, m);
        saveMembersToFile();
        return m;
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String regex = "^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    public String issueBook(int bookId, int memberId) throws IOException {
        Book book = books.get(bookId);
        Member member = members.get(memberId);
        if (book == null) return "Book not found.";
        if (member == null) return "Member not found.";
        if (book.isIssued()) return "Book is already issued.";
        book.markAsIssued();
        member.addIssuedBook(bookId);
        saveBooksToFile();
        saveMembersToFile();
        return "Book issued successfully.";
    }

    public String returnBook(int bookId, int memberId) throws IOException {
        Book book = books.get(bookId);
        Member member = members.get(memberId);
        if (book == null) return "Book not found.";
        if (member == null) return "Member not found.";
        if (!book.isIssued()) return "Book is not marked as issued.";
        book.markAsReturned();
        member.returnIssuedBook(bookId);
        saveBooksToFile();
        saveMembersToFile();
        return "Book returned successfully.";
    }

    public List<Book> searchBooksByTitle(String q) {
        String lower = q.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByAuthor(String q) {
        String lower = q.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> searchBooksByCategory(String q) {
        String lower = q.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getCategory().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Book> sortBooksByTitle() {
        List<Book> list = new ArrayList<>(books.values());
        Collections.sort(list);
        return list;
    }

    public List<Book> sortBooksByAuthor() {
        List<Book> list = new ArrayList<>(books.values());
        list.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public List<Book> sortBooksByCategory() {
        List<Book> list = new ArrayList<>(books.values());
        list.sort(Comparator.comparing(Book::getCategory, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public void loadFromFile() throws IOException {
        if (!Files.exists(booksFile)) Files.createFile(booksFile);
        if (!Files.exists(membersFile)) Files.createFile(membersFile);

        try (BufferedReader br = Files.newBufferedReader(booksFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Book b = Book.fromCSVLine(line);
                if (b != null) {
                    books.put(b.getBookId(), b);
                    categories.add(b.getCategory());
                }
            }
        }

        try (BufferedReader br = Files.newBufferedReader(membersFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Member m = Member.fromCSVLine(line);
                if (m != null) members.put(m.getMemberId(), m);
            }
        }
    }

    public void saveBooksToFile() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(booksFile)) {
            for (Book b : books.values()) {
                bw.write(b.toCSVLine());
                bw.newLine();
            }
        }
    }

    public void saveMembersToFile() throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(membersFile)) {
            for (Member m : members.values()) {
                bw.write(m.toCSVLine());
                bw.newLine();
            }
        }
    }

    public void saveToFile() {
        try {
            saveBooksToFile();
            saveMembersToFile();
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public Optional<Book> getBookById(int id) { return Optional.ofNullable(books.get(id)); }
    public Optional<Member> getMemberById(int id) { return Optional.ofNullable(members.get(id)); }

    public void printAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        books.values().forEach(Book::displayBookDetails);
    }

    public void printAllMembers() {
        if (members.isEmpty()) {
            System.out.println("No members available.");
            return;
        }
        members.values().forEach(Member::displayMemberDetails);
    }

    public Set<String> getCategories() { return categories; }
}

public class Main {
    private static LibraryManager lm = new LibraryManager();

    public static void main(String[] args) {
        System.out.println("Welcome to City Library Digital Management System CLI");
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addBookUI(sc);
                    case "2" -> addMemberUI(sc);
                    case "3" -> issueBookUI(sc);
                    case "4" -> returnBookUI(sc);
                    case "5" -> searchBooksUI(sc);
                    case "6" -> sortBooksUI(sc);
                    case "7" -> {
                        System.out.println("Saving and exiting...");
                        lm.saveToFile();
                        sc.close();
                        return;
                    }
                    case "8" -> {
                        lm.printAllBooks();
                    }
                    case "9" -> {
                        lm.printAllMembers();
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("1. Add Book");
        System.out.println("2. Add Member");
        System.out.println("3. Issue Book");
        System.out.println("4. Return Book");
        System.out.println("5. Search Books");
        System.out.println("6. Sort Books");
        System.out.println("7. Exit");
        System.out.println("8. Debug: List all Books");
        System.out.println("9. Debug: List all Members");
        System.out.print("Enter your choice: ");
    }

    private static void addBookUI(Scanner sc) throws IOException {
        System.out.print("Enter Book Title: ");
        String title = sc.nextLine().trim();
        System.out.print("Enter Author: ");
        String author = sc.nextLine().trim();
        System.out.print("Enter Category: ");
        String category = sc.nextLine().trim();
        Book b = lm.addBook(title, author, category);
        System.out.println("Book added successfully with ID: " + b.getBookId());
    }

    private static void addMemberUI(Scanner sc) throws IOException {
        System.out.print("Enter Member Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Enter Member Email: ");
        String email = sc.nextLine().trim();
        Member m = lm.addMember(name, email);
        System.out.println("Member added successfully with ID: " + m.getMemberId());
    }

    private static void issueBookUI(Scanner sc) throws IOException {
        System.out.print("Enter Book ID to issue: ");
        int bookId = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Enter Member ID: ");
        int memberId = Integer.parseInt(sc.nextLine().trim());
        System.out.println(lm.issueBook(bookId, memberId));
    }

    private static void returnBookUI(Scanner sc) throws IOException {
        System.out.print("Enter Book ID to return: ");
        int bookId = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Enter Member ID: ");
        int memberId = Integer.parseInt(sc.nextLine().trim());
        System.out.println(lm.returnBook(bookId, memberId));
    }

    private static void searchBooksUI(Scanner sc) {
        System.out.println("Search by: 1) Title 2) Author 3) Category");
        String opt = sc.nextLine().trim();
        System.out.print("Enter search query: ");
        String q = sc.nextLine().trim();
        List<Book> results;
        switch (opt) {
            case "1" -> results = lm.searchBooksByTitle(q);
            case "2" -> results = lm.searchBooksByAuthor(q);
            case "3" -> results = lm.searchBooksByCategory(q);
            default -> {
                System.out.println("Invalid option.");
                return;
            }
        }
        if (results.isEmpty()) System.out.println("No books match your search.");
        else results.forEach(Book::displayBookDetails);
    }

    private static void sortBooksUI(Scanner sc) {
        System.out.println("Sort by: 1) Title 2) Author 3) Category");
        String opt = sc.nextLine().trim();
        List<Book> sorted;
        switch (opt) {
            case "1" -> sorted = lm.sortBooksByTitle();
            case "2" -> sorted = lm.sortBooksByAuthor();
            case "3" -> sorted = lm.sortBooksByCategory();
            default -> {
                System.out.println("Invalid option.");
                return;
            }
        }
        sorted.forEach(Book::displayBookDetails);
    }
}
