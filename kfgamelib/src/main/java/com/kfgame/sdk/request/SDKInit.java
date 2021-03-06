package com.kfgame.sdk.request;

import android.content.Context;
import android.widget.Toast;

import com.kfgame.sdk.KFGameSDK;
import com.kfgame.sdk.common.Config;
import com.kfgame.sdk.common.Encryption;
import com.kfgame.sdk.model.KFGameResponse;
import com.kfgame.sdk.okgo.callback.JsonCallback;
import com.kfgame.sdk.pojo.InitModel;
import com.kfgame.sdk.util.DeviceUtils;
import com.kfgame.sdk.util.LogUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.base.Request;

import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Tobin on 2018/2/8.
 */

public class SDKInit {
    private static SDKInit ourInstance = null;

    private InitModel initModel;

    public static SDKInit getInstance() {
        if (ourInstance == null) {
            synchronized (SDKInit.class) {
                if (ourInstance == null) {
                    ourInstance = new SDKInit();
                }
            }
        }
        return ourInstance;
    }

    public void sdkInit() {
        Context context = KFGameSDK.getInstance().getActivity();
        TreeMap<String,String> paraMap = new TreeMap<>();
        paraMap.put("appId",Config.APP_ID);
        paraMap.put("channelId",Config.CHANNEL_ID);
        paraMap.put("deviceInfo",DeviceUtils.getSystemModel());
        paraMap.put("imei",DeviceUtils.getIMEI(context));
        paraMap.put("imsi",DeviceUtils.getIMSI(context));
        paraMap.put("mac",DeviceUtils.getAdresseMAC(context));
        paraMap.put("mobile",DeviceUtils.getPhoneNumber(context));
        paraMap.put("osInfo","android");
        paraMap.put("udid",DeviceUtils.getUniqueId(context));
        paraMap.put("timestamp", System.currentTimeMillis() / 1000 + "");
        paraMap.put("version",Config.API_VERSION);

        String signpre = "";
        PostRequest postRequest = OkGo.<KFGameResponse<InitModel>>post(Config.URL_SDK_INIT);
        postRequest.cacheMode(CacheMode.NO_CACHE);
        for (Map.Entry<String, String> entry: paraMap.entrySet()) {
            signpre +=entry.getKey() + "=" + entry.getValue() + "&";
            postRequest.params(entry.getKey(),entry.getValue());
            LogUtil.e("Tobin getKey: " + entry.getKey() + " getValue: " + entry.getValue());
        }

        LogUtil.e("Tobin: signpre md5Crypt: " + signpre + "+key");
        String sign = Encryption.md5Crypt(signpre + Config.encodeKey);
        LogUtil.e("Tobin: sign md5Crypt: " + sign);

        paraMap.put("sign",sign);

        JSONObject jsons = new JSONObject(paraMap);

        postRequest.upJson(jsons);

        postRequest.tag("initSDK");
        postRequest.execute(jsonCallback);

    }

    JsonCallback jsonCallback = new JsonCallback<KFGameResponse<InitModel>>() {
        @Override
        public void onStart(Request<KFGameResponse<InitModel>, ? extends Request> request) {
            LogUtil.e("Tobin: sdkInit onStart: ");
        }

        @Override
        public void onError(Response<KFGameResponse<InitModel>> response) {
            String error = response.getException().getMessage();
            LogUtil.e("Tobin onError" + error);
            LogUtil.e("Tobin: sdkInit onError: ");
            Toast.makeText(KFGameSDK.getInstance().getActivity(), "SDK初始化: " + error,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFinish() {
            LogUtil.e("Tobin: sdkInit onFinish: ");
        }

        @Override
        public void onSuccess(Response<KFGameResponse<InitModel>> response) {
            initModel = response.body().data;
            LogUtil.e("Tobin: sdkInit onSuccess: " + initModel.toString());
        }
    };

}
