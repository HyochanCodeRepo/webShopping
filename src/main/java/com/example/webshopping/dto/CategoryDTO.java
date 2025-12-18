package com.example.webshopping.dto;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;

}
