package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

// CSV 파일 읽기/쓰기와 한 줄 파싱을 담당. 파일 입출력 예외와 손상된 데이터 처리를 한 곳에 모은다.
public class CsvStorage {

    // 파일의 각 줄을 parser로 객체로 바꿔 리스트로 반환. 형식이 깨진 줄은 경고만 출력하고 건너뛴다.
    public static <T> List<T> readAll(String path, Function<String[], T> parser) {
        List<T> items = new ArrayList<>();
        for (String line : readLines(path)) {
            try {
                items.add(parser.apply(parseLine(line)));
            } catch (IllegalArgumentException e) {
                System.out.println("[경고] " + path + "의 손상된 줄을 건너뜁니다: " + line + " (" + e.getMessage() + ")");
            }
        }
        return items;
    }

    public static void writeAll(String path, Collection<? extends CsvConvertible> items) {
        List<String> lines = new ArrayList<>();
        for (CsvConvertible item : items) {
            lines.add(item.toCsvLine());
        }
        writeLines(path, lines);
    }

    // 여러 값을 CSV 한 줄로 합친다. 쉼표나 따옴표가 들어간 값은 따옴표로 감싸서 저장한다.
    public static String toLine(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    // CSV 한 줄을 값 배열로 나눈다. 따옴표로 감싼 값 안의 쉼표는 구분자로 보지 않는다.
    public static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"'); // "" 는 따옴표 문자 하나
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException("따옴표가 닫히지 않았습니다");
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    public static void requireFieldCount(String[] fields, int expected) {
        if (fields.length != expected) {
            throw new IllegalArgumentException("값이 " + expected + "개여야 하는데 " + fields.length + "개입니다");
        }
    }

    // Boolean.parseBoolean은 이상한 값도 false로 넘겨버리므로, true/false만 허용한다.
    public static boolean parseBoolean(String value) {
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("true/false가 아닌 값입니다: " + value);
    }

    private static String escape(String field) {
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // 파일이 없으면(최초 실행) 빈 리스트 반환
    private static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("파일을 읽는 중 오류가 발생했습니다: " + path);
        }
        return lines;
    }

    // 상위 폴더가 없으면 자동 생성한 뒤 덮어쓴다
    private static void writeLines(String path, List<String> lines) {
        File file = new File(path);
        File parentDir = file.getParentFile();

        try {
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("파일을 저장하는 중 오류가 발생했습니다: " + path);
        }
    }
}
