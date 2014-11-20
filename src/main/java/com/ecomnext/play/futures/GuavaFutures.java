/*
 * Copyright (C) Ecomnext <http://www.ecomnext.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ecomnext.play.futures;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import play.libs.Akka;
import play.libs.F;
import scala.concurrent.Future;
import scala.concurrent.Promise;

import java.util.concurrent.Executor;

/**
 * Convenience methods to transform ListenableFuture objects into Scala
 * Futures or PlayFramework Promises.
 */
public class GuavaFutures {
    /**
     * Converts a {@link ListenableFuture} into a {@link Future}.
     *
     * <p>Note: If the callback is slow or heavyweight, consider {@linkplain
     * #asPromise(ListenableFuture, Executor) supplying an executor}.
     * If you do not supply an executor, it will use the default executor,
     * the Play default thread pool will be used.
     *
     * @param lf the {@link ListenableFuture} to register a listener on.
     * @return a {@link Future} that emits the value when the future completes.
     */
    public static <V> Future<V> asScalaFuture(final ListenableFuture<V> lf) {
        Promise<V> promise = akka.dispatch.Futures.promise();
        Futures.addCallback(lf, new FutureCallbackAdapter<>(promise), getDefaultExecutor());
        return promise.future();
    }

    /**
     * Converts a {@link ListenableFuture} into a {@link Future}.
     * @param lf the {@link ListenableFuture} to register a listener on.
     * @param executor the {@link Executor} where the callback will be executed.
     * @return a {@link Future} that emits the value when the future completes.
     */
    public static <V> Future<V> asScalaFuture(final ListenableFuture<V> lf, final Executor executor) {
        Promise<V> promise = akka.dispatch.Futures.promise();
        Futures.addCallback(lf, new FutureCallbackAdapter<>(promise), executor);
        return promise.future();
    }

    /**
     * Converts a {@link ListenableFuture} into a {@link F.Promise}.
     *
     * <p>Note: If the callback is slow or heavyweight, consider {@linkplain
     * #asPromise(ListenableFuture, Executor) supplying an executor}.
     * If you do not supply an executor, it will use the default executor,
     * the Play default thread pool will be used.
     *
     * @param lf the {@link ListenableFuture} to register a listener on.
     * @return a {@link F.Promise} that emits the value when the future completes.
     *
     * @see com.google.common.util.concurrent.Futures#addCallback(ListenableFuture, FutureCallback)
     */
    public static <V> F.Promise<V> asPromise(ListenableFuture<V> lf) {
        return F.Promise.wrap(asScalaFuture(lf, getDefaultExecutor()));
    }

    /**
     * Converts a {@link ListenableFuture} into a {@link F.Promise}.
     * @param lf the {@link ListenableFuture} to register a listener on.
     * @param executor the {@link Executor} where the callback will be executed.
     * @return a {@link F.Promise} that emits the value when the future completes.
     */
    public static <V> F.Promise<V> asPromise(ListenableFuture<V> lf, Executor executor) {
        return F.Promise.wrap(asScalaFuture(lf, executor));
    }

    /**
     * Gets the Play default thread. This is the default thread pool in which
     * all application code in Play Framework is executed. It is an Akka dispatcher,
     * and can be configured by configuring Akka, described below. By default,
     * it has one thread per processor.
     * @return the executor
     */
    private static Executor getDefaultExecutor() {
        return Akka.system().dispatchers().lookup("play.akka.actor.default-dispatcher");
    }

    private static class FutureCallbackAdapter<V> implements FutureCallback<V> {
        Promise<V> promise;

        FutureCallbackAdapter(Promise<V> p) {
            promise = p;
        }

        @Override
        public void onSuccess(V result) {
            promise.success(result);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public void onFailure(Throwable throwable) {
            promise.failure(throwable);
        }
    }
}
