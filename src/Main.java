import model.Book;
import model.Member;
import model.Rental;
import service.BookNotAvailableException;
import service.BookNotFoundException;
import service.DuplicateBookException;
import service.DuplicateIdException;
import service.InvalidInputException;
import service.InvalidPasswordException;
import service.LibraryService;
import service.LoginFailedException;
import service.MemberService;
import service.RentalNotFoundException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in, consoleInputCharset());
    private static final MemberService memberService = new MemberService();
    private static final LibraryService libraryService = new LibraryService();
    private static boolean running = true; // 시작 메뉴에서 "종료"를 고르면 false

    public static void main(String[] args) {
        System.out.println("==== 도서 대여 관리 프로그램 ====");

        while (running) {
            Member loggedInMember = startMenu();
            if (loggedInMember == null) {
                continue; // 로그인 실패, 회원가입, 잘못된 입력, 종료 -> 반복 조건을 다시 확인
            }
            if (loggedInMember.isAdmin()) {
                adminMenu();
            } else {
                userMenu(loggedInMember);
            }
        }

        scanner.close();
        System.out.println("프로그램을 종료합니다.");
    }

    // JDK 18부터 기본 문자셋이 UTF-8로 바뀌었지만, 한국어 Windows 터미널은 여전히 MS949로 키보드 입력을 보낸다.
    // 그래서 입력은 운영체제 기본 인코딩(native.encoding)으로 읽는다. WSL·Mac에서는 이 값이 UTF-8이다.
    // native.encoding이 없는 JDK 17 미만은 기본 문자셋이 곧 운영체제 인코딩이므로 그대로 쓴다.
    private static Charset consoleInputCharset() {
        String nativeEncoding = System.getProperty("native.encoding");
        if (nativeEncoding == null) {
            return Charset.defaultCharset();
        }
        try {
            return Charset.forName(nativeEncoding);
        } catch (IllegalArgumentException e) {
            return Charset.defaultCharset();
        }
    }

    // 로그인 / 회원가입 / 종료를 선택하는 시작 메뉴
    private static Member startMenu() {
        System.out.println();
        System.out.println("1. 로그인");
        System.out.println("2. 회원가입");
        System.out.println("3. 종료");
        System.out.print(">> ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                return login();
            case "2":
                signUp();
                return null;
            case "3":
                running = false;
                return null;
            default:
                System.out.println("잘못된 입력입니다.");
                return null;
        }
    }

    private static Member login() {
        System.out.print("id: ");
        String id = scanner.nextLine().trim();
        System.out.print("password: ");
        String password = scanner.nextLine().trim();

        try {
            Member member = memberService.login(id, password);
            System.out.println(id + "님 환영합니다.");
            return member;
        } catch (LoginFailedException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static void signUp() {
        System.out.print("사용할 id (영문, 숫자): ");
        String id = scanner.nextLine().trim();
        System.out.print("password (8자 이상): ");
        String password = scanner.nextLine().trim();

        try {
            memberService.signUp(id, password);
            System.out.println("회원가입이 완료되었습니다. 로그인해주세요.");
        } catch (InvalidInputException | DuplicateIdException | InvalidPasswordException e) {
            System.out.println(e.getMessage());
        }
    }

    // 관리자 메뉴: 도서 등록 / 목록 / 검색 / 수정 / 삭제 / 전체 대여 현황 / 전체 대여 기록 / 회원 현황
    private static void adminMenu() {
        while (true) {
            System.out.println();
            System.out.println("-- 관리자 메뉴 --");
            System.out.println("1. 도서 등록");
            System.out.println("2. 도서 목록 보기");
            System.out.println("3. 도서 검색");
            System.out.println("4. 도서 정보 수정");
            System.out.println("5. 도서 삭제");
            System.out.println("6. 전체 대여 현황");
            System.out.println("7. 전체 대여 기록");
            System.out.println("8. 회원 현황");
            System.out.println("9. 로그아웃");
            System.out.print(">> ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    registerBook();
                    break;
                case "2":
                    printBooks(libraryService.getAllBooks(), "등록된 도서가 없습니다.", true);
                    break;
                case "3":
                    searchBook(true);
                    break;
                case "4":
                    updateBook();
                    break;
                case "5":
                    deleteBook();
                    break;
                case "6":
                    printRentals(libraryService.getActiveRentals(), true, "현재 대여 중인 도서가 없습니다.");
                    break;
                case "7":
                    printRentals(libraryService.getAllRentals(), true, "대여 기록이 없습니다.");
                    break;
                case "8":
                    printMembers();
                    break;
                case "9":
                    System.out.println("로그아웃되었습니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // 일반 회원 메뉴: 목록 / 검색 / 대여 / 반납 / 내 대여 현황 / 내 대여 기록
    private static void userMenu(Member member) {
        while (true) {
            System.out.println();
            System.out.println("-- 회원 메뉴 --");
            System.out.println("1. 도서 목록 보기");
            System.out.println("2. 도서 검색");
            System.out.println("3. 도서 대여");
            System.out.println("4. 도서 반납");
            System.out.println("5. 내 대여 현황");
            System.out.println("6. 내 대여 기록");
            System.out.println("7. 로그아웃");
            System.out.print(">> ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    printBooks(libraryService.getAllBooks(), "등록된 도서가 없습니다.", false);
                    break;
                case "2":
                    searchBook(false);
                    break;
                case "3":
                    borrowBook(member);
                    break;
                case "4":
                    returnBook(member);
                    break;
                case "5":
                    printRentals(libraryService.getActiveRentalsByMember(member.getId()), false,
                            "현재 대여 중인 도서가 없습니다.");
                    break;
                case "6":
                    printRentals(libraryService.getRentalsByMember(member.getId()), false, "대여 기록이 없습니다.");
                    break;
                case "7":
                    System.out.println("로그아웃되었습니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private static void registerBook() {
        System.out.print("제목: ");
        String title = scanner.nextLine().trim();
        System.out.print("저자: ");
        String author = scanner.nextLine().trim();

        try {
            Book book = libraryService.registerBook(title, author);
            System.out.println("등록되었습니다. (도서 id: " + book.getId() + ")");
        } catch (InvalidInputException | DuplicateBookException e) {
            System.out.println(e.getMessage());
        }
    }

    // 빈칸으로 입력한 값은 기존 값을 그대로 유지한다
    private static void updateBook() {
        String bookId = readBookId("수정할 도서 id: ");
        if (bookId == null) {
            return;
        }

        try {
            Book book = libraryService.getBook(bookId);
            System.out.println("현재 제목: " + book.getTitle());
            System.out.println("현재 저자: " + book.getAuthor());
            System.out.print("새 제목 (그대로 두려면 Enter): ");
            String title = scanner.nextLine().trim();
            System.out.print("새 저자 (그대로 두려면 Enter): ");
            String author = scanner.nextLine().trim();

            if (title.isEmpty()) {
                title = book.getTitle();
            }
            if (author.isEmpty()) {
                author = book.getAuthor();
            }

            libraryService.updateBook(bookId, title, author);
            System.out.println("수정되었습니다.");
        } catch (BookNotFoundException | InvalidInputException | DuplicateBookException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void deleteBook() {
        String bookId = readBookId("삭제할 도서 id: ");
        if (bookId == null) {
            return;
        }

        try {
            libraryService.deleteBook(bookId);
            System.out.println("삭제되었습니다.");
        } catch (BookNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (BookNotAvailableException e) {
            System.out.println("대여 중인 도서는 삭제할 수 없습니다.");
        }
    }

    private static void searchBook(boolean showRenter) {
        System.out.print("검색할 제목(일부): ");
        String keyword = scanner.nextLine().trim();
        printBooks(libraryService.searchByTitle(keyword), "검색 결과가 없습니다.", showRenter);
    }

    private static void borrowBook(Member member) {
        String bookId = readBookId("대여할 도서 id: ");
        if (bookId == null) {
            return;
        }

        try {
            libraryService.borrowBook(bookId, member.getId());
            System.out.println("대여가 완료되었습니다.");
        } catch (BookNotFoundException | BookNotAvailableException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void returnBook(Member member) {
        String bookId = readBookId("반납할 도서 id: ");
        if (bookId == null) {
            return;
        }

        try {
            libraryService.returnBook(bookId, member.getId());
            System.out.println("반납이 완료되었습니다.");
        } catch (BookNotFoundException | RentalNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    // 도서 id를 입력받는다. 빈칸이면 안내 후 null 반환
    private static String readBookId(String prompt) {
        System.out.print(prompt);
        String bookId = scanner.nextLine().trim();
        if (bookId.isEmpty()) {
            System.out.println("도서 id를 입력해주세요.");
            return null;
        }
        return bookId;
    }

    // showRenter가 true면(관리자) 대여 중인 도서를 빌린 회원 id도 보여준다.
    // 제목에 어떤 문자가 있어도 저자와 구분되도록 저자 앞에 "저자:"를 붙인다.
    private static void printBooks(List<Book> books, String emptyMessage, boolean showRenter) {
        if (books.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Book book : books) {
            String status = "대여가능";
            if (book.isRented()) {
                status = "대여중";
                if (showRenter) {
                    String renterId = libraryService.getRenterId(book.getId());
                    status += ": " + (renterId == null ? "알 수 없음" : renterId);
                }
            }
            System.out.printf("[%s] %s / 저자: %s (%s)%n", book.getId(), book.getTitle(), book.getAuthor(), status);
        }
    }

    // 대여 현황/기록 공용 출력. showMemberId가 true면 회원 id도 보여준다 (관리자용)
    private static void printRentals(List<Rental> rentals, boolean showMemberId, String emptyMessage) {
        if (rentals.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Rental rental : rentals) {
            String status = rental.isReturned() ? ("반납완료 (" + rental.getReturnDate() + ")") : "대여중";
            String memberPart = showMemberId ? " / 회원 id: " + rental.getMemberId() : "";
            System.out.printf("[%s] %s%s / 대여일: %s / %s%n",
                    rental.getBookId(), titleOf(rental), memberPart, rental.getBorrowDate(), status);
        }
    }

    // 회원마다 현재 대여 중인 도서와 누적 대여 횟수를 보여준다 (비밀번호는 표시하지 않음)
    private static void printMembers() {
        List<Member> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            System.out.println("가입한 회원이 없습니다.");
            return;
        }

        System.out.println("전체 회원 " + members.size() + "명");
        for (Member member : members) {
            List<Rental> activeRentals = libraryService.getActiveRentalsByMember(member.getId());
            int totalRentals = libraryService.getRentalsByMember(member.getId()).size();

            System.out.printf("%s / 대여 중 %d권 / 누적 대여 %d회%n", member.getId(), activeRentals.size(), totalRentals);
            // 제목에 쉼표가 있을 수 있어 한 줄에 한 권씩 출력한다
            for (Rental rental : activeRentals) {
                System.out.printf("    [%s] %s%n", rental.getBookId(), titleOf(rental));
            }
        }
    }

    // 도서가 남아 있으면 현재 제목(수정 내용 반영)을, 삭제됐으면 대여 당시 저장해 둔 제목을 보여준다
    private static String titleOf(Rental rental) {
        try {
            return libraryService.getBook(rental.getBookId()).getTitle();
        } catch (BookNotFoundException e) {
            return rental.getBookTitle() + " (삭제된 도서)";
        }
    }
}
