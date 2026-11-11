package com.library.model;

/**
 * Abstract base class representing a Person in the library system.
 * Demonstrates inheritance and encapsulation.
 */
public abstract class Person {
    private String id;
    private String name;
    private String contact;
    private String email;

    public Person(String id, String name, String contact, String email) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Name: " + name + " | Contact: " + contact + " | Email: " + email;
    }
}
