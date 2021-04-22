package com.dataexpo.nfcsample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.nfcsample.listener.OnItemClickListener;
import com.dataexpo.nfcsample.pojo.MsgBean;
import com.dataexpo.nfcsample.pojo.PermissionBean;
import com.dataexpo.nfcsample.pojo.PermissionList;
import com.dataexpo.nfcsample.pojo.Permissions;
import com.dataexpo.nfcsample.pojo.User;
import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.Utils;
import com.dataexpo.nfcsample.utils.net.HttpCallback;
import com.dataexpo.nfcsample.utils.net.HttpService;
import com.dataexpo.nfcsample.utils.net.URLs;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class MainSelectorActiivty extends BascActivity implements OnItemClickListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();
    private Context mContext;
    private CodeRecordAdapter dateAdapter;
    private RecyclerView recyclerView;
    private List<Permissions> permissions;
    private CircularProgressView progressView;
    private TextView tv_msg;
    private Button btn_msg;
    private TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        mContext = this;

        initView();
        initSelectorValue();
    }

    private void initSelectorValue() {
        tv_version.setText(Utils.getVersionName(mContext));
        goQuery();

        dateAdapter = new CodeRecordAdapter();
        recyclerView.setAdapter(dateAdapter);

        dateAdapter.setItemClickListener(this);
    }

    private void goQuery() {
        tv_msg.setVisibility(View.GONE);
        btn_msg.setVisibility(View.GONE);
        final HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("expoId", 10000 + "");

        HttpService.getWithParams(mContext, URLs.queryAccessGroup, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_msg.setVisibility(View.VISIBLE);
                        btn_msg.setVisibility(View.VISIBLE);
                        progressView.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onResponse(final String response, int id) {
//                final MsgBean msgBean = new Gson().fromJson(response, MsgBean.class);
                Log.i(TAG, "online check expoid response: " + response);
                final PermissionBean result = new Gson().fromJson(response, PermissionBean.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressView.setVisibility(View.INVISIBLE);
                        if (result != null && result.data != null && result.data.size() > 0) {
                            permissions = result.data;
                            dateAdapter.setData(permissions);
                            dateAdapter.notifyDataSetChanged();
                        } else {
                            tv_msg.setVisibility(View.VISIBLE);
                            btn_msg.setVisibility(View.VISIBLE);
                            Toast.makeText(mContext, "项目查询门禁列表失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void initView() {
        tv_msg = findViewById(R.id.tv_msg);
        tv_msg.setOnClickListener(this);
        btn_msg = findViewById(R.id.btn_msg);
        btn_msg.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_scan_record);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        progressView.setVisibility(View.VISIBLE);
        tv_version = findViewById(R.id.tv_version);
    }

    @Override
    public void onItemClick(View view, int position) {
        MyApplication.setPermissionSelect(MyApplication.PERMISSION_SELECT_OK);
        Log.i(TAG, "select position: " + position);
        Permissions p = permissions.get(position);
        Intent intent = new Intent();

        Bundle bundle = new Bundle();
        bundle.putSerializable("pomission", p);
        intent.putExtras(bundle);
        intent.setClass(mContext, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_msg:
            case R.id.btn_msg:
                goQuery();
                break;
            default:
        }
    }

    private static class PermissionHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private ImageView iv_image_show;
        private TextView tv_permission;

        public PermissionHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            iv_image_show = itemView.findViewById(R.id.iv_permission_show);
            tv_permission = itemView.findViewById(R.id.tv_item_permission);
        }
    }

    public class CodeRecordAdapter extends RecyclerView.Adapter<PermissionHolder> implements View.OnClickListener {
        private List<Permissions> mList;
        private OnItemClickListener mItemClickListener;

        public void setData(List<Permissions> list) {
            mList = list;
        }

        @Override
        public void onClick(View v) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(v, (Integer) v.getTag());
//            }
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public PermissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_record, parent, false);
            PermissionHolder viewHolder = new PermissionHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionHolder holder, final int position) {
            holder.itemView.setTag(position);
            // 添加数据
            //holder.iv_image_show.setVisibility(View.INVISIBLE);
            holder.tv_permission.setText(mList.get(position).getNames());

            holder.iv_image_show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });
            holder.tv_permission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }
}
