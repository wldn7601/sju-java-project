package service;

// 존재하지 않는 도서 id를 조회/대여/반납하려 할 때 발생
public class BookNotFoundException extends Exception {
    public BookNotFoundException(String bookId) {
        super("존재하지 않는 도서 id입니다: " + bookId);
    }
}
