package ru.skypro.homework.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdvertisingPrice {

    private final BigDecimal PRICE = BigDecimal.valueOf(1.0);

    public BigDecimal getPrice() {
        return PRICE;
    }
}
