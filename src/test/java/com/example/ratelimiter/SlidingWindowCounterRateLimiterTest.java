package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

@Slf4j
public class SlidingWindowCounterRateLimiterTest {
    public static void main(String[] args) {
        val slidingWindow = new SlidingWindowCounterRateLimiter(7, 10);
        for (int i = 1; i < 21; i++) {
            sleepInSec(1);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(slidingWindow.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
