package service;

// 회원가입 시 비밀번호가 조건(8자 이상)을 만족하지 않을 때 발생
public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("비밀번호는 8자 이상이어야 합니다.");
    }
}
