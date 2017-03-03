package org.chronopolis.medic.support;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

/**
 *
 * Created by shake on 3/3/17.
 */
public class CallWrapper<E> implements Call<E> {

    private final E e;

    public CallWrapper(E e) {
        this.e = e;
    }

    @Override
    public Response<E> execute() throws IOException {
        return Response.success(e);
    }

    @Override
    public void enqueue(Callback<E> callback) {
        callback.onResponse(this, Response.success(e));
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<E> clone() {
        return null;
    }

    @Override
    public Request request() {
        return new Request.Builder().url("http://mock-url").build();
    }
}
