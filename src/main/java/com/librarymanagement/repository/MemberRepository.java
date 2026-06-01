package com.librarymanagement.repository;

import com.librarymanagement.entity.Member;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);
    Optional<Member> findById(String id);
    List<Member> findAll();
    void deleteById(String id);
    boolean existsById(String id);
    void replaceAll(List<Member> members);
}