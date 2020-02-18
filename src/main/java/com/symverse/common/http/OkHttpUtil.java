package com.symverse.common.http;
import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();

    public String sendGet(String connectionUrl , Map<String,String> parmas ) throws Exception {
    	
        HttpUrl.Builder urlBuilder = HttpUrl.parse(connectionUrl).newBuilder();
         
        for ( String key : parmas.keySet()) {
        	 String value = parmas.get(key);
        	 urlBuilder.addQueryParameter(key,value );
        }
         
        String requestUrl =  urlBuilder.build().toString();
         
        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Content-Type", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }

    }

    public String sendPost(String connectionUrl , String jsonParam) throws Exception {

    	RequestBody body = RequestBody.create(JSON, jsonParam );
    	Request request = new Request.Builder()
    		      .url(connectionUrl)
    		      .post(body)
    		      .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }
}
