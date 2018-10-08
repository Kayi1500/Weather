package com.example.lenovo.weatherapp;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IMG {
    String base_url = "https://www.met.no/";
    @GET
    Call<ResponseBody> getData2(@Url String link2);
}
