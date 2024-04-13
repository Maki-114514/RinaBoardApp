package com.rinaboard.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class SecondActivity extends AppCompatActivity {
    private ImageButton bt_mainPage;
    private ImageButton bt_thirdPage;
    private ImageView bt_setting;
    private ImageView iv_batteryDisplay;
    private Button bt_showExp;
    private Button bt_saveExp;
    private Button bt_existExp;
    private Button bt_showBoardExp;
    private Button bt_clearExp;
    private Button bt_fillExp;
    private PixelDrawingView pixelDrawingView;
    private ImageView[] iv_eyesBitmaps = new ImageView[PreExpression.Eyes.num];
    private ImageView[] iv_cheekBitmaps = new ImageView[PreExpression.Cheek.num];
    private ImageView[] iv_mouthBitmaps = new ImageView[PreExpression.Mouth.num];
    private Button[] bt_clears = new Button[3];
    private UDPInteraction udp1;
    private ConnectThread connectThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

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

                            //重绘UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateView(state);
                                }
                            });
                            System.out.println("View Update!");
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
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
        connectThread1.setOnBatteryVoltageChangedListener(new ConnectThread.OnBatteryVoltageChangedListener() {
            @Override
            public void onBatteryVoltageChanged(float voltage) {
                app.setBatteryVoltage(voltage);
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

        //主页面按钮
        bt_mainPage = findViewById(R.id.bt_mainPage);
        bt_mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainPage Button is clicked!");
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //第三页按钮
        bt_thirdPage = findViewById(R.id.bt_thirdPage);
        bt_thirdPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("thirdPage Button is clicked!");
                startActivity(new Intent(SecondActivity.this, ThirdActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //设置页面
        bt_setting = findViewById(R.id.bt_setting);
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondActivity.this, SettingActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //电量显示图标
        iv_batteryDisplay = findViewById(R.id.iv_batteryDisplay);

        //像素绘图板
        pixelDrawingView = findViewById(R.id.pixelDrawingView);
        pixelDrawingView.setBackgroundColor(app.getCustomColor());
        pixelDrawingView.setOnBitmapUpdateListener(new PixelDrawingView.OnBitmapUpdateListener() {//绘图板发生变动时的外部回调
            @Override
            public void onBitmapUpdated(byte[] editBitmap) {
                app.setEditBitmap(editBitmap);
            }
        });
        pixelDrawingView.drawBitmap(app.getEditBitmap());

        //表情显示按钮
        bt_showExp = findViewById(R.id.bt_showExp);
        bt_showExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.updateBitmap();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] data = setBitmapToBoard(app.getEditBitmap());
                        udp1.send(data);
                    }
                }).start();
            }
        });

        //获取板子表情按钮
        bt_showBoardExp = findViewById(R.id.bt_showBoardExp);
        bt_showBoardExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        app.setEditBitmap(GetBitmap(udp1));
                        pixelDrawingView.drawBitmap(app.getEditBitmap());
                    }
                }).start();
            }
        });

        //保存表情按钮
        bt_saveExp = findViewById(R.id.bt_saveExp);
        bt_saveExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSaveBitmapDialog();
            }
        });

        //打开已有表情的列表
        bt_existExp = findViewById(R.id.bt_existExp);
        bt_existExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExpressionDialog(SecondActivity.this);
            }
        });

        //清除表情按钮
        bt_clearExp = findViewById(R.id.bt_clearExp);
        bt_clearExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.turnOffAllButtons();
            }
        });

        //填满图像按钮
        bt_fillExp = findViewById(R.id.bt_fillExp);
        bt_fillExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.turnOnAllButtons();
            }
        });

        iv_eyesBitmaps[0] = findViewById(R.id.iv_eyesBitmap1);
        iv_eyesBitmaps[1] = findViewById(R.id.iv_eyesBitmap2);
        iv_eyesBitmaps[2] = findViewById(R.id.iv_eyesBitmap3);
        iv_eyesBitmaps[3] = findViewById(R.id.iv_eyesBitmap4);
        iv_eyesBitmaps[4] = findViewById(R.id.iv_eyesBitmap5);
        iv_eyesBitmaps[5] = findViewById(R.id.iv_eyesBitmap6);
        iv_eyesBitmaps[6] = findViewById(R.id.iv_eyesBitmap7);
        iv_eyesBitmaps[7] = findViewById(R.id.iv_eyesBitmap8);
        for (int i = 0; i < PreExpression.Eyes.num; i++) {
            iv_eyesBitmaps[i].setImageBitmap(convertByteArrayToBitmap(PreExpression.Eyes.bitmaps[i], 18, 7, app.getCustomColor()));
            int finalI = i;
            iv_eyesBitmaps[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pixelDrawingView.changeEyes(PreExpression.Eyes.bitmaps[finalI]);
                }
            });
        }

        iv_cheekBitmaps[0] = findViewById(R.id.iv_cheekBitmap1);
        iv_cheekBitmaps[1] = findViewById(R.id.iv_cheekBitmap2);
        iv_cheekBitmaps[2] = findViewById(R.id.iv_cheekBitmap3);
        iv_cheekBitmaps[3] = findViewById(R.id.iv_cheekBitmap4);
        iv_cheekBitmaps[4] = findViewById(R.id.iv_cheekBitmap5);
        for (int i = 0; i < PreExpression.Cheek.num; i++) {
            iv_cheekBitmaps[i].setImageBitmap(convertByteArrayToBitmap(PreExpression.Cheek.bitmaps[i], 18, 3, app.getCustomColor()));
            int finalI = i;
            iv_cheekBitmaps[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pixelDrawingView.changeCheek(PreExpression.Cheek.bitmaps[finalI]);
                }
            });
        }

        iv_mouthBitmaps[0] = findViewById(R.id.iv_mouthBitmap1);
        iv_mouthBitmaps[1] = findViewById(R.id.iv_mouthBitmap2);
        iv_mouthBitmaps[2] = findViewById(R.id.iv_mouthBitmap3);
        iv_mouthBitmaps[3] = findViewById(R.id.iv_mouthBitmap4);
        iv_mouthBitmaps[4] = findViewById(R.id.iv_mouthBitmap5);
        iv_mouthBitmaps[5] = findViewById(R.id.iv_mouthBitmap6);
        iv_mouthBitmaps[6] = findViewById(R.id.iv_mouthBitmap7);
        iv_mouthBitmaps[7] = findViewById(R.id.iv_mouthBitmap8);
        iv_mouthBitmaps[8] = findViewById(R.id.iv_mouthBitmap9);
        iv_mouthBitmaps[9] = findViewById(R.id.iv_mouthBitmap10);
        iv_mouthBitmaps[10] = findViewById(R.id.iv_mouthBitmap11);
        iv_mouthBitmaps[11] = findViewById(R.id.iv_mouthBitmap12);
        iv_mouthBitmaps[12] = findViewById(R.id.iv_mouthBitmap13);
        for (int i = 0; i < PreExpression.Mouth.num; i++) {
            iv_mouthBitmaps[i].setImageBitmap(convertByteArrayToBitmap(PreExpression.Mouth.bitmaps[i], 18, 6, app.getCustomColor()));
            int finalI = i;
            iv_mouthBitmaps[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pixelDrawingView.changeMouth(PreExpression.Mouth.bitmaps[finalI]);
                }
            });
        }


        bt_clears[0] = findViewById(R.id.bt_clearEyes);
        bt_clears[1] = findViewById(R.id.bt_clearCheek);
        bt_clears[2] = findViewById(R.id.bt_clearMouth);
        bt_clears[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.changeEyes(PreExpression.Eyes.clearBitmap);
            }
        });
        bt_clears[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.changeCheek(PreExpression.Cheek.clearBitmap);
            }
        });
        bt_clears[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pixelDrawingView.changeMouth(PreExpression.Mouth.clearBitmap);
            }
        });
    }

    private void updateView(ConnectState state) {
        RinaBoardApp app = (RinaBoardApp) getApplication();

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

        pixelDrawingView.setBackgroundColor(app.getCustomColor());

        if (state == ConnectState.CONNECTED) {
            enableWidget();
        } else if (state == ConnectState.DISCONNECT) {
            disableWidget();
        }
    }

    private void enableWidget() {
        bt_showExp.setEnabled(true);
        bt_showBoardExp.setEnabled(true);
    }

    private void disableWidget() {
        bt_showExp.setEnabled(false);
        bt_showBoardExp.setEnabled(false);
    }

    private void showSaveBitmapDialog() {
        RinaBoardApp app = (RinaBoardApp) getApplication();
        final AlertDialog[] dialog = new AlertDialog[1];

        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("给你的表情起个名字(不允许特殊字符):");

        // 创建一个 EditText，并添加到 AlertDialog 中
        final EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(layoutParams);
        builder.setView(editText);

        // 自定义布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button bt_saveToBoard = new Button(this);
        bt_saveToBoard.setText("保存到璃奈板");
        bt_saveToBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bitmapName = editText.getText().toString();
                if (!bitmapName.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SaveBitmapToBoard(udp1, bitmapName);
                        }
                    }).start();
                    dialog[0].dismiss();
                }

            }
        });
        layout.addView(bt_saveToBoard);
        bt_saveToBoard.setEnabled(connectThread1.getConnectState() == ConnectState.CONNECTED);

        Button bt_saveToApp = new Button(this);
        bt_saveToApp.setText("保存到手机");
        bt_saveToApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bitmapName = editText.getText().toString();
                if (!bitmapName.isEmpty()) {
                    ExpressionFileManager.addKeyValuePair(getApplicationContext(), bitmapName, app.getEditBitmap());
                    dialog[0].dismiss();
                }
            }
        });
        layout.addView(bt_saveToApp);

        // 将EditText和自定义按钮布局添加到对话框中
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(editText);
        dialogLayout.addView(layout);
        builder.setView(dialogLayout);

        // 创建并显示对话框
        dialog[0] = builder.create();
        dialog[0].show();
    }

    private void showExpressionDialog(Context context) {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        Map<String, byte[]>[] map = new Map[]{ExpressionFileManager.getMapFromJson(getApplicationContext())};

        final AlertDialog[] dialog = new AlertDialog[1];

        // 加载自定义布局文件
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.expressionselect_dialog_layout, null);

        // 获取两个ListView
        ListView lv_boardExp = view.findViewById(R.id.lv_boardExp);
        ListView lv_localExp = view.findViewById(R.id.lv_localExp);


        // 准备数据
        List<String> expOnBoard = new ArrayList<>();
        List<String> expOnLocal = new ArrayList<>();

        String[] keys = ExpressionFileManager.getAllKeysFromMap(map[0]);
        if (keys != null) {
            expOnLocal.addAll(Arrays.asList(keys));
        }

        // 创建适配器
        MyAdapter adapter1 = new MyAdapter(this, expOnBoard);
        MyAdapter adapter2 = new MyAdapter(this, expOnLocal);

        // 设置适配器
        lv_boardExp.setAdapter(adapter1);
        lv_localExp.setAdapter(adapter2);

        lv_boardExp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter1.setSelectedPosition(position);
                adapter2.setSelectedPosition(-1); // 取消 listView2 中的选中状态
            }
        });

        lv_localExp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter2.setSelectedPosition(position);
                adapter1.setSelectedPosition(-1); // 取消 listView1 中的选中状态
            }
        });

        Button bt_show = view.findViewById(R.id.bt_show);
        Button bt_delete = view.findViewById(R.id.bt_delete);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);

        bt_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition1 = adapter1.getSelectedPosition();
                int selectedPosition2 = adapter2.getSelectedPosition();
                if (selectedPosition1 != -1) {
                    String selectedItem = adapter1.getItem(selectedPosition1);
                    if (selectedItem != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ChangeBitmapOnBoard(udp1, selectedItem);
                                app.setEditBitmap(GetBitmap(udp1));
                                pixelDrawingView.drawBitmap(app.getEditBitmap());
                            }
                        }).start();
                    }
                } else if (selectedPosition2 != -1) {
                    String selectedItem = adapter2.getItem(selectedPosition2);
                    if (selectedItem != null & map[0] != null) {
                        byte[] bitmap = map[0].get(selectedItem);
                        if (bitmap != null) {
                            app.setEditBitmap(bitmap);
                            pixelDrawingView.drawBitmap(bitmap);
                        }
                    }
                }
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition1 = adapter1.getSelectedPosition();
                int selectedPosition2 = adapter2.getSelectedPosition();
                if (selectedPosition1 != -1)//如果左列表不是-1，则左列表被选中
                {
                    String selectedItem = adapter1.getItem(selectedPosition1);
                    if (selectedItem != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DeleteBitmapOnBoard(udp1, selectedItem);
                                System.out.println("Delete bitmap named " + selectedItem);
                            }
                        }).start();
                    }
                } else if (selectedPosition2 != -1) {
                    String selectedItem = adapter2.getItem(selectedPosition2);
                    if (selectedItem != null) {
                        ExpressionFileManager.removeKey(getApplicationContext(), selectedItem);
                        map[0] = ExpressionFileManager.getMapFromJson(getApplicationContext());
                    }
                }
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });

        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        dialog[0] = builder.create();
        dialog[0].show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] list = getExpressionList(udp1);
                if (list != null) {
                    List<String> newExpOnBoard = new ArrayList<String>(Arrays.asList(list)); // 获取更新后的数据
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            expOnBoard.clear(); // 清空原始数据
                            expOnBoard.addAll(newExpOnBoard); // 添加更新后的数据
                            adapter1.notifyDataSetChanged(); // 通知适配器数据已更改，以便更新ListView
                        }
                    });
                }
            }
        }).start();

    }

    public Bitmap convertByteArrayToBitmap(byte[] byteArray, int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int rowBytes = (width + 7) / 8;

        int byteIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int bitIndex = x % 8;
                int colorIndex = byteIndex + (x / 8);
                int bit = (byteArray[colorIndex] & (1 << (7 - bitIndex))) == 0 ? Color.WHITE : color;
                bitmap.setPixel(x, y, bit);
            }
            byteIndex += rowBytes;
        }
        return Bitmap.createScaledBitmap(bitmap, width * 25, height * 25, false);
    }
}

class MyAdapter extends ArrayAdapter<String> {
    private int selectedPosition = -1; // 记录当前选中的位置

    public MyAdapter(Context context, List<String> data) {
        super(context, 0, data);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        String item = getItem(position);
        TextView textView = itemView.findViewById(android.R.id.text1);
        textView.setText(item);

        // 设置选中状态的背景颜色
        if (position == selectedPosition) {
            itemView.setBackgroundColor(android.graphics.Color.LTGRAY);
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT); // 默认透明背景
        }

        return itemView;
    }

    // 设置选中位置
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // 刷新列表以更新视图
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}