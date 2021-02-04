package com.example.facedetection.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofitClient=null;

    public static Retrofit getRetrofitClient() {
        if(retrofitClient == null)
        {
            retrofitClient = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.105:5000")//192.168.0.105 is localhost on emulator
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();


        }
        return retrofitClient;
    }
}
