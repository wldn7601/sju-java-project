package service;

// 회원가입 시 이미 존재하는 id로 가입을 시도할 때 발생
public class DuplicateIdException extends Exception {
    public DuplicateIdException(String id) {
        super("이미 존재하는 id입니다: " + id);
    }
}
