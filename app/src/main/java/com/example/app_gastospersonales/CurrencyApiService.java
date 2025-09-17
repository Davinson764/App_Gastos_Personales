package com.example.app_gastospersonales;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyApiService {
    @GET("latest")
    Call<CurrencyResponse> getLatestRates(
            @Query("apikey") String apiKey,
            @Query("base_currency") String base
    );
}
