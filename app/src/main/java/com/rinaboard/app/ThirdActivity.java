package com.rinaboard.app;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.*;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.PacketUtils.GetLightBrightness;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class ThirdActivity extends AppCompatActivity {
    private ImageButton bt_mainPage;
    private ImageButton bt_secondPage;
    private ImageView bt_setting;
    private ImageView iv_batteryDisplay;
    private Button bt_appendExpression;
    private Button bt_appendMicroSecond;
    private Button bt_appendSecond;
    private Button bt_delete;
    private Button bt_existList;
    private Button bt_saveList;
    private Button bt_updateList;
    private UDPInteraction udp1;
    private ConnectThread connectThread1;
    private StartAnimeView startAnimeView;
    LinkedList<StartAnime> startAnimeLinkedList;

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

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateView(state);
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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ThirdActivity.this);
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
        startAnimeLinkedList = app.getStartAnimeLinkedList();

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
                switch (app.getMode()) {
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

        //电量显示图标
        iv_batteryDisplay = findViewById(R.id.iv_batteryDisplay);

        //显示开机动画顺序的控件
        startAnimeView = findViewById(R.id.startAnimeView);
        startAnimeView.setOnStartAnimeClickedListener(new StartAnimeView.OnStartAnimeClickedListener() {
            @Override
            public void onStartAnimeClicked(int position) {
                showChangeStartAnimeDialog(ThirdActivity.this, position);
            }
        });

        bt_appendExpression = findViewById(R.id.bt_appendExpression);
        bt_appendExpression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddExpressionDialog(ThirdActivity.this);
            }
        });

        bt_appendMicroSecond = findViewById(R.id.bt_appendMicroSecond);
        bt_appendMicroSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDelayDialog("毫秒");
            }
        });
        bt_appendSecond = findViewById(R.id.bt_appendSecond);
        bt_appendSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDelayDialog("秒");
            }
        });
        bt_delete = findViewById(R.id.bt_delete);
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!startAnimeLinkedList.isEmpty()) {
                    startAnimeLinkedList.removeLast();
                    startAnimeView.deleteStartAnime();
                }
            }
        });

        bt_updateList = findViewById(R.id.bt_updateList);
        bt_updateList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!startAnimeLinkedList.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SetStartBitmap(udp1, startAnimeLinkedList);
                            ReBootBoard(udp1);
                        }
                    }).start();
                }
            }
        });

        bt_existList = findViewById(R.id.bt_existList);
        bt_existList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExistStartAnimeDialog(ThirdActivity.this);
            }
        });

        bt_saveList = findViewById(R.id.bt_saveList);
        bt_saveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!startAnimeLinkedList.isEmpty()){
                    showSaveStartAnimeDialog(ThirdActivity.this);
                }
            }
        });

        updateView(connectThread1.getConnectState());
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

        startAnimeView.setStartAnimeView(startAnimeLinkedList);

        if (state == ConnectState.CONNECTED) {
            enableWidget();
        } else {
            disableWidget();
        }
    }

    private void enableWidget() {
        bt_updateList.setEnabled(true);
    }

    private void disableWidget() {
        bt_updateList.setEnabled(false);
    }

    private void showSaveStartAnimeDialog(Context context){
        final AlertDialog[] dialog = new AlertDialog[1];

        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("给开机动画起个名字(不允许特殊字符):");

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

        Button bt_saveToApp = new Button(this);
        bt_saveToApp.setText("保存到手机");
        bt_saveToApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String animeName = editText.getText().toString();
                if (!animeName.isEmpty()) {
                    StartAnimeFileManager.saveStartAnimeList(context, animeName, startAnimeLinkedList);
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

    private void showExistStartAnimeDialog(Context context){
        RinaBoardApp app = (RinaBoardApp) getApplication();

        List<String> animeNames = StartAnimeFileManager.getAllStartAnimeNames(context);

        final AlertDialog[] dialog = new AlertDialog[1];

        // 加载自定义布局文件
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.startanimeselect_dialog_layout, null);

        ListView lv_existAnime = view.findViewById(R.id.lv_existAnime);

        // 创建适配器
        SelectAdapter adapter = new SelectAdapter(this, animeNames);

        lv_existAnime.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.setSelectedPosition(position);
            }
        });

        // 设置适配器
        lv_existAnime.setAdapter(adapter);

        Button bt_show = view.findViewById(R.id.bt_show);
        Button bt_delete = view.findViewById(R.id.bt_delete);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);

        bt_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = adapter.getSelectedPosition();
                if (selectedPosition != -1){
                    String selectedItem = adapter.getItem(selectedPosition);
                    if(selectedItem != null){
                        app.setStartAnimeLinkedList(StartAnimeFileManager.loadStartAnimeList(context, selectedItem));
                        startAnimeLinkedList = app.getStartAnimeLinkedList();
                        startAnimeView.setStartAnimeView(startAnimeLinkedList);
                    }
                }else {
                    return;
                }
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = adapter.getSelectedPosition();
                if (selectedPosition != -1){
                    String selectedItem = adapter.getItem(selectedPosition);
                    if(selectedItem != null){
                        StartAnimeFileManager.deleteStartAnimeList(context, selectedItem);
                    }
                }else {
                    return;
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
    }

    private void showAddExpressionDialog(Context context) {
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
        SelectAdapter adapter1 = new SelectAdapter(this, expOnBoard);
        SelectAdapter adapter2 = new SelectAdapter(this, expOnLocal);

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

        bt_show.setText("确定");
        bt_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition1 = adapter1.getSelectedPosition();
                int selectedPosition2 = adapter2.getSelectedPosition();
                if (selectedPosition1 != -1) {
                    String selectedItem = adapter1.getItem(selectedPosition1);
                    if (selectedItem != null) {
                        appendStartAnime(new ExpressionOnBoard(selectedItem));
                    }
                } else if (selectedPosition2 != -1) {
                    String selectedItem = adapter2.getItem(selectedPosition2);
                    if (selectedItem != null & map[0] != null) {
                        byte[] bitmap = map[0].get(selectedItem);
                        if (bitmap != null) {
                            appendStartAnime(new Expression(selectedItem, bitmap));
                        }
                    }
                } else {
                    return;
                }
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });
        bt_delete.setVisibility(View.GONE);
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

    private void showAddDelayDialog(String type) {
        final AlertDialog[] dialog = new AlertDialog[1];
        int max = 0;

        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (type.equals("毫秒")) {
            max = 999;
        } else if (type.equals("秒")) {
            max = 10;
        }
        builder.setTitle("延时" + type + "(最大为" + max + "):");

        // 创建一个 EditText，并添加到 AlertDialog 中
        final EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(layoutParams);
        // 设置输入类型为数字
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // 设置过滤器，限制输入范围为0-999
        int finalMax = max;
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    // 尝试把输入的字符串转换为数字
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    // 检查是否在0-999范围内
                    if (input >= 0 && input <= finalMax) {
                        return null;  // 返回null表示接受输入
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略转换异常
                }

                // 输入不在0-999范围内，返回空字符串表示拒绝输入
                return "";
            }
        };
        // 添加过滤器
        editText.setFilters(new InputFilter[]{inputFilter});
        builder.setView(editText);

        // 自定义布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button bt_ensure = new Button(this);
        bt_ensure.setText("确定");
        bt_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit = editText.getText().toString();
                if (!edit.isEmpty()) {
                    try {
                        int number = (short) Integer.parseInt(edit);
                        if (type.equals("毫秒")) {
                            appendStartAnime(new DelayMicroSeconds((short) number));
                        } else if (type.equals("秒")) {
                            appendStartAnime(new DelaySeconds((byte) number));
                        }
                        dialog[0].dismiss();
                    } catch (NumberFormatException e) {
                        Log.e("EditView", "输入的字符串不是有效的整数");
                    }
                }
            }
        });
        layout.addView(bt_ensure);

        Button bt_cancel = new Button(this);
        bt_cancel.setText("取消");
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
            }
        });
        layout.addView(bt_cancel);


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

    private void showChangeStartAnimeDialog(Context context, int position) {
        final AlertDialog[] dialog = new AlertDialog[1];
        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("改变为:");
        // 自定义布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button bt_expression = new Button(this);
        bt_expression.setText("表情");
        bt_expression.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                showChangeExpressionDialog(context, position);
            }
        });
        layout.addView(bt_expression);

        Button bt_microSecond = new Button(this);
        bt_microSecond.setText("延时毫秒");
        bt_microSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                showChangeDelayDialog("毫秒", position);
            }
        });
        layout.addView(bt_microSecond);

        Button bt_second = new Button(this);
        bt_second.setText("延时秒");
        bt_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
                showChangeDelayDialog("秒", position);
            }
        });
        layout.addView(bt_second);

        builder.setView(layout);

        // 创建并显示对话框
        dialog[0] = builder.create();
        dialog[0].show();

        WindowManager.LayoutParams params = dialog[0].getWindow().getAttributes();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        params.width = (int) (displayMetrics.widthPixels * 0.25);
        params.height = (int) (displayMetrics.heightPixels * 0.6);
        dialog[0].getWindow().setAttributes(params);
    }

    private void showChangeExpressionDialog(Context context, int position) {
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
        SelectAdapter adapter1 = new SelectAdapter(this, expOnBoard);
        SelectAdapter adapter2 = new SelectAdapter(this, expOnLocal);

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

        bt_show.setText("确定");
        bt_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition1 = adapter1.getSelectedPosition();
                int selectedPosition2 = adapter2.getSelectedPosition();
                if (selectedPosition1 != -1) {
                    String selectedItem = adapter1.getItem(selectedPosition1);
                    if (selectedItem != null) {
                        changeStartAnime(new ExpressionOnBoard(selectedItem), position);
                    }
                } else if (selectedPosition2 != -1) {
                    String selectedItem = adapter2.getItem(selectedPosition2);
                    if (selectedItem != null & map[0] != null) {
                        byte[] bitmap = map[0].get(selectedItem);
                        if (bitmap != null) {
                            changeStartAnime(new Expression(selectedItem, bitmap), position);
                        }
                    }
                } else {
                    return;
                }
                if (dialog[0] != null) {
                    dialog[0].dismiss();
                }
            }
        });
        bt_delete.setVisibility(View.GONE);
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

    private void showChangeDelayDialog(String type, int position) {
        final AlertDialog[] dialog = new AlertDialog[1];
        int max = 0;

        // 创建一个 AlertDialog.Builder 对象
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (type.equals("毫秒")) {
            max = 999;
        } else if (type.equals("秒")) {
            max = 10;
        }
        builder.setTitle("延时" + type + "(最大为" + max + "):");

        // 创建一个 EditText，并添加到 AlertDialog 中
        final EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        editText.setLayoutParams(layoutParams);
        // 设置输入类型为数字
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        // 设置过滤器，限制输入范围为0-999
        int finalMax = max;
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    // 尝试把输入的字符串转换为数字
                    int input = Integer.parseInt(dest.toString() + source.toString());
                    // 检查是否在0-999范围内
                    if (input >= 0 && input <= finalMax) {
                        return null;  // 返回null表示接受输入
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略转换异常
                }

                // 输入不在0-999范围内，返回空字符串表示拒绝输入
                return "";
            }
        };
        // 添加过滤器
        editText.setFilters(new InputFilter[]{inputFilter});
        builder.setView(editText);

        // 自定义布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button bt_ensure = new Button(this);
        bt_ensure.setText("确定");
        bt_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String edit = editText.getText().toString();
                if (!edit.isEmpty()) {
                    try {
                        int number = (short) Integer.parseInt(edit);
                        if (type.equals("毫秒")) {
                            changeStartAnime(new DelayMicroSeconds((short) number), position);
                        } else if (type.equals("秒")) {
                            changeStartAnime(new DelaySeconds((byte) number), position);
                        }
                        dialog[0].dismiss();
                    } catch (NumberFormatException e) {
                        Log.e("EditView", "输入的字符串不是有效的整数");
                    }
                }
            }
        });
        layout.addView(bt_ensure);

        Button bt_cancel = new Button(this);
        bt_cancel.setText("取消");
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog[0].dismiss();
            }
        });
        layout.addView(bt_cancel);


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

    private void appendStartAnime(StartAnime anime) {
        if (startAnimeLinkedList.size() < StartAnimeView.CELLS) {
            startAnimeLinkedList.add(anime);
            startAnimeView.appendStartAnime(anime);
        }
    }

    private void changeStartAnime(StartAnime anime, int position) {
        startAnimeLinkedList.set(position, anime);
        startAnimeView.changeStartAnime(anime, position);
    }
}