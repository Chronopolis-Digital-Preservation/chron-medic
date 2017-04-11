package org.chronopolis.medic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Optional;
import java.util.concurrent.Phaser;

/**
 * Callback which returns an Optional of the returned item
 *
 * Created by shake on 2/23/17.
 */
public class OptionalCallback<T> implements Callback<T> {
    private final Logger log = LoggerFactory.getLogger(OptionalCallback.class);

    private T body;

    // Need two parties for the phaser, one for the callback and one for the get
    private Phaser phaser = new Phaser(2);


    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            this.body = response.body();
        } else {
            log.warn("Failed http call: [{}]{} | {}", call.request().method(), call.request().url(), response.code());
        }

        phaser.arriveAndDeregister();
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        log.warn("Failed http call: [{}]{}", call.request().method(), call.request().url(), t);
        phaser.arriveAndDeregister();
    }

    public Optional<T> get() {
        phaser.arriveAndAwaitAdvance();
        return Optional.ofNullable(body);
    }
}
