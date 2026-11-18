package service;

import io.CsvStorage;
import model.Book;
import model.Rental;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 도서 등록/수정/삭제/검색과 대여/반납을 담당
public class LibraryService {
    private static final String BOOKS_FILE = "data/books.csv";
    private static final String RENTALS_FILE = "data/rentals.csv";

    // {제목, 저자}
    private static final String[][] SAMPLE_BOOKS = {
            {"어린 왕자", "생텍쥐페리"},
            {"데미안", "헤르만 헤세"},
            {"총, 균, 쇠", "재레드 다이아몬드"},
            {"채식주의자", "한강"},
            {"자바의 정석", "남궁성"}
    };

    private List<Book> books;
    private List<Rental> rentals;
    private int nextBookId;

    public LibraryService() {
        boolean booksFileMissing = !new File(BOOKS_FILE).exists();

        books = CsvStorage.readAll(BOOKS_FILE, Book::fromCsvFields);
        rentals = CsvStorage.readAll(RENTALS_FILE, Rental::fromCsvFields);
        nextBookId = findMaxBookId() + 1;

        if (booksFileMissing) {
            addSampleBooks();
        }
    }

    // books.csv가 아예 없을 때(최초 실행, 또는 data 폴더를 비워 초기화한 뒤)만 샘플 도서를 넣는다.
    // 관리자가 도서를 전부 삭제해서 파일이 비어 있는 경우에는 다시 넣지 않는다.
    private void addSampleBooks() {
        for (String[] sample : SAMPLE_BOOKS) {
            addBook(sample[0], sample[1]);
        }
        saveBooks();
        System.out.println("샘플 도서 " + SAMPLE_BOOKS.length + "권을 등록했습니다.");
    }

    public Book registerBook(String title, String author) throws InvalidInputException, DuplicateBookException {
        validateBookInfo(title, author);
        if (isDuplicateBook(title, author, null)) {
            throw new DuplicateBookException(title, author);
        }

        Book book = addBook(title, author);
        saveBooks();
        return book;
    }

    private Book addBook(String title, String author) {
        String id = String.valueOf(nextBookId++);
        Book book = new Book(id, title, author, false);
        books.add(book);
        return book;
    }

    public Book getBook(String bookId) throws BookNotFoundException {
        for (Book book : books) {
            if (book.getId().equals(bookId)) {
                return book;
            }
        }
        throw new BookNotFoundException(bookId);
    }

    public void updateBook(String bookId, String newTitle, String newAuthor)
            throws BookNotFoundException, InvalidInputException, DuplicateBookException {
        Book book = getBook(bookId);
        validateBookInfo(newTitle, newAuthor);
        if (isDuplicateBook(newTitle, newAuthor, bookId)) {
            throw new DuplicateBookException(newTitle, newAuthor);
        }

        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        saveBooks();
    }

    // 대여 중인 도서는 삭제할 수 없다. 대여 기록은 이력으로 남겨둔다.
    public void deleteBook(String bookId) throws BookNotFoundException, BookNotAvailableException {
        Book book = getBook(bookId);
        if (book.isRented()) {
            throw new BookNotAvailableException(bookId);
        }
        books.remove(book);
        saveBooks();
    }

    public List<Book> getAllBooks() {
        return Collections.unmodifiableList(books);
    }

    public List<Book> searchByTitle(String keyword) {
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().contains(keyword)) {
                result.add(book);
            }
        }
        return result;
    }

    public Rental borrowBook(String bookId, String memberId) throws BookNotFoundException, BookNotAvailableException {
        Book book = getBook(bookId);
        if (book.isRented()) {
            throw new BookNotAvailableException(bookId);
        }

        book.setRented(true);
        Rental rental = new Rental(bookId, memberId, LocalDate.now(), null, false);
        rentals.add(rental);
        saveBooks();
        saveRentals();
        return rental;
    }

    public Rental returnBook(String bookId, String memberId) throws BookNotFoundException, RentalNotFoundException {
        Book book = getBook(bookId);
        Rental activeRental = findActiveRental(bookId, memberId);
        if (activeRental == null) {
            throw new RentalNotFoundException(bookId, memberId);
        }

        activeRental.markReturned(LocalDate.now());
        book.setRented(false);
        saveBooks();
        saveRentals();
        return activeRental;
    }

    // 반납한 기록까지 포함한 해당 회원의 전체 대여 기록
    public List<Rental> getRentalsByMember(String memberId) {
        List<Rental> result = new ArrayList<>();
        for (Rental rental : rentals) {
            if (rental.getMemberId().equals(memberId)) {
                result.add(rental);
            }
        }
        return result;
    }

    // 해당 회원이 아직 반납하지 않은 대여만
    public List<Rental> getActiveRentalsByMember(String memberId) {
        List<Rental> result = new ArrayList<>();
        for (Rental rental : rentals) {
            if (rental.getMemberId().equals(memberId) && !rental.isReturned()) {
                result.add(rental);
            }
        }
        return result;
    }

    // 반납한 기록까지 포함한 전체 대여 기록
    public List<Rental> getAllRentals() {
        return Collections.unmodifiableList(rentals);
    }

    // 모든 회원의 아직 반납되지 않은 대여만
    public List<Rental> getActiveRentals() {
        List<Rental> result = new ArrayList<>();
        for (Rental rental : rentals) {
            if (!rental.isReturned()) {
                result.add(rental);
            }
        }
        return result;
    }

    private void validateBookInfo(String title, String author) throws InvalidInputException {
        if (title.isBlank() || author.isBlank()) {
            throw new InvalidInputException("제목과 저자를 모두 입력해주세요.");
        }
    }

    // excludeId: 수정할 때 자기 자신과는 비교하지 않도록 제외할 도서 id (등록 시에는 null)
    private boolean isDuplicateBook(String title, String author, String excludeId) {
        for (Book book : books) {
            if (book.getId().equals(excludeId)) {
                continue;
            }
            if (book.getTitle().equals(title) && book.getAuthor().equals(author)) {
                return true;
            }
        }
        return false;
    }

    private Rental findActiveRental(String bookId, String memberId) {
        for (Rental rental : rentals) {
            if (rental.getBookId().equals(bookId) && rental.getMemberId().equals(memberId) && !rental.isReturned()) {
                return rental;
            }
        }
        return null;
    }

    // 삭제된 도서의 id가 재사용되어 남아 있는 대여 기록과 섞이지 않도록 대여 기록의 도서 id까지 고려한다
    private int findMaxBookId() {
        int maxId = 0;
        for (Book book : books) {
            maxId = Math.max(maxId, parseIdOrZero(book.getId()));
        }
        for (Rental rental : rentals) {
            maxId = Math.max(maxId, parseIdOrZero(rental.getBookId()));
        }
        return maxId;
    }

    private int parseIdOrZero(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void saveBooks() {
        CsvStorage.writeAll(BOOKS_FILE, books);
    }

    private void saveRentals() {
        CsvStorage.writeAll(RENTALS_FILE, rentals);
    }
}
