package com.example.newapp2;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

public interface GetDataService {

    @Headers("User-Agent: newapp2")
    //@GET("?addressdetails=1&namedetails=1&extratags=1&q=Spożywcze+in+Gdansk&format=json&limit=10")
    //Call<List<Data>> getData();

    @GET("?getData")
    Call<List<Data>> getData(@QueryMap (encoded=true) Map<String,String> filters);
}
