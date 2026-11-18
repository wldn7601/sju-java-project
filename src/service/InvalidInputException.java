package service;

// 아이디, 제목, 저자 등 필수 입력값이 비어 있을 때 발생
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}
