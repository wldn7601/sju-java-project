package service;

// id가 없거나 비밀번호가 틀렸을 때 발생
public class LoginFailedException extends Exception {
    public LoginFailedException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
