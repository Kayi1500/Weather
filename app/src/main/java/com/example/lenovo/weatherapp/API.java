package com.example.lenovo.weatherapp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by lenovo on 2018-09-07.
 */



public interface API {
    String base_url = "https://www.met.no/";
    @GET
    Call<ResponseBody> getData(@Url String link);
}

