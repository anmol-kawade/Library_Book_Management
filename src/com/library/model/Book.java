package com.library.model;

/**
 * Represents a Book in the library.
 * Encapsulates book attributes: id, title, author, category, and availability status.
 */
public class Book {
    private String id;
    private String title;
    private String author;
    private String category;
    private boolean available;

    public Book(String id, String title, String author, String category, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.available = available;
    }

    public Book(String id, String title, String author, String category) {
        this(id, title, author, category, true);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("ID: %-7s | Title: %-30s | Author: %-20s | Category: %-15s | Status: %s",
                id,
                title.length() > 30 ? title.substring(0, 27) + "..." : title,
                author.length() > 20 ? author.substring(0, 17) + "..." : author,
                category,
                available ? "[Available]" : "[Issued]");
    }

    /**
     * Serializes Book to CSV format.
     */
    public String toCsv() {
        return String.join(",",
                id,
                escapeCsv(title),
                escapeCsv(author),
                escapeCsv(category),
                String.valueOf(available)
        );
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
