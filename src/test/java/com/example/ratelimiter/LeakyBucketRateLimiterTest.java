package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

@Slf4j
public class LeakyBucketRateLimiterTest {

    @Test
    public void testBucketTokenTest() {
        val leaky = new LeakyBucketRateLimiter(3, 1);
        for (int i = 1; i < 32; i++) {
            sleepInSec(0.5);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(leaky.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
