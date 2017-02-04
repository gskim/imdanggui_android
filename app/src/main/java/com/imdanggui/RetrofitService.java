package com.imdanggui;

import com.imdanggui.model.Setting;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by gskim on 2016. 6. 13..
 */
public interface RetrofitService {

    String domain = "http://dlaekdrnl2.godohosting.com/";
    @FormUrlEncoded
    @POST("/imdanggui/register.php")
    Call<Setting> postSetting
            (
                    @Field("device") String device,
                    @Field("regid") String regid,
                    @Field("type") String type,
                    @Field("random") String random
            );

}
