package model;

import io.CsvConvertible;
import io.CsvStorage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// 대여 기록 한 건을 나타내는 클래스
public class Rental implements CsvConvertible {
    private String bookId;
    private String bookTitle; // 대여 당시의 제목. 도서가 삭제된 뒤에도 무슨 책이었는지 알 수 있도록 저장
    private String memberId;
    private LocalDate borrowDate;
    private LocalDate returnDate; // 아직 반납 안 했으면 null
    private boolean returned;

    public Rental(String bookId, String bookTitle, String memberId,
                  LocalDate borrowDate, LocalDate returnDate, boolean returned) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.returned = returned;
    }

    public String getBookId() {
        return bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned(LocalDate returnDate) {
        this.returned = true;
        this.returnDate = returnDate;
    }

    // CSV 한 줄 형식: bookId,bookTitle,memberId,borrowDate,returnDate(없으면 빈칸),returned여부
    @Override
    public String toCsvLine() {
        String returnDateStr = (returnDate == null) ? "" : returnDate.toString();
        return CsvStorage.toLine(bookId, bookTitle, memberId, borrowDate.toString(), returnDateStr,
                String.valueOf(returned));
    }

    // 형식이 맞지 않으면 IllegalArgumentException 발생
    public static Rental fromCsvFields(String[] fields) {
        CsvStorage.requireFieldCount(fields, 6);
        LocalDate borrowDate = parseDate(fields[3]);
        LocalDate returnDate = fields[4].isEmpty() ? null : parseDate(fields[4]);
        boolean returned = CsvStorage.parseBoolean(fields[5]);
        return new Rental(fields[0], fields[1], fields[2], borrowDate, returnDate, returned);
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다: " + value);
        }
    }
}
