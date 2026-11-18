package service;

// 제목과 저자가 모두 같은 도서를 중복 등록하려 할 때 발생
public class DuplicateBookException extends Exception {
    public DuplicateBookException(String title, String author) {
        super("이미 등록된 도서입니다. (제목: " + title + " / 저자: " + author + ")");
    }
}
