package org.chronopolis.medic.scheduled;

import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.medic.OptionalCallback;
import retrofit2.Call;

import java.util.Optional;
import java.util.function.Function;

/**
 * Super class for Schedulers, just to provide a few helper methods
 *
 * Created by shake on 3/16/17.
 */
public class Scheduler<T> {

    private final TrackingThreadPoolExecutor<T> pool;

    public Scheduler(TrackingThreadPoolExecutor<T> pool) {
        this.pool = pool;
    }

    public void submit(T t, Runnable runnable) {
        pool.submitIfAvailable(runnable, t);
    }

    public <U, E> Optional<E> get(Function<U, Call<E>> get, U params) {
        Call<E> call = get.apply(params);
        OptionalCallback<E> cb = new OptionalCallback<>();
        call.enqueue(cb);
        return cb.get();
    }

}
