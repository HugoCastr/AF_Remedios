package com.example.af_remedios;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface WikiApi {
    @Headers("User-Agent: AppRemediosEstudante/1.0 (seu_email@gmail.com)")
    @GET("page/summary/Grêmio_Foot-Ball_Porto_Alegrense")
    Call<WikiResponse> getGremioInfo();
}