package com.ecomnext.play.futures;

import com.google.common.util.concurrent.Futures;
import org.jooq.lambda.Unchecked;
import org.junit.Test;
import play.libs.F;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class GuavaFuturesTest {

    @Test
    public void should_return_the_value() {
        running(fakeApplication(), () -> {
            F.Promise<String> stringPromise = GuavaFutures.asPromise(
                    Futures.immediateFuture("Whats up!")
            );
            assertEquals("Whats up!", stringPromise.get(1000));
        });
    }

    @Test
    public void should_return_the_value_using_executor() {
        F.Promise<String> stringPromise = GuavaFutures.asPromise(
                Futures.immediateFuture("Whats up!"),
                ForkJoinPool.commonPool()
        );
        assertEquals("Whats up!", stringPromise.get(1000));
    }

    @Test(expected = RuntimeException.class)
    public void should_return_the_error() {
        F.Promise<String> stringPromise = GuavaFutures.asPromise(
                Futures.immediateFailedFuture(new RuntimeException("Oops!"))
        );
        stringPromise.get(1000);
    }

    @Test(expected = RuntimeException.class)
    public void should_return_the_error_using_executor() {
        F.Promise<String> stringPromise = GuavaFutures.asPromise(
                Futures.immediateFailedFuture(new RuntimeException("Oops!")),
                ForkJoinPool.commonPool()
        );
        stringPromise.get(1000);
    }

    @Test
    public void should_return_the_value_within_scala_future() throws Exception {
        running(fakeApplication(), Unchecked.runnable(() -> {
            Future<String> stringFuture = GuavaFutures.asScalaFuture(
                    Futures.immediateFuture("Whats up!")
            );
            assertEquals("Whats up!", Await.result(stringFuture, Duration.create(5, "seconds")));
        }));
    }

    @Test(expected = RuntimeException.class)
    public void should_return_the_error_within_scala_future() throws Exception {
        running(fakeApplication(), Unchecked.runnable( () -> {
            Future<String> stringFuture = GuavaFutures.asScalaFuture(
                    Futures.immediateFailedFuture(new RuntimeException("Oops!"))
            );
            Await.result(stringFuture, Duration.create(5, "seconds"));
        }));
    }

}