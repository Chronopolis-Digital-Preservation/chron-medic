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

    /**
     * Apply a get operation with the given parameters, returning the result
     *
     * @param get the get operation to execute
     * @param params the parameters to execute with
     * @param <U> The type of the parameters
     * @param <E> The return type of the get operation
     * @return the result of the get
     */
    public <U, E> Optional<E> get(Function<U, Call<E>> get, U params) {
        Call<E> call = get.apply(params);
        OptionalCallback<E> cb = new OptionalCallback<>();
        call.enqueue(cb);
        return cb.get();
    }

}
