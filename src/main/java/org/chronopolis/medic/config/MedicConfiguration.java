package org.chronopolis.medic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import org.chronopolis.common.ace.OkBasicInterceptor;
import org.chronopolis.medic.client.Repairs;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.repair.Fulfillment;
import org.chronopolis.rest.models.repair.Repair;
import org.chronopolis.rest.support.PageDeserializer;
import org.chronopolis.rest.support.ZonedDateTimeDeserializer;
import org.chronopolis.rest.support.ZonedDateTimeSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.List;

/**
 *
 * Created by shake on 2/20/17.
 */
@Configuration
@EnableConfigurationProperties
public class MedicConfiguration {

    @Bean
    private Repairs repairs(IngestConfiguration ingest) {
        OkHttpClient okClient = new OkHttpClient.Builder()
                .addInterceptor(new OkBasicInterceptor(ingest.getUsername(), ingest.getPassword()))
                .build();

        // Not sure what types we'll actually need but we can figure that out over time
        Type bagPage = new TypeToken<PageImpl<Bag>>() {}.getType();
        Type bagList = new TypeToken<List<Bag>>() {}.getType();

        Type repairPage = new TypeToken<PageImpl<Repair>>() {}.getType();
        Type repairList = new TypeToken<List<Repair>>() {}.getType();

        Type ffPage = new TypeToken<PageImpl<Fulfillment>>() {}.getType();
        Type ffList = new TypeToken<List<Fulfillment>>() {}.getType();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ffPage, new PageDeserializer(ffList))
                .registerTypeAdapter(bagPage, new PageDeserializer(bagList))
                .registerTypeAdapter(repairPage, new PageDeserializer(repairList))
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeDeserializer())
                .create();

        Retrofit r = new Retrofit.Builder()
                .client(okClient)
                .baseUrl(ingest.getUsername())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return r.create(Repairs.class);
    }

}
