package com.example.ratelimiter.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {
    @SneakyThrows
    public static void sleepInSec(double sec) {
        Thread.sleep((long) (sec * 1000));
    }
}
