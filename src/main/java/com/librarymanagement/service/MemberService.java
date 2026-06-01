package com.librarymanagement.service;

import com.librarymanagement.entity.Member;
import com.librarymanagement.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;
    private final StorageService storageService;

    public MemberService(MemberRepository memberRepository, StorageService storageService) {
        this.memberRepository = memberRepository;
        this.storageService = storageService;
    }

    public boolean addMember(Member member) {
        if (memberRepository.existsById(member.getId())) return false;
        memberRepository.save(member);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(String id) {
        return memberRepository.findById(id);
    }

    public boolean updateMember(String id, String name, String email, String phone) {
        Optional<Member> found = memberRepository.findById(id);
        if (found.isEmpty()) return false;
        Member member = found.get();
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        memberRepository.save(member);
        storageService.persistCurrentIfNeeded();
        return true;
    }

    public boolean deleteMember(String id) {
        if (!memberRepository.existsById(id)) return false;
        memberRepository.deleteById(id);
        storageService.persistCurrentIfNeeded();
        return true;
    }
}