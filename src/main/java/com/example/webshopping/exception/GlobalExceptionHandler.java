package com.example.webshopping.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 전역 예외 처리
 */
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 커스텀 비즈니스 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public String handleCustomException(CustomException e, RedirectAttributes redirectAttributes) {
        log.error("CustomException: {} - {}", e.getErrorCode().getCode(), e.getMessage());
        
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        redirectAttributes.addFlashAttribute("errorCode", e.getErrorCode().getCode());
        
        // Referer로 돌아가기 (이전 페이지)
        return "redirect:/";
    }
    
    /**
     * EntityNotFoundException 처리
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException e, Model model) {
        log.error("EntityNotFoundException: {}", e.getMessage());
        
        model.addAttribute("errorMessage", "요청하신 데이터를 찾을 수 없습니다.");
        model.addAttribute("errorCode", "404");
        
        return "error/404";
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        log.error("IllegalArgumentException: {}", e.getMessage());
        
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        
        return "redirect:/";
    }
    
    /**
     * IllegalStateException 처리
     */
    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException e, RedirectAttributes redirectAttributes) {
        log.error("IllegalStateException: {}", e.getMessage());
        
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        
        return "redirect:/";
    }
    
    /**
     * 모든 예외 처리 (최종 방어선)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected Exception: ", e);
        
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorDetail", e.getMessage());
        
        return "error/500";
    }
}
