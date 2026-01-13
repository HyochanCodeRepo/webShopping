package com.example.webshopping.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductType {
    CLOTHES("의류", "S,M,L,XL,XXL"),
    SHOES("신발", "230,240,250,260,270,280,290"),
    BAG("가방", "FREE"),
    CAP("모자", "FREE"),
    ACCESSORY("악세서리", "FREE"),
    ETC("기타", "FREE");

    private final String description;
    private final String defaultSizes;  // 기본 사이즈 옵션

    public String[] getDefaultSizeArray() {
        return defaultSizes.split(",");
    }
}
