package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.FulfillmentStatus;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairRequest;
import org.springframework.data.domain.Page;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

/**
 * Interface defining the repair api
 *
 * Created by shake on 2/16/17.
 */
public interface Repairs {

    // todo: Should these be split up in to two interfaces?
    // Repair API

    @GET("api/repair/requests")
    Call<Page<Repair>> getRepairs(@QueryMap Map<String, String> params);

    @GET("api/repair/request/{id}")
    Call<Repair> getRepair(@Path("id") Long id);

    @POST("api/repair/requests")
    Call<Repair> createRepair(@Body RepairRequest request);

    @PUT("api/repair/requests/{id}/cleaned")
    Call<Repair> repairCleaned(@Path("id") Long id);

    @PUT("api/repair/requests/{id}/replaced")
    Call<Repair> repairCopied(@Path("id") Long id);

    @PUT("api/repair/requests/{id}/audit")
    Call<Repair> repairAudited(@Path("id") Long id, @Body AuditStatus status);

    // Fulfillment API

    @POST("api/requests/repair/{id}/fulfill")
    Call<Fulfillment> fulfillRepair(@Path("id") Long id);

    @GET("api/repair/fulfillments")
    Call<Page<Fulfillment>> getFulfillments(@QueryMap Map<String, String> params);

    @GET("api/repair/fulfillments/{id}")
    Call<Fulfillment> getFulfillment(@Path("id") Long id);

    @PUT("api/repair/fulfillments/{id}/cleaned")
    Call<Fulfillment> fulfillmentCleaned(@Path("id") Long id);

    @PUT("api/repair/fulfillments/{id}/complete")
    Call<Fulfillment> fulfillmentCompleted(@Path("id") Long id);

    @PUT("api/repair/fulfillments/{id}/validated")
    Call<Fulfillment> fulfillmentValidated(@Path("id") Long id);

    @PUT("api/repair/fulfillments/{id}/ready")
    Call<Fulfillment> readyFulfillment(@Path("id") Long id, @Body FulfillmentStrategy strategy);

    @PUT("api/repair/fulfillments/{id}/status")
    Call<Fulfillment> fulfillmentUpdated(@Path("id") Long id, @Body FulfillmentStatus status);

}
