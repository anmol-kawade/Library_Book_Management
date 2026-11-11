package com.library.service;

import com.library.exception.LibraryException;
import com.library.exception.MemberNotFoundException;
import com.library.model.Member;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Service class for managing library members (Students and Faculty).
 * Applies encapsulation, collections, and lambda expressions.
 */
public class MemberService {
    private final Map<String, Member> memberMap; // Member ID -> Member

    public MemberService(Map<String, Member> initialMembers) {
        this.memberMap = new HashMap<>(initialMembers);
    }

    /**
     * Registers a new member in the system.
     */
    public void registerMember(Member member) throws LibraryException {
        if (member == null || member.getId() == null || member.getId().trim().isEmpty()) {
            throw new LibraryException("Member and Member ID cannot be null or empty.");
        }
        String id = member.getId().trim().toUpperCase();
        member.setId(id);
        if (memberMap.containsKey(id)) {
            throw new LibraryException("A member with ID '" + id + "' is already registered.");
        }
        memberMap.put(id, member);
    }

    /**
     * Retrieves member by ID or throws MemberNotFoundException.
     */
    public Member getMemberById(String id) throws MemberNotFoundException {
        if (id == null) {
            throw new MemberNotFoundException("Member ID cannot be null.");
        }
        Member member = memberMap.get(id.trim().toUpperCase());
        if (member == null) {
            throw new MemberNotFoundException("Member with ID '" + id + "' was not found.");
        }
        return member;
    }

    /**
     * Checks if member exists.
     */
    public boolean exists(String id) {
        return id != null && memberMap.containsKey(id.trim().toUpperCase());
    }

    /**
     * Returns all registered members sorted by ID.
     */
    public List<Member> getAllMembers() {
        return memberMap.values().stream()
                .sorted(Comparator.comparing(Member::getId))
                .collect(Collectors.toList());
    }

    /**
     * Filters members using functional Predicate interface and lambdas.
     */
    public List<Member> filterMembers(Predicate<Member> predicate) {
        return memberMap.values().stream()
                .filter(predicate)
                .sorted(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Searches members by name.
     */
    public List<Member> searchByName(String nameQuery) {
        String query = nameQuery.toLowerCase();
        return filterMembers(m -> m.getName().toLowerCase().contains(query));
    }

    /**
     * Removes a member if they have zero active borrowed books.
     */
    public void removeMember(String id) throws LibraryException {
        Member member = getMemberById(id);
        if (member.getCurrentBorrowedCount() > 0) {
            throw new LibraryException("Cannot remove member '" + member.getName() +
                    "' because they have " + member.getCurrentBorrowedCount() + " active borrowed book(s).");
        }
        memberMap.remove(member.getId());
    }

    /**
     * Generates the next sequential Member ID based on member type (S101+ for student, F101+ for faculty).
     */
    public String generateNextMemberId(String type) {
        String prefix = "FACULTY".equalsIgnoreCase(type) ? "F" : "S";
        int maxId = 100;
        for (String id : memberMap.keySet()) {
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return prefix + (maxId + 1);
    }

    public Map<String, Member> getMemberMap() {
        return memberMap;
    }
}
