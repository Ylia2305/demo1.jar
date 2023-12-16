package com.example.demo1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class PhoneBook implements Serializable {

    private final List<Contact> contacts;

    public PhoneBook() {
        this.contacts = new ArrayList<>();
    }

    public List<Contact> searchByKeyword(String keyword) {
        List<Contact> result = new ArrayList<>();

        for (Contact contact : contacts) {
            if (contact.getName().equalsIgnoreCase(keyword) ||
                    contact.getLastName().equalsIgnoreCase(keyword) ||
                    contact.getMiddleName().equalsIgnoreCase(keyword) ||
                    contact.getPhoneNumbers().contains(keyword)) {
                result.add(contact);
            }
        }

        return result;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }

    public List<Contact> getAllContacts() {
        return contacts;
    }
}
