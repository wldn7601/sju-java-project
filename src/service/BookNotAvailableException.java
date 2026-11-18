package service;

// 이미 대여 중인 도서를 다시 대여하려 할 때 발생
public class BookNotAvailableException extends Exception {
    public BookNotAvailableException(String bookId) {
        super("도서 id " + bookId + "번은 이미 대여 중인 도서입니다.");
    }
}
