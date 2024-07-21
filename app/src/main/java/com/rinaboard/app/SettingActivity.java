package com.rinaboard.app;


import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class SettingActivity extends AppCompatActivity {

    private ImageButton bt_mainPage;
    private ImageButton bt_secondPage;
    private ImageButton bt_thirdPage;
    private ImageView iv_batteryDisplay;
    private TextView tv_batteryVoltage;
    private EditText et_deviceName;
    private EditText et_wifiSSID;
    private EditText et_wifiPassword;
    private Button bt_saveReboot;
    UDPInteraction udp1;
    ConnectThread connectThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
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

                            app.setBatteryVoltage(connectThread1.getVoltage());

                            //光害相关设置
                            app.setDamageLightState(GetDamageLightState(udp1));
                            app.setDamageWords(GetDamageWords(udp1));

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bt_saveReboot.setEnabled(true);

                                    float voltage = app.getBatteryVoltage();
                                    String formattedString = String.format("%.1f", voltage);
                                    tv_batteryVoltage.setText(formattedString + "V");
                                    tv_batteryVoltage.setText(formattedString + "V");
                                    if (voltage >= 3.9f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_4);
                                    } else if (voltage >= 3.79f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_3);
                                    } else if (voltage >= 3.65f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_2);
                                    } else if (voltage >= 3.5f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_1);
                                    }else {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_0);
                                    }
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

                        app.setBatteryVoltage(0.0f);

                        //光害相关设置
                        app.setDamageLightState(false);
                        app.setDamageWords("");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bt_saveReboot.setEnabled(false);

                                float voltage = app.getBatteryVoltage();
                                String formattedString = String.format("%.1f", voltage);
                                tv_batteryVoltage.setText(formattedString + "V");
                                tv_batteryVoltage.setText(formattedString + "V");
                                if (voltage >= 3.9f) {
                                    iv_batteryDisplay.setImageResource(R.drawable.battery_4);
                                } else if (voltage >= 3.79f) {
                                    iv_batteryDisplay.setImageResource(R.drawable.battery_3);
                                } else if (voltage >= 3.65f) {
                                    iv_batteryDisplay.setImageResource(R.drawable.battery_2);
                                } else if (voltage >= 3.5f) {
                                    iv_batteryDisplay.setImageResource(R.drawable.battery_1);
                                }else {
                                    iv_batteryDisplay.setImageResource(R.drawable.battery_0);
                                }

                                /*AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                                builder.setTitle("警告");
                                builder.setMessage("璃奈板连接丢失");
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();*/
                            }
                        });
                        System.out.println("View Update!");
                }
            }
        });
        connectThread1.setOnBatteryVoltageChangedListener(new ConnectThread.OnBatteryVoltageChangedListener() {
            @Override
            public void onBatteryVoltageChanged(float voltage) {
                app.setBatteryVoltage(voltage);
                System.out.println("The Battery voltage is " + voltage + "V");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String formattedString = String.format("%.1f", voltage);
                        tv_batteryVoltage.setText(formattedString + "V");
                        if (voltage >= 3.9f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_4);
                        } else if (voltage >= 3.79f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_3);
                        } else if (voltage >= 3.65f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_2);
                        } else if (voltage >= 3.5f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_1);
                        }else {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
                        }
                    }
                });
            }
        });

        //UI的init需要放在最后，否则在建立需要用到udp通讯的监听时，会出现udp1 = null而报错重启程序的情况
        initView();
    }

    private void initView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        bt_mainPage = findViewById(R.id.bt_mainPage);

        bt_mainPage = findViewById(R.id.bt_mainPage);
        bt_mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //第二页（绘图或者视频播放页面）
        bt_secondPage = findViewById(R.id.bt_secondPage);
        bt_secondPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (app.getMode()) {
                    case ExpressionMode:
                        startActivity(new Intent(SettingActivity.this, SecondActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case VideoMode:
                        startActivity(new Intent(SettingActivity.this, SecondVideoActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case DamageMode:
                        startActivity(new Intent(SettingActivity.this, SecondDamageActivity.class));
                        overridePendingTransition(0, 0);
                    case RecognitionMode:
                        break;
                }
            }
        });

        //开机动画设置页面
        bt_thirdPage = findViewById(R.id.bt_thirdPage);
        bt_thirdPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this, ThirdActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        iv_batteryDisplay = findViewById(R.id.iv_batteryDisplay);
        tv_batteryVoltage = findViewById(R.id.tv_batteryVoltage);
        float voltage = app.getBatteryVoltage();
        String formattedString = String.format("%.1f", voltage);
        tv_batteryVoltage.setText(formattedString + "V");
        tv_batteryVoltage.setText(formattedString + "V");
        if (voltage >= 3.9f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_4);
        } else if (voltage >= 3.79f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_3);
        } else if (voltage >= 3.65f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_2);
        } else if (voltage >= 3.5f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_1);
        }else {
            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
        }

        et_deviceName = findViewById(R.id.et_deviceName);
        et_wifiSSID = findViewById(R.id.et_wifiSSID);
        et_wifiPassword = findViewById(R.id.et_wifiPassword);

        bt_saveReboot = findViewById(R.id.bt_saveReboot);
        bt_saveReboot.setEnabled(connectThread1.getConnectState() != ConnectState.DISCONNECT);

        bt_saveReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!et_deviceName.getText().toString().isEmpty()){
                            udp1.send(setDeviceNameToBoard(et_deviceName.getText().toString()));
                        }
                        if(!et_wifiSSID.getText().toString().isEmpty()){
                            udp1.send(setWifiSSIDToBoard(et_wifiSSID.getText().toString()));
                        }
                        if(!et_wifiPassword.getText().toString().isEmpty()){
                            udp1.send(setWifiPasswordToBoard(et_wifiPassword.getText().toString()));
                        }
                        ReBootBoard(udp1);
                    }
                }).start();
            }
        });
    }
}