package service;

// 해당 회원이 대여 중인 도서 기록을 찾을 수 없을 때(반납 시도 시) 발생
public class RentalNotFoundException extends Exception {
    public RentalNotFoundException(String bookId, String memberId) {
        super("도서 id " + bookId + "번은 " + memberId + "님이 대여 중인 도서가 아닙니다.");
    }
}
