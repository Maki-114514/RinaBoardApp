package com.rinaboard.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.*;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.PacketUtils.GetDamageWords;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class SecondDamageActivity extends AppCompatActivity {
    private ImageButton bt_mainPage;
    private ImageButton bt_thirdPage;
    private ImageView bt_setting;
    private ImageView iv_batteryDisplay;
    private Switch sw_damageLightState;
    private Button bt_updateWords;
    private EditText et_editWords;
    private UDPInteraction udp1;
    private ConnectThread connectThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_damage);

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

                        if (udp1 != null) {
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
                                    float voltage = app.getBatteryVoltage();
                                    if (voltage >= 3.9f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_4);
                                    } else if (voltage >= 3.79f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_3);
                                    } else if (voltage >= 3.65f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_2);
                                    } else if (voltage >= 3.5f) {
                                        iv_batteryDisplay.setImageResource(R.drawable.battery_1);
                                    } else {
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
                        app.setBoardBrightness(75);

                        //灯条设置
                        app.setLightState(true);
                        app.setLightBrightness(127);

                        app.setBatteryVoltage(0.0f);

                        //光害相关设置
                        app.setDamageLightState(false);
                        app.setDamageWords("");

                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SecondDamageActivity.this);
                                builder.setTitle("警告");
                                builder.setMessage("璃奈板连接丢失");
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        // 对话框关闭时启动 MainActivity
                                        startActivity(new Intent(SecondDamageActivity.this, MainActivity.class));
                                        overridePendingTransition(0, 0);
                                    }
                                });
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });*/
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
                        if (voltage >= 3.9f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_4);
                        } else if (voltage >= 3.79f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_3);
                        } else if (voltage >= 3.65f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_2);
                        } else if (voltage >= 3.5f) {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_1);
                        } else {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
                        }
                    }
                });
            }
        });

        initView();
        updateView();
    }

    private void initView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        bt_mainPage = findViewById(R.id.bt_mainPage);
        bt_mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainPage Button is clicked!");
                startActivity(new Intent(SecondDamageActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //第三页按钮
        bt_thirdPage = findViewById(R.id.bt_thirdPage);
        bt_thirdPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("thirdPage Button is clicked!");
                startActivity(new Intent(SecondDamageActivity.this, ThirdActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //设置页面
        bt_setting = findViewById(R.id.bt_setting);
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondDamageActivity.this, SettingActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //电量显示图标
        iv_batteryDisplay = findViewById(R.id.iv_batteryDisplay);
        float voltage = app.getBatteryVoltage();
        if (voltage >= 3.9f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_4);
        } else if (voltage >= 3.79f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_3);
        } else if (voltage >= 3.65f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_2);
        } else if (voltage >= 3.5f) {
            iv_batteryDisplay.setImageResource(R.drawable.battery_1);
        } else {
            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
        }

        sw_damageLightState = findViewById(R.id.sw_damageLightState);
        sw_damageLightState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                app.setDamageLightState(isChecked);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isChecked) {
                            app.setDamageLightState(true);
                            udp1.send(setDamageLightStateToBoard(true));
                        } else {
                            app.setDamageLightState(false);
                            udp1.send(setDamageLightStateToBoard(false));
                        }
                    }
                }).start();
            }
        });

        et_editWords = findViewById(R.id.et_editWords);
        // 创建一个InputFilter
        // 设置输入类型为英文
        et_editWords.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        et_editWords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                app.setDamageWords(s.toString());
            }
        });

        bt_updateWords = findViewById(R.id.bt_updateWords);
        bt_updateWords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String words = et_editWords.getText().toString();
                if (!words.isEmpty()) {
                    app.setDamageWords(words);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setDamageWordsToBoard(words));
                        }
                    }).start();
                }
            }
        });
    }

    private void updateView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        sw_damageLightState.setChecked(app.getDamageLightState());
        et_editWords.setText(app.getDamageWords());
    }
}