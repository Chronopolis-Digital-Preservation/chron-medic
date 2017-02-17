package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairRequest;
import org.springframework.data.domain.Page;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

/**
 * Interface defining the repair api
 *
 * Created by shake on 2/16/17.
 */
public interface Repairs {

    @GET("api/repair/requests")
    Page<Repair> getRepairs(@QueryMap Map<String, String> params);

    @GET("api/repair/request/{id}")
    Repair getRepair(@Path("id") Long id);

    @POST("api/requests")
    Repair createRepair(@Body RepairRequest request);

    @POST("api/requests/repair/{id}/fulfill")
    Fulfillment fulfillRepair(@Path("id") Long id);

    @GET("api/requests/fulfillments")
    Page<Fulfillment> getFulfillments(@QueryMap Map<String, String> params);

    @GET("api/requests/fulfillments/{id}")
    Fulfillment getFulfillment(@Path("id") Long id);

    @POST("api/requests/fulfillments/{id}/ready")
    Fulfillment readyFulfillment(@Path("id") Long id, @Body FulfillmentStrategy strategy);

    @POST("api/requests/fulfillments/{id}/complete")
    Fulfillment completeFulfillment(@Path("id") Long id);


}
