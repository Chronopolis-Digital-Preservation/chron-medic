package org.chronopolis.medic.client;

import org.chronopolis.rest.models.repair.AuditStatus;
import org.chronopolis.rest.models.repair.FulfillmentStrategy;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.models.repair.RepairRequest;
import org.chronopolis.rest.models.repair.RepairStatus;
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

    @GET("api/repairs")
    Call<Page<Repair>> getRepairs(@QueryMap Map<String, String> params);

    @GET("api/repairs/{id}")
    Call<Repair> getRepair(@Path("id") Long id);

    @POST("api/repairs")
    Call<Repair> createRepair(@Body RepairRequest request);

    @PUT("api/repairs/{id}/cleaned")
    Call<Repair> repairCleaned(@Path("id") Long id);

    @PUT("api/repairs/{id}/replaced")
    Call<Repair> repairCopied(@Path("id") Long id);

    @PUT("api/repairs/{id}/audit")
    Call<Repair> repairAudited(@Path("id") Long id, @Body AuditStatus status);

    @POST("api/repairs/{id}/fulfill")
    Call<Repair> fulfillRepair(@Path("id") Long id);

    // @PUT("api/repairs/{id}/cleaned")
    // Call<Fulfillment> fulfillmentCleaned(@Path("id") Long id);

    @PUT("api/repairs/{id}/complete")
    Call<Repair> repairComplete(@Path("id") Long id);

    @PUT("api/repairs/{id}/validated")
    Call<Repair> repairValid(@Path("id") Long id);

    @PUT("api/repairs/{id}/ready")
    Call<Repair> repairReady(@Path("id") Long id, @Body FulfillmentStrategy strategy);

    @PUT("api/repairs/{id}/status")
    Call<Repair> repairUpdate(@Path("id") Long id, @Body RepairStatus status);

}
