package org.chronopolis.medic.support;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

/**
 *
 * Created by shake on 3/3/17.
 */
public class NotFoundCallWrapper<E> extends CallWrapper<E> {

    private final E e;

    public NotFoundCallWrapper(E e) {
        super(e);
        this.e = e;
    }

    @Override
    public Response<E> execute() throws IOException {
        return Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "test-sample-error"));
    }

    @Override
    public void enqueue(Callback<E> callback) {
        callback.onResponse(this, Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "test-sample-error")));
    }
}
