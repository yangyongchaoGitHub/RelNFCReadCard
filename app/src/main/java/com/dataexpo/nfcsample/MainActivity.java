package com.dataexpo.nfcsample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.nfcsample.pojo.MsgBean;
import com.dataexpo.nfcsample.pojo.Permissions;
import com.dataexpo.nfcsample.pojo.RegStatus;
import com.dataexpo.nfcsample.pojo.User;
import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.NfcUtils;
import com.dataexpo.nfcsample.utils.Utils;
import com.dataexpo.nfcsample.utils.net.HttpCallback;
import com.dataexpo.nfcsample.utils.net.HttpService;
import com.dataexpo.nfcsample.utils.net.URLs;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

import static android.util.Base64.NO_WRAP;
import static com.dataexpo.nfcsample.utils.Utils.FORMAT_YMD_HMS;

/**
 * 读的是IC卡
 */
public class MainActivity extends BascActivity {
    private static final String TAG = MainActivity.class.getName();
    private Context mContext;

    private TextView main_tv_init_left;
    private ImageView iv_init;
    private ImageView iv_head;
    private TextView main_tv_name;
    private TextView main_tv_group;
    private TextView main_tv_companytitle;
    private TextView main_tv_timeorpermission;
    private TextView main_tv_permissionfail;
    private ImageView iv_success;
    private ImageView iv_fail;
    private ImageView iv_fail_permission;
    private CircularProgressView progressView;
    private TextView main_tv_area;
    private TextView main_tv_ename;

    private final int STATUS_INIT = 1;
    private final int STATUS_CHECK_CARD_EXIST = 2;
    private final int STATUS_CHECK_IMAGE = 3;
    private final int STATUS_SHOWING = 4;
    private final int STATUS_ERROR = 5;

    private final int SHOW_STATUS_INIT = 1;
    private final int SHOW_STATUS_SUCCESS = 2;
    private final int SHOW_STATUS_ERROR_PERMISSION = 3;

    private final int SHOW_INIT = 0;
    private final int SHOW_ING = 1;
    private final int SHOW_WORKING = 2;

    Permissions localPermission;

    private User user;

    private int mStatus = STATUS_INIT;

    private String cardId;


    private volatile int showInt = SHOW_INIT;
    private volatile long showStartTime = System.currentTimeMillis();
    private volatile int running = 0;

    /**
     * 手机震动
     */
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate " + MyApplication.getPermissionSelect());

        if (MyApplication.getPermissionSelect() == MyApplication.PERMISSION_SELECT_WAIT) {
            this.finish();
            return;
        }

        mContext = this;
        initView();
        File f=new File("/sdcard/cardImage");
        if (!f.exists()) {
            f.mkdir();
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            localPermission = (Permissions) bundle.getSerializable("pomission");
            main_tv_area.setText(localPermission.getNames());
        }

        NfcUtils.NfcCheck(mContext);
        TimerThread timerThread = new TimerThread();
        timerThread.start();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void initView() {
        main_tv_init_left = findViewById(R.id.main_tv_init_left);
        iv_init = findViewById(R.id.iv_init);
        iv_head = findViewById(R.id.iv_head);
        main_tv_name = findViewById(R.id.main_tv_name);
        main_tv_ename = findViewById(R.id.main_tv_ename);
        main_tv_group = findViewById(R.id.main_tv_group);
        main_tv_companytitle = findViewById(R.id.main_tv_companytitle);
        main_tv_timeorpermission = findViewById(R.id.main_tv_timeorpermission);
        iv_success = findViewById(R.id.iv_success);
        iv_fail = findViewById(R.id.iv_fail);
        iv_fail_permission = findViewById(R.id.iv_fail_permission);
        main_tv_permissionfail = findViewById(R.id.main_tv_permissionfail);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        main_tv_area = findViewById(R.id.main_tv_area);
    }

    private void reSetView(int status, boolean permission) {
        if (status == SHOW_STATUS_INIT) {
            main_tv_init_left.setVisibility(View.VISIBLE);
            iv_init.setVisibility(View.VISIBLE);
            iv_head.setVisibility(View.INVISIBLE);
            main_tv_name.setVisibility(View.INVISIBLE);
            main_tv_ename.setVisibility(View.INVISIBLE);
            main_tv_group.setVisibility(View.INVISIBLE);
            main_tv_timeorpermission.setVisibility(View.INVISIBLE);
            main_tv_companytitle.setVisibility(View.INVISIBLE);
            iv_success.setVisibility(View.INVISIBLE);
            iv_fail.setVisibility(View.INVISIBLE);
            iv_fail_permission.setVisibility(View.INVISIBLE);
            main_tv_permissionfail.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);

        } else if (status == SHOW_STATUS_SUCCESS) {
            main_tv_init_left.setVisibility(View.INVISIBLE);
            iv_init.setVisibility(View.INVISIBLE);
            iv_head.setVisibility(View.VISIBLE);
            main_tv_name.setVisibility(View.VISIBLE);
            main_tv_ename.setVisibility(View.VISIBLE);
            main_tv_group.setVisibility(View.VISIBLE);
            main_tv_timeorpermission.setVisibility(View.VISIBLE);
            main_tv_companytitle.setVisibility(View.VISIBLE);

            iv_fail_permission.setVisibility(View.INVISIBLE);
            progressView.setVisibility(View.INVISIBLE);
            if (permission) {
                iv_success.setVisibility(View.VISIBLE);
                iv_fail.setVisibility(View.INVISIBLE);
                main_tv_permissionfail.setVisibility(View.INVISIBLE);
            } else {
                iv_success.setVisibility(View.INVISIBLE);
                iv_fail.setVisibility(View.VISIBLE);
                main_tv_permissionfail.setVisibility(View.VISIBLE);
            }

        } else if (status == SHOW_STATUS_ERROR_PERMISSION) {
            main_tv_timeorpermission.setText("");
            main_tv_init_left.setVisibility(View.VISIBLE);
            iv_init.setVisibility(View.INVISIBLE);
            iv_head.setVisibility(View.INVISIBLE);
            main_tv_name.setVisibility(View.INVISIBLE);
            main_tv_ename.setVisibility(View.INVISIBLE);
            main_tv_group.setVisibility(View.INVISIBLE);
            main_tv_companytitle.setVisibility(View.INVISIBLE);
            iv_success.setVisibility(View.INVISIBLE);
            iv_fail.setVisibility(View.INVISIBLE);
            main_tv_permissionfail.setVisibility(View.INVISIBLE);
            iv_fail_permission.setVisibility(View.VISIBLE);
            main_tv_timeorpermission.setVisibility(View.VISIBLE);
            progressView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent (intent);
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals (intent.getAction ())) {
            // 处理该intent
            cardId = NfcUtils.getCardId(intent);
            Log.i(TAG, " cardId is " + cardId);

            progressView.setVisibility(View.VISIBLE);
            if (mStatus == STATUS_INIT || mStatus == STATUS_SHOWING || mStatus == STATUS_ERROR) {
                showInt = SHOW_WORKING;
                mStatus = STATUS_CHECK_CARD_EXIST;
                checkCard();
            }
        }
    }

    private void checkCard() {
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
                mStatus = STATUS_ERROR;
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
                                List<RegStatus> regStatuses = user.getRegList();
                                boolean bOk = false;
                                String permission = "";
                                for (RegStatus r : regStatuses) {
                                    if (r.getRegionId() == localPermission.getId()) {
                                        bOk = true;
                                    }
                                    permission += r.getNames() + " ";
                                }

                                main_tv_name.setText(user.getUiName());
                                main_tv_ename.setText(user.getUiDapt());
                                main_tv_group.setText(user.getEuDefine());
                                main_tv_companytitle.setText(user.getUiCompanyTitle());

                                main_tv_timeorpermission.setText(user.getPrintTime());

                                if (!bOk) {
                                    main_tv_timeorpermission.setText(permission);
                                }
                                reSetView(SHOW_STATUS_SUCCESS, bOk);
                                main_tv_timeorpermission.setVisibility(View.VISIBLE);

                                if (user.initsuffix()) {
                                    progressView.setVisibility(View.VISIBLE);
                                    mStatus = STATUS_CHECK_IMAGE;
                                    showHead();
                                } else {
                                    mStatus = STATUS_SHOWING;
                                    showStartTime = System.currentTimeMillis();
                                    showInt = SHOW_ING;
                                }
                            } else {
                                main_tv_init_left.setText("审核未通过");
                                reSetView(SHOW_STATUS_ERROR_PERMISSION, false);
                                mStatus = STATUS_ERROR;
                                showStartTime = System.currentTimeMillis();
                                showInt = SHOW_ING;
                            }

                        } else {
                            main_tv_init_left.setText("非法卡");
                            reSetView(SHOW_STATUS_ERROR_PERMISSION, false);
                            mStatus = STATUS_ERROR;
                            showStartTime = System.currentTimeMillis();
                            showInt = SHOW_ING;
                            //手机震动
                            long[] pattern = { 200, 1000};
                            vibrator.vibrate(pattern, -1);
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
        //Log.i(TAG, "filedir " + this.getExternalFilesDir("images").getAbsolutePath());
        //如果人像不存在
        File f = new File("/sdcard/cardImage/" + cardId + user.getSuffix());
        Log.i(TAG, "- " + f.getAbsolutePath());

        if(f.exists()) {
            //显示本地图片
            try {
                FileInputStream fis = new FileInputStream(f);
                iv_head.setImageBitmap(BitmapFactory.decodeStream(fis));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mStatus = STATUS_SHOWING;
            progressView.setVisibility(View.INVISIBLE);
            showStartTime = System.currentTimeMillis();
            showInt = SHOW_ING;

        } else {
            //获取人像
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("euId", user.getEuId() + "");

            HttpService.getWithParams(mContext, URLs.getHead, hashMap, new HttpCallback() {
                @Override
                public void onError(Call call, final Exception e, int id) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "网络异常，获取人像失败", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, e.getMessage());
                            progressView.setVisibility(View.INVISIBLE);
                            mStatus = STATUS_SHOWING;
                            showStartTime = System.currentTimeMillis();
                            showInt = SHOW_ING;
                        }
                    });
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.i(TAG, "online check expoid response: " + response);
                    final MsgBean result = new Gson().fromJson(response, MsgBean.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] imgBytes = Base64.decode(result.data.getEuImage(), NO_WRAP);
                            Bitmap bitMap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                            saveToLocal(imgBytes, cardId + user.getSuffix());
                            iv_head.setImageBitmap(bitMap);
                            mStatus = STATUS_SHOWING;
                            progressView.setVisibility(View.INVISIBLE);
                            showStartTime = System.currentTimeMillis();
                            showInt = SHOW_ING;
                        }
                    });
                }
            });
        }
    }

    private void saveToLocal(byte[] imgBytes, String name) {
        FileOutputStream fos = null;

        try {
            File file = new File("/sdcard/cardImage/" + name);
            //File file = new File(this.getExternalFilesDir("images").getAbsolutePath() + "/" + name);
            fos = new FileOutputStream(file);
            fos.write(imgBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class TimerThread extends Thread {
        @Override
        public void run() {
            while (running == 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //如果在显示，那么就在3秒钟后回到等待界面
                if (showInt == SHOW_ING) {
                    if (System.currentTimeMillis() - showStartTime > 3000) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                main_tv_init_left.setText("请刷卡");
                                reSetView(SHOW_STATUS_INIT, false);
                            }
                        });
                    }
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
        Log.i(TAG, "on Pause!!!!!!!!!!!!");

        MyApplication.setPermissionSelect(MyApplication.PERMISSION_SELECT_WAIT);
    }

}
