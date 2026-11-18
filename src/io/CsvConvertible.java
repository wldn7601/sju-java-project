package io;

// CSV 파일 한 줄로 저장될 수 있는 객체가 구현하는 인터페이스
public interface CsvConvertible {
    String toCsvLine();
}
