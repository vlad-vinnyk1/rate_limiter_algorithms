package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

@Slf4j
public class FixedWindowCounterRateLimiterTest {
    @Test
    public void testBucketTokenTest() {
        val leaky = new FixedWindowCounterRateLimiter(10, 1);
        for (int i = 1; i < 200; i++) {
            sleepInSec(0.01);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(leaky.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
