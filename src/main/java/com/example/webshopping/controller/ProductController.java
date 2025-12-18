package com.example.webshopping.controller;

import com.example.webshopping.dto.ProductDTO;
import com.example.webshopping.entity.Category;
import com.example.webshopping.entity.Product;
import com.example.webshopping.repository.CategoryRepository;
import com.example.webshopping.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/register")
    public String register(Model model) {
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("categories", categories);

        return "product/register";
    }

    @PostMapping("/register")
    public String register(ProductDTO productDTO, RedirectAttributes redirectAttributes) {
        log.info("productDTO : {}",productDTO);
        productService.create(productDTO);

        redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록되었습니다!");

        return "redirect:/product/register";
    }


}
