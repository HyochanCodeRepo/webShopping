package com.example.webshopping.controller;

import com.example.webshopping.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {

    private final CategoryRepository categoryRepository;


    @GetMapping("/")
    public String Main(Model model) {

        model.addAttribute("categories", categoryRepository.findAll());

        return "main";
    }


}
