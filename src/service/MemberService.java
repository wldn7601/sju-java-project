package service;

import io.CsvStorage;
import model.Member;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 회원가입 / 로그인을 담당. id로 빠르게 조회할 수 있도록 HashMap으로 관리한다.
public class MemberService {
    private static final String MEMBERS_FILE = "data/members.csv";
    private static final String ADMIN_FILE = "data/admin.csv"; // 참고용 기록 파일. 인증 자체는 아래 상수로 함
    private static final String ADMIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin0000";
    private static final int MIN_PASSWORD_LENGTH = 8;

    private Map<String, Member> membersById = new HashMap<>();

    public MemberService() {
        for (Member member : CsvStorage.readAll(MEMBERS_FILE, Member::fromCsvFields)) {
            // 운영자는 코드 상수로만 인증한다. members.csv에 관리자로 표시된 줄이 있어도 권한을 주지 않는다.
            if (member.isAdmin()) {
                continue;
            }
            membersById.put(member.getId(), member);
        }
        ensureAdminFileExists();
    }

    // 운영자 계정 정보를 담은 파일이 없으면 기록용으로 한 번 만들어 둔다.
    // members.csv를 지워도 운영자 로그인에는 전혀 영향이 없다 (아래 login()에서 상수로 직접 비교).
    private void ensureAdminFileExists() {
        if (!new File(ADMIN_FILE).exists()) {
            CsvStorage.writeAll(ADMIN_FILE, List.of(new Member(ADMIN_ID, ADMIN_PASSWORD, true)));
        }
    }

    // 가입한 일반 회원 목록을 id 순으로 반환. 내부 HashMap은 건드리지 않도록 새 리스트에 담는다.
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>(membersById.values());
        members.sort(Comparator.comparing(Member::getId));
        return members;
    }

    public boolean idExists(String id) {
        return id.equals(ADMIN_ID) || membersById.containsKey(id);
    }

    public void signUp(String id, String password)
            throws InvalidInputException, DuplicateIdException, InvalidPasswordException {
        if (id.isBlank()) {
            throw new InvalidInputException("아이디를 입력해주세요.");
        }
        // 공백이나 기호가 있으면 출력에서 다른 항목과 헷갈릴 수 있어 영문과 숫자만 허용
        if (!id.matches("[A-Za-z0-9]+")) {
            throw new InvalidInputException("아이디는 영문과 숫자만 사용할 수 있습니다.");
        }
        if (idExists(id)) {
            throw new DuplicateIdException(id);
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException();
        }
        membersById.put(id, new Member(id, password, false));
        save();
    }

    public Member login(String id, String password) throws LoginFailedException {
        // 운영자는 파일이 아니라 코드 상수와 직접 비교 -> members.csv를 지워도 항상 로그인 가능
        if (id.equals(ADMIN_ID)) {
            if (password.equals(ADMIN_PASSWORD)) {
                return new Member(ADMIN_ID, ADMIN_PASSWORD, true);
            }
            throw new LoginFailedException();
        }

        Member member = membersById.get(id);
        if (member == null || !member.getPassword().equals(password)) {
            throw new LoginFailedException();
        }
        return member;
    }

    private void save() {
        CsvStorage.writeAll(MEMBERS_FILE, membersById.values());
    }
}
