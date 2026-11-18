package model;

import io.CsvConvertible;
import io.CsvStorage;

// 회원 정보를 나타내는 클래스 (일반 회원 / 관리자 공용)
public class Member implements CsvConvertible {
    private String id;
    private String password;
    private boolean admin;

    public Member(String id, String password, boolean admin) {
        this.id = id;
        this.password = password;
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }

    // CSV 한 줄 형식: id,password,admin여부
    @Override
    public String toCsvLine() {
        return CsvStorage.toLine(id, password, String.valueOf(admin));
    }

    // 형식이 맞지 않으면 IllegalArgumentException 발생
    public static Member fromCsvFields(String[] fields) {
        CsvStorage.requireFieldCount(fields, 3);
        return new Member(fields[0], fields[1], CsvStorage.parseBoolean(fields[2]));
    }
}
