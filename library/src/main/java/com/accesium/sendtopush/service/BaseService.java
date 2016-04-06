package com.accesium.sendtopush.service;

import com.accesium.sendtopush.util.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Created by Fran Gilberte on 20/01/2016.
 */
    public class BaseService {

    protected String mBaseUrl;

    public Retrofit getAdapter(String baseUrl){
        this.mBaseUrl = baseUrl;

        OkHttpClient client = Utils.getCustomOkHttpClient();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        client.interceptors().add(interceptor);

        return new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
