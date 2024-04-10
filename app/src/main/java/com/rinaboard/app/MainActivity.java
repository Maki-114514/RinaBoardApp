package com.rinaboard.app;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import static com.rinaboard.app.RinaBoardApp.SystemState.*;
import static com.rinaboard.app.PacketUtils.*;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainActivity extends AppCompatActivity {
    private TextView tv_connectState;
    private TextView tv_deviceName;
    private TextView tv_deviceType;
    private ImageButton bt_secondPage;
    private ImageButton bt_thirdPage;
    private ImageView bt_setting;
    private Switch sw_expMod;
    private Switch sw_videoMod;
    private Switch sw_recognitionMod;
    private Button bt_lastExp;
    private Button bt_nextExp;
    private Button bt_selectColor;
    private SeekBar sb_boardBrightness;
    private Button bt_resetColor;
    private Switch sw_lightState;
    private SeekBar sb_lightBrightness;
    private UDPInteraction udp1;
    private ConnectThread connectThread1;
    private RinaBoardApp.SystemState mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RinaBoardApp app = (RinaBoardApp) getApplication();
        //获取udp线程
        udp1 = app.getUdp1();
        //获取连接状态检测
        connectThread1 = app.getConnectThread1();
        connectThread1.setOnConnectChangedListener(new ConnectThread.OnConnectChangedListener() {
            @Override
            public void OnConnectChanged(ConnectState state) {
                switch (state) {
                    case CONNECTED://如果连接上了，那么就去向璃奈版发出请求并获得数据
                        System.out.println("Connect success");
                        System.out.println();
//                        try {
//                            Thread.sleep(500);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
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
                                updateView(state);
                            }
                        });
                        break;
                    case DISCONNECT:

                        //获取系统信息
                        app.setDeviceName("");
                        app.setDeviceType("");
                        app.setWifiSSID("");
                        app.setMode(ExpressionMode);

                        //颜色设置
                        app.setCustomColor(Color.parseColor("#FF1493"));
                        app.setBoardBrightness(75);

                        //灯条设置
                        app.setLightState(true);
                        app.setLightBrightness(127);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("警告");
                                builder.setMessage("璃奈板连接丢失");
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                                updateView(state);
                            }
                        });
                        System.out.println("View Update!");
                }
            }
        });

        if (connectThread1.getState() == Thread.State.RUNNABLE || connectThread1.isAlive()) {
            System.out.println("Connect checking thread has started");
        } else {
            connectThread1.start();
        }

        //UI的init需要放在最后，否则在建立需要用到udp通讯的监听时，会出现udp1 = null而报错重启程序的情况
        initView();
        updateView(connectThread1.getConnectState());
    }

    private void initView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();
        mode = app.getMode();
        //第二页（绘图或者视频播放页面）
        bt_secondPage = findViewById(R.id.bt_secondPage);
        bt_secondPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (app.getMode()) {
                    case ExpressionMode:
                        startActivity(new Intent(MainActivity.this, SecondActivity.class));
                        overridePendingTransition(0, 0);
                        break;
                    case VideoMode:
                        startActivity(new Intent(MainActivity.this, SecondVideoActivity.class));
                        overridePendingTransition(0, 0);
                        break;
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
                startActivity(new Intent(MainActivity.this, ThirdActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //设置界面切换
        bt_setting = findViewById(R.id.bt_setting);
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //连接状态显示文本
        tv_connectState = findViewById(R.id.tv_connectState);
        //设备名显示文本
        tv_deviceName = findViewById(R.id.tv_deviceName);
        //设备型号显示文本
        tv_deviceType = findViewById(R.id.tv_deviceType);

        //模式切换
        sw_expMod = findViewById(R.id.sw_expMod);
        sw_videoMod = findViewById(R.id.sw_videoMod);
        sw_recognitionMod = findViewById(R.id.sw_recognitionMod);
        //表情模式
        sw_expMod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sw_videoMod.setChecked(false);
                    sw_recognitionMod.setChecked(false);
                    app.setMode(ExpressionMode);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setSystemState(app.getMode()));
                        }
                    }).start();
                }
            }
        });
        //视频模式
        sw_videoMod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sw_expMod.setChecked(false);
                    sw_recognitionMod.setChecked(false);
                    app.setMode(VideoMode);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setSystemState(app.getMode()));
                        }
                    }).start();
                }
            }
        });
        //同步模式
        sw_recognitionMod.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sw_videoMod.setChecked(false);
                    sw_expMod.setChecked(false);
                    app.setMode(RecognitionMode);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setSystemState(app.getMode()));
                        }
                    }).start();
                }
            }
        });

        //上一个和下一个表情
        bt_lastExp = findViewById(R.id.bt_lastExp);
        bt_nextExp = findViewById(R.id.bt_nextExp);
        bt_lastExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LastExpression(udp1);
                    }
                }).start();
            }
        });
        bt_nextExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NextExpression(udp1);
                    }
                }).start();
            }
        });

        //颜色选择
        bt_selectColor = findViewById(R.id.bt_selectColor);
        bt_selectColor.setBackgroundColor(app.getCustomColor());
        bt_selectColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog();
            }
        });


        bt_resetColor = findViewById(R.id.bt_resetColor);
        bt_resetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.setCustomColor(Color.parseColor("#FF1493"));
                bt_selectColor.setBackgroundColor(Color.parseColor("#FF1493"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        udp1.send(setColorToBoard(app.getCustomColor()));
                    }
                }).start();
            }
        });

        sb_boardBrightness = findViewById(R.id.sb_boardBrightness);
        sb_boardBrightness.setProgress(app.getBoardBrightness());
        sb_boardBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                app.setBoardBrightness(progress);
                if (fromUser) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setBoardBrightnessToBoard(app.getBoardBrightness()));
                        }
                    }).start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //当停止改变亮度时，向璃奈版发出指令告知亮度已经停止改变，并保存在eeprom里
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        udp1.send(setBoardBrightnessOver());
                        System.out.println("Board brightness is saved to eeprom");
                    }
                }).start();
            }
        });

        sw_lightState = findViewById(R.id.sw_lightState);
        sw_lightState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isChecked) {
                            app.setLightState(true);
                            udp1.send(setLightStateToBoard(true));
                        } else {
                            app.setLightState(false);
                            udp1.send(setLightStateToBoard(false));
                        }
                    }
                }).start();
            }
        });

        sb_lightBrightness = findViewById(R.id.sb_lightBrightness);
        sb_lightBrightness.setProgress(app.getLightBrightness());
        sb_lightBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                app.setLightBrightness(progress);
                if (fromUser) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            udp1.send(setLightBrightnessToBoard(app.getLightBrightness()));
                        }
                    }).start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //当停止改变亮度时，向璃奈版发出指令告知亮度已经停止改变，并保存在eeprom里
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        udp1.send(setLightBrightnessOver());
                        System.out.println("Light brightness is saved to eeprom");
                    }
                }).start();
            }
        });
    }

    private void updateView(ConnectState state) {
        RinaBoardApp app = (RinaBoardApp) getApplication();
        mode = app.getMode();

        bt_selectColor.setBackgroundColor(app.getCustomColor());
        sb_boardBrightness.setProgress(app.getBoardBrightness());
        sw_lightState.setChecked(app.getLightState());
        sb_lightBrightness.setProgress(app.getLightBrightness());

        switch (mode) {
            case ExpressionMode:
                sw_expMod.setChecked(true);
                sw_videoMod.setChecked(false);
                sw_recognitionMod.setChecked(false);
                break;
            case VideoMode:
                sw_expMod.setChecked(false);
                sw_videoMod.setChecked(true);
                sw_recognitionMod.setChecked(false);
                break;
            case RecognitionMode:
                sw_expMod.setChecked(false);
                sw_videoMod.setChecked(false);
                sw_recognitionMod.setChecked(true);
                break;
        }

        if (state == ConnectState.CONNECTED) {
            tv_connectState.setText("已连接");
            tv_deviceName.setText(app.getDeviceName());
            tv_deviceType.setText("型号: RinaBoard " + app.getDeviceType());

            enableWidget();
        } else if (state == ConnectState.DISCONNECT) {
            tv_connectState.setText("未连接");
            tv_deviceName.setText("");
            tv_deviceType.setText("");
            disableWidget();
        }


    }

    private void enableWidget() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        sw_expMod.setEnabled(true);
        sw_videoMod.setEnabled(true);
        if (app.getDeviceType().equals("V1")) {
            sw_recognitionMod.setEnabled(false);
        } else if (app.getDeviceType().equals("V2") | app.getDeviceType().equals("V1.5")) {
            sw_recognitionMod.setEnabled(true);
        }
        bt_lastExp.setEnabled(true);
        bt_nextExp.setEnabled(true);
        bt_selectColor.setEnabled(true);
        sb_boardBrightness.setEnabled(true);
        bt_resetColor.setEnabled(true);
        sw_lightState.setEnabled(true);
        sb_lightBrightness.setEnabled(true);
    }

    private void disableWidget() {
        sw_expMod.setEnabled(false);
        sw_videoMod.setEnabled(false);
        sw_recognitionMod.setEnabled(false);
        bt_lastExp.setEnabled(false);
        bt_nextExp.setEnabled(false);
        bt_selectColor.setEnabled(false);
        sb_boardBrightness.setEnabled(false);
        bt_resetColor.setEnabled(false);
        sw_lightState.setEnabled(false);
        sb_lightBrightness.setEnabled(false);
    }

    private void showColorPickerDialog() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("选择板的颜色")//设置标题
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)//设置花型
                //.wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)//设置圆形也可以
                .density(12)//设置距离
                .setPositiveButton("确定", new ColorPickerClickListener() {//设置确定事件
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        //修改全局变量中的颜色变量
                        RinaBoardApp app = (RinaBoardApp) getApplication();
                        app.setCustomColor(selectedColor);
                        bt_selectColor.setBackgroundColor(app.getCustomColor());
                        app.setCustomColor(app.getCustomColor());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                byte[] data = setColorToBoard(app.getCustomColor());
                                udp1.send(data);
                            }
                        }).start();
                    }
                })

                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//设置取消事件
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }
}
