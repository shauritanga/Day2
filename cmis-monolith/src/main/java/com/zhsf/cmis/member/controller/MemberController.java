package com.zhsf.cmis.member.controller;

import com.zhsf.cmis.member.CreateMemberRequest;
import com.zhsf.cmis.member.MemberResponse;
import com.zhsf.cmis.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> listAll() {
        return memberService.findAll();
    }

    @GetMapping("/{memberNumber}")
    public MemberResponse getByMemberNumber(@PathVariable String memberNumber) {
        return memberService.findByMemberNumber(memberNumber);
    }

    @GetMapping("/internal/{id}")
    public MemberResponse getByIdInternal(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@Valid @RequestBody CreateMemberRequest request) {
        return memberService.create(request);
    }
}
