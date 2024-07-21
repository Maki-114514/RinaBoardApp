package com.rinaboard.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.RinaBoardApp.SystemState.ExpressionMode;

public class SecondVideoActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_VIDEO = 1;
    Size targetSize = new Size(18, 16);
    private ImageButton bt_mainPage;
    private ImageButton bt_thirdPage;
    private ImageView bt_setting;
    private ImageView iv_batteryDisplay;
    private Button bt_updateVideo;
    private Button bt_controlVideo;
    private Switch sw_rollBack;
    private boolean rollBackIsOn = false;
    private TextureView tv_video;
    private Uri videoUri;
    private MediaPlayer mediaPlayer;
    private Bitmap bitmap;
    private Mat mat;
    private ImageView iv_processedImage;
    private SeekBar sb_video;
    private final Handler handler = new Handler();
    private UDPInteraction udp1;
    private ConnectThread connectThread1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_second_video);

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
                                    float voltage = app.getBatteryVoltage();
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
                        app.setCustomColor(Color.parseColor("#FF1493"));
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(SecondVideoActivity.this);
                                builder.setTitle("警告");
                                builder.setMessage("璃奈板连接丢失");
                                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        // 对话框关闭时启动 MainActivity
                                        startActivity(new Intent(SecondVideoActivity.this, MainActivity.class));
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
                        }else {
                            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
                        }
                    }
                });
            }
        });

        if (!OpenCVLoader.initDebug()) {
            Log.i("cv", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.i("cv", "OpenCV library found inside package. Using it!");
        }
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == RESULT_OK && data != null) {
            // 获取选中视频的 URI
            videoUri = data.getData();
            if (videoUri != null) {
                releaseMediaPlayer();
                // 加载视频到 MediaPlayer 中
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getApplicationContext(), videoUri);
                    mediaPlayer.setSurface(new Surface(tv_video.getSurfaceTexture()));
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            bt_controlVideo.setText("重新播放");
                        }
                    });

                    mediaPlayer.prepare();
                    mediaPlayer.start();

                    sb_video.setMax(mediaPlayer.getDuration());
                    sb_video.setVisibility(View.VISIBLE);
                    bt_controlVideo.setEnabled(true);
                    bt_controlVideo.setText("暂停播放");

                    updateProgress();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "无法加载视频", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 禁用按钮
                bt_controlVideo.setEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            bt_controlVideo.setText("继续播放");
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在 Activity 停止时停止视频播放
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            bt_controlVideo.setText("继续播放");
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mediaPlayer != null) {
            updateProgress();
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        if (mediaPlayer != null) {
            updateProgress();
        }
    }
    private void initView() {
        RinaBoardApp app = (RinaBoardApp) getApplication();

        bt_mainPage = findViewById(R.id.bt_mainPage);
        bt_mainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("MainPage Button is clicked!");
                startActivity(new Intent(SecondVideoActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //第三页按钮
        bt_thirdPage = findViewById(R.id.bt_thirdPage);
        bt_thirdPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("thirdPage Button is clicked!");
                startActivity(new Intent(SecondVideoActivity.this, ThirdActivity.class));
                overridePendingTransition(0, 0);
            }
        });

        //设置页面
        bt_setting = findViewById(R.id.bt_setting);
        bt_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SecondVideoActivity.this, SettingActivity.class));
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
        }else {
            iv_batteryDisplay.setImageResource(R.drawable.battery_0);
        }

        //视频选择按钮
        bt_updateVideo = findViewById(R.id.bt_updateVideo);
        bt_updateVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
                startActivityForResult(intent, REQUEST_PICK_VIDEO);
            }
        });

        bt_controlVideo = findViewById(R.id.bt_controlVideo);
        bt_controlVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bt_controlVideo.getText().equals("暂停播放")) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                    bt_controlVideo.setText("继续播放");
                } else if (bt_controlVideo.getText().equals("继续播放") | bt_controlVideo.getText().equals("重新播放")) {
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                    bt_controlVideo.setText("暂停播放");
                }
            }
        });

        sw_rollBack = findViewById(R.id.sw_rollBack);
        sw_rollBack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rollBackIsOn = isChecked;
            }
        });

        tv_video = findViewById(R.id.tv_video);

        sb_video = findViewById(R.id.sb_video);
        sb_video.setVisibility(View.GONE);
        sb_video.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 如果是用户手动拖动SeekBar改变的进度，则更新视频的播放进度
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        iv_processedImage = findViewById(R.id.iv_processedImage);
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void updateProgress() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            sb_video.setProgress(currentPosition);

            bitmap = tv_video.getBitmap();
            mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(mat, mat, 170, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

            Imgproc.resize(mat, mat, targetSize);
            bitmap = Bitmap.createBitmap(18, 16, Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bitmap);
            iv_processedImage.setImageBitmap(bitmap);
            updateBitmap(bitmap);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateProgress();
                }
            }, 30);
        }
    }


    private void updateBitmap(Bitmap bitmap){
        final int ROWS = 16;
        final int COLS = 18;
        final int rowBytes = (COLS + 7) / 8;

        byte[] bytesData = new byte[48];

        // 遍历所有按钮，根据按钮状态设置字节数组的值
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    int pixel = bitmap.getPixel(col, row);
                    if(rollBackIsOn)
                    {
                        if (pixel == 0xFFFFFFFF) { // 0xFFFFFFFF 表示白色
                            bytesData[row * rowBytes + col / 8] |= (byte) (1 << (7 - col % 8));
                        }
                    }else {
                        if (pixel != 0xFFFFFFFF) { // 0xFFFFFFFF 表示白色
                            bytesData[row * rowBytes + col / 8] |= (byte) (1 << (7 - col % 8));
                        }
                    }
                }
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] data = setBitmapToBoard(bytesData);
                udp1.send(data);
            }
        }).start();
    }
}