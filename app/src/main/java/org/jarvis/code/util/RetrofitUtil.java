package org.jarvis.code.util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by KimChheng on 5/28/2017.
 */

public final class RetrofitUtil {

    public final static Retrofit RETROFIT;

    static {
        RETROFIT = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private RetrofitUtil() {

    }
}
