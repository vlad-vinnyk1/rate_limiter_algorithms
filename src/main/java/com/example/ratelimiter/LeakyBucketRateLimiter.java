package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LeakyBucketRateLimiter {
    private final ArrayBlockingQueue<Integer> queue;
    private final ScheduledExecutorService dummyConsumer;

    public LeakyBucketRateLimiter(int capacity, int pollingInterval) {
        this.queue = new ArrayBlockingQueue<>(capacity);

        this.dummyConsumer = new ScheduledThreadPoolExecutor(1);
        this.dummyConsumer.scheduleAtFixedRate(this::someDummyPollLogic, 0, pollingInterval, TimeUnit.SECONDS);
    }

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        if (queue.remainingCapacity() > 0) {
            queue.add(f.apply(numb));

            return Response.builder()
                    .code(Response.StatusCode.SUCCESS)
                    .value(f.apply(numb))
                    .build();
        } else {
            return Response.builder()
                    .code(Response.StatusCode.ERROR_RATE_EXCEEDED)
                    .value(numb)
                    .build();
        }

    }

    private void someDummyPollLogic() {
        if (!queue.isEmpty()) {
            log.info("Consumed " + queue.poll());
        } else {
            log.info("The queue is empty, waiting polling interval");
        }
    }
}
