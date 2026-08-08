package ru.skypro.homework.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AdvertisingPrice {

    private final Integer PRICE = 100;

    public Integer getPrice() {
        return PRICE;
    }
}
