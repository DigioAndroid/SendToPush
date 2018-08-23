package com.accesium.sendtopush.service;

import com.accesium.sendtopush.util.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Fran Gilberte on 20/01/2016.
 */
    public class BaseService {

    protected String mBaseUrl;

    public Retrofit getAdapter(String baseUrl){
        this.mBaseUrl = baseUrl;

        OkHttpClient client = Utils.getCustomOkHttpClient();

        return new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
