package ru.skypro.homework.service;

import org.springframework.stereotype.Service;

@Service
public class AdvertisingPrice {

    private static final Integer PRICE = 100;

    public Integer getPrice() {
        return PRICE;
    }
}
