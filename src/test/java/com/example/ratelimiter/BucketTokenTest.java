package com.example.ratelimiter;

import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

import static com.example.ratelimiter.utils.TestUtils.sleepInSec;

@Slf4j
public class BucketTokenTest {

    @Test
    public void testBucketTokenTest() {
        val token = new BucketTokenRateLimiter(3, 1, 1);
        for (int i = 0; i < 33; i++) {
            sleepInSec(0.25);
            int finalI = i;
            Executors.newSingleThreadExecutor().submit(
                    () -> log.info(token.limitFunc(finalI, Function1.identity()).toString())
            );
        }
    }
}
