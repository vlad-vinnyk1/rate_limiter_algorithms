package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

// Can see bottleneck of the algo: 6 first are allowed, then expected distribution: 4rps = 3 allow and 1 rejected.
@Slf4j
public class FixedWindowCounterRateLimiterTest {
    @Test
    public void testBucketTokenTest() {
        val leaky = new FixedWindowCounterRateLimiter(3, 1);
        for (int i = 1; i < 33; i++) {
            sleepInSec(0.25);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(leaky.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
