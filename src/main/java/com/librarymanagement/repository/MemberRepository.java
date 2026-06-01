package com.librarymanagement.repository;

import com.librarymanagement.entity.Member;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);
    void save(Member member, Connection conn);
    Optional<Member> findById(String id);
    List<Member> findAll();
    PagedResult<Member> findAll(PageRequest pageRequest);
    long count();
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsById(String id, Connection conn);
    void replaceAll(List<Member> members);
    void replaceAll(List<Member> members, Connection conn);
}