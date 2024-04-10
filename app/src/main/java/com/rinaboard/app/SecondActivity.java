package com.rinaboard.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
    private Button bt_showExp;
    private Button bt_saveExp;
    private Button bt_existExp;
    private Button bt_showBoardExp;
    private Button bt_clearExp;
    private Button bt_fillExp;
    private PixelDrawingView pixelDrawingView;

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

                        //重绘UI
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateView(state);
                            }
                        });
                        System.out.println("View Update!");
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

    }

    private void updateView(ConnectState state) {
        RinaBoardApp app = (RinaBoardApp) getApplication();

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
                    if(selectedItem != null & map[0] != null){
                        byte[] bitmap = map[0].get(selectedItem);
                        if(bitmap != null){
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
                }else if(selectedPosition2 != -1){
                    String selectedItem = adapter2.getItem(selectedPosition2);
                    if(selectedItem != null){
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