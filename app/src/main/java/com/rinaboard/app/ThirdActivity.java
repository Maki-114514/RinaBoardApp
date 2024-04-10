package com.rinaboard.app;


import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.PacketUtils.GetLightBrightness;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class ThirdActivity extends AppCompatActivity {
    private ImageButton bt_mainPage;
    private ImageButton bt_secondPage;
    private ImageView bt_setting;
    private UDPInteraction udp1;
    private ConnectThread connectThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        RinaBoardApp app = (RinaBoardApp) getApplication();
        //获取udp线程
        udp1 = app.getUdp1();
        //获取连接状态检测
        connectThread1 = app.getConnectThread1();
        connectThread1.setOnConnectChangedListener(new ConnectThread.OnConnectChangedListener() {
            @Override
            public void onConnectChanged(ConnectState state) {
                switch (state) {
                    case CONNECTED://如果连接上了，那么就去向璃奈版发出请求并获得数据
                        System.out.println("Connect success");
                        System.out.println();
//                        try {
//                            Thread.sleep(500);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        if(udp1 != null){
                            //获取系统信息
                            app.setDeviceName(GetDeviceName(udp1));
                            app.setDeviceType(GetDeviceType(udp1));
                            app.setWifiSSID(GetWifiSSID(udp1));
                            app.setMode(GetSystemState(udp1));

                            //颜色设置
                            app.setCustomColor(GetColor(udp1));
                            app.setBoardBrightness(GetBoardBrightness(udp1));

                            //灯条设置
                            app.setLightState(GetLightState(udp1));
                            app.setLightBrightness(GetLightBrightness(udp1));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                        }
                        break;
                    case DISCONNECT:

                        //获取系统信息
                        app.setDeviceName("");
                        app.setDeviceType("");
                        app.setWifiSSID("");
                        app.setMode(ExpressionMode);

                        //颜色设置
                        app.setCustomColor(android.graphics.Color.parseColor("#FF1493"));
                        app.setBoardBrightness(100);

                        //灯条设置
                        app.setLightState(true);
                        app.setLightBrightness(100);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ThirdActivity.this);
                                builder.setTitle("警告");
                                builder.setMessage("璃奈板连接丢失");
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });
                        System.out.println("View Update!");
                }
            }
        });
        //UI的init需要放在最后，否则在建立需要用到udp通讯的监听时，会出现udp1 = null而报错重启程序的情况
        initView();
    }

    private void initView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        //主页面按钮
        bt_mainPage = findViewById(R.id.bt_mainPage);
        bt_mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThirdActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //第二页按钮
        bt_secondPage = findViewById(R.id.bt_secondPage);
        bt_secondPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (app.getMode()){
                    case ExpressionMode:
                        startActivity(new Intent(ThirdActivity.this, SecondActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case VideoMode:
                        startActivity(new Intent(ThirdActivity.this, SecondVideoActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case RecognitionMode:
                        break;
                }
            }
        });
        //设置页面
        bt_setting = findViewById(R.id.bt_setting);
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ThirdActivity.this, SettingActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }
}