package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

@Slf4j
public class SlidingWindowRateLOGLimiterTest {

    @Test
    public void testSlidingWindowRateLimiter() {
        val slidingWindow = new SlidingWindowLOGRateLimiter(2, 5);
        for (int i = 1; i < 32; i++) {
            sleepInSec(0.5);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(slidingWindow.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
