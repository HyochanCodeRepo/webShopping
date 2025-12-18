package com.example.webshopping.controller;

import com.example.webshopping.dto.MembersDTO;
import com.example.webshopping.service.MembersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/members")
public class MembersController {
    private final MembersService membersService;

    @GetMapping("/register")
    public String register() {
        return "members/register";
    }

    @PostMapping("/register")
    public String register(MembersDTO memberDTO) {

        log.info(memberDTO);

        membersService.create(memberDTO);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "members/login";
    }

}
