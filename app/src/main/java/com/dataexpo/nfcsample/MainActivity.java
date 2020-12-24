package com.dataexpo.nfcsample;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.nfcsample.pojo.MsgBean;
import com.dataexpo.nfcsample.pojo.MsgBeanString;
import com.dataexpo.nfcsample.pojo.User;
import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.NfcUtils;
import com.dataexpo.nfcsample.utils.Utils;
import com.dataexpo.nfcsample.utils.net.HttpCallback;
import com.dataexpo.nfcsample.utils.net.HttpService;
import com.dataexpo.nfcsample.utils.net.URLs;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

import static android.util.Base64.NO_WRAP;
import static com.dataexpo.nfcsample.utils.Utils.FORMAT_YMD_HMS;

/**
 * 读的是IC卡
 */
public class MainActivity extends BascActivity {
    private static final String TAG = MainActivity.class.getName();
    private Context mContext;

    private TextView tv_card_id;
    private TextView tv_time;
    private TextView main_tv_init_left;
    private ImageView iv_init;
    private ImageView iv_head;
    private TextView main_tv_name;
    private TextView main_tv_group;
    private ImageView iv_success;
    private ImageView iv_fail_permission;
    private CircularProgressView progressView;

    private final int STATUS_INIT = 1;
    private final int STATUS_CHECK_CARD_EXIST = 2;
    private final int STATUS_CHECK_IMAGE = 3;
    private final int STATUS_SHOWING = 4;

    private final int SHOW_STATUS_INIT = 1;
    private final int SHOW_STATUS_SUCCESS = 2;
    private final int SHOW_STATUS_ERROR_PERMISSION = 3;

    private User user;

    private int mStatus = STATUS_INIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        File f=new File("/sdcard/cardImage");
        if (!f.exists()) {
            f.mkdir();
        }

        NfcUtils.NfcCheck(mContext);
        initView();
    }

    private void initView() {
        tv_card_id = findViewById(R.id.main_tv_cardid);
        tv_time = findViewById(R.id.main_tv_time);
        main_tv_init_left = findViewById(R.id.main_tv_init_left);
        iv_init = findViewById(R.id.iv_init);
        iv_head = findViewById(R.id.iv_head);
        main_tv_name = findViewById(R.id.main_tv_name);
        main_tv_group = findViewById(R.id.main_tv_group);
        iv_success = findViewById(R.id.iv_success);
        iv_fail_permission = findViewById(R.id.iv_fail_permission);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
    }

    private void reSetView(int status) {
        if (status == SHOW_STATUS_INIT) {
            main_tv_init_left.setVisibility(View.VISIBLE);
            iv_init.setVisibility(View.VISIBLE);
            iv_head.setVisibility(View.INVISIBLE);
            main_tv_name.setVisibility(View.INVISIBLE);
            main_tv_group.setVisibility(View.INVISIBLE);
            iv_success.setVisibility(View.INVISIBLE);
            iv_fail_permission.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);

        } else if (status == SHOW_STATUS_SUCCESS) {
            main_tv_init_left.setVisibility(View.INVISIBLE);
            iv_init.setVisibility(View.INVISIBLE);
            iv_head.setVisibility(View.VISIBLE);
            main_tv_name.setVisibility(View.VISIBLE);
            main_tv_group.setVisibility(View.VISIBLE);
            iv_success.setVisibility(View.VISIBLE);
            iv_fail_permission.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);
        } else if (status == SHOW_STATUS_ERROR_PERMISSION) {
            main_tv_init_left.setVisibility(View.VISIBLE);
            iv_init.setVisibility(View.INVISIBLE);
            iv_head.setVisibility(View.INVISIBLE);
            main_tv_name.setVisibility(View.INVISIBLE);
            main_tv_group.setVisibility(View.INVISIBLE);
            iv_success.setVisibility(View.INVISIBLE);
            iv_fail_permission.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent (intent);
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals (intent.getAction ())) {
            // 处理该intent
            String cardId = NfcUtils.getCardId(intent);
            Log.i(TAG, " cardId is " + cardId);
            tv_card_id.setText(cardId);

            tv_time.setText(Utils.formatTime(new Date().getTime(), FORMAT_YMD_HMS));
            progressView.setVisibility(View.VISIBLE);
            if (mStatus == STATUS_INIT || mStatus == STATUS_SHOWING) {
                checkCard(cardId);
            }
        }
    }

    private void checkCard(String cardId) {
        Log.i(TAG, "checkCard " + cardId);

        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("ICD", cardId);

        HttpService.getWithParams(mContext, URLs.checkCard, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());

                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
//                final MsgBean msgBean = new Gson().fromJson(response, MsgBean.class);
                Log.i(TAG, "online check expoid response: " + response);
                final MsgBean result = new Gson().fromJson(response, MsgBean.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.data != null) {
                            Log.i(TAG, " names " + result.data.toString());
                            user = (User) result.data;

                            if (user.getIsFort().equals(1) && (user.getEuStatus().equals(1) || user.getEuStatus().equals(3))) {
                                main_tv_name.setText(user.getUiName());
                                main_tv_group.setText(user.getEuDefine());
                                reSetView(SHOW_STATUS_SUCCESS);
                                showHead();
                            } else {
                                main_tv_init_left.setText("无权限");
                                reSetView(SHOW_STATUS_ERROR_PERMISSION);
                            }

                        } else {
                            main_tv_init_left.setText("无权限");
                            reSetView(SHOW_STATUS_ERROR_PERMISSION);
                        }
                    }
                });
            }
        });
    }

    private void addCount(String cardId) {
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cardId", cardId);

        HttpService.getWithParams(mContext, URLs.addCount, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());

                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.i(TAG, "online check expoid response: " + response);

                //添加使用次数成功
                //progressView.setVisibility(View.INVISIBLE);
                //显示拿到的数据， 进行下一个步骤
                //显示图像
                //showHead("");
            }
        });
    }

    private void showHead() {
        //如果人像不存在
        File f=new File("/sdcard/cardImage/" + user.euImage);
        Log.i(TAG, "- " + f.getAbsolutePath());

        if(f.exists())
        {
            try {
                FileInputStream fis = new FileInputStream(f);
                iv_head.setImageBitmap(BitmapFactory.decodeStream(fis));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
//            return
//            ContentResolver cr = this.getContentResolver();
//            BitmapFactory.decodeStream(cr.openInputStream(f.));
//            Bitmap bitmap = getLoacalBitmap("/sdcard/tubiao.jpg"); //从本地取图片(在cdcard中获取)  //
//            //image1 .setImageBitmap(bitmap); //设置Bitmap
        } else {

            //获取人像
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("euId", user.euId + "");

            HttpService.getWithParams(mContext, URLs.getHead, hashMap, new HttpCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i(TAG, e.getMessage());

                    progressView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.i(TAG, "online check expoid response: " + response);
                    MsgBeanString result = new Gson().fromJson(response, MsgBeanString.class);
                    saveImage(result.data.euImage, user.euImage);
//                    }
//                }
                    //显示图像， 并且保存到本地
                    //Bitmap bitmap = getLoacalBitmap("/sdcard/tubiao.jpg"); //从本地取图片(在cdcard中获取)  //
                    //image1 .setImageBitmap(bitmap); //设置Bitmap
                }
            });
        }
    }

    private void saveImage(String imageStr, String name) {
        byte[] imgBytes = Base64.decode(imageStr, NO_WRAP);
        Bitmap bitMap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
        iv_head.setImageBitmap(bitMap);

        saveToLocal(imgBytes, name);
    }

    private void saveToLocal(byte[] imgBytes, String name) {

        FileOutputStream out = null;

        try {
            File file = new File("/sdcard/cardImage/" + name);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(imgBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        NfcUtils.enable(this);
    }

    @Override
    protected void onPause(){
        super.onPause ();
        //注销注册
        NfcUtils.disable(this);
    }

    public static void jsonTree(JsonElement e)
    {
        if (e.isJsonNull())
        {
            System.out.println(e.toString());
            return;
        }

        if (e.isJsonPrimitive())
        {
            System.out.println("111 " + e.toString());
            return;
        }

        if (e.isJsonArray())
        {
            JsonArray ja = e.getAsJsonArray();
            if (null != ja)
            {
                for (JsonElement ae : ja)
                {
                    System.out.println("t 1");
                            jsonTree(ae);
                }
            }
            return;
        }

        if (e.isJsonObject())
        {
            Set<Map.Entry<String, JsonElement>> es = e.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> en : es)
            {
                System.out.println("o 1");
                jsonTree(en.getValue());
            }
        }
    }

}
