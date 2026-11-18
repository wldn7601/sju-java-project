package model;

import io.CsvConvertible;
import io.CsvStorage;

// 도서 정보를 나타내는 클래스
public class Book implements CsvConvertible {
    private String id;
    private String title;
    private String author;
    private boolean rented;

    public Book(String id, String title, String author, boolean rented) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.rented = rented;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isRented() {
        return rented;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setRented(boolean rented) {
        this.rented = rented;
    }

    // CSV 한 줄 형식: id,title,author,rented여부
    @Override
    public String toCsvLine() {
        return CsvStorage.toLine(id, title, author, String.valueOf(rented));
    }

    // 형식이 맞지 않으면 IllegalArgumentException 발생
    public static Book fromCsvFields(String[] fields) {
        CsvStorage.requireFieldCount(fields, 4);
        return new Book(fields[0], fields[1], fields[2], CsvStorage.parseBoolean(fields[3]));
    }
}
