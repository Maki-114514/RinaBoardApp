package com.rinaboard.app;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import java.io.ByteArrayOutputStream;

public class PixelDrawingView extends GridLayout {
    private final int ROWS = 16;
    private final int COLS = 18;
    private Button[][] buttons;
    private Paint paint;
    private int margin;
    private int buttonColor = Color.parseColor("#FF1493");
    private final int rowBytes = (COLS + 7) / 8;
    private byte[] bitmap = new byte[ROWS * rowBytes];
    private OnBitmapUpdateListener bitmapUpdateListener;

    public void setOnBitmapUpdateListener(OnBitmapUpdateListener listener) {
        this.bitmapUpdateListener = listener;
    }

    public PixelDrawingView(Context context) {
        super(context);
        init();
    }

    public PixelDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public void setBackgroundColor(int setColor) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    Button button = buttons[row][col];
                    int color = (int) button.getTag(); // 获取按钮的状态（颜色）
                    // 如果按钮的状态为点亮，则将对应的字节位设置为1
                    if (color != Color.WHITE) {
                        button.setBackgroundColor(setColor);
                        button.setTag(setColor); // 设置按钮的 tag 为白色
                    }
                }
            }
        }
        this.buttonColor = setColor;
    }

    private void init() {
        setRowCount(ROWS);
        setColumnCount(COLS);
        buttons = new Button[ROWS][COLS];
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        // 计算按钮的宽度和高度
        int buttonSize = Math.min(getWidth() / COLS, getHeight() / ROWS);
        margin = 4;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    Button button = new Button(getContext());
                    // 设置按钮的初始背景颜色为白色
                    button.setBackgroundColor(Color.WHITE);
                    button.setTag(Color.WHITE); // 设置按钮的 tag 为白色
                    button.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button clickedButton = (Button) v;
                            int currentColor = (int) clickedButton.getTag();
                            if (currentColor == Color.WHITE) { // 如果当前是白色
                                clickedButton.setBackgroundColor(buttonColor); // 改为深粉色
                                clickedButton.setTag(buttonColor); // 更新状态为深粉色
                            } else {
                                clickedButton.setBackgroundColor(Color.WHITE); // 否则改为白色
                                clickedButton.setTag(Color.WHITE); // 更新状态为白色
                            }
                            updateBitmap();
                        }
                    });
                    buttons[row][col] = button;
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = buttonSize;
                    params.height = buttonSize;
                    params.rowSpec = GridLayout.spec(row, 1f);
                    params.columnSpec = GridLayout.spec(col, 1f);
                    // 设置外边距
                    params.setMargins(margin, margin, margin, margin);
                    button.setLayoutParams(params);
                    addView(button);
                }
            }
        }
    }

    public void updateBitmap() {
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
                    Button button = buttons[row][col];
                    int color = (int) button.getTag(); // 获取按钮的状态（颜色）
                    // 如果按钮的状态为点亮，则将对应的字节位设置为1
                    if (color == buttonColor) {
                        bitmap[row * rowBytes + col / 8] |= (byte) (1 << (7 - col % 8));
                    } else {
                        // 如果按钮的状态为关闭，则将对应的字节位设置为0
                        bitmap[row * rowBytes + col / 8] &= (byte) ~(1 << (7 - col % 8));
                    }
                }
            }
        }
        // 在循环结束后调用onBitmapUpdated()方法
        if (bitmapUpdateListener != null) {
            bitmapUpdateListener.onBitmapUpdated(getBitmap());
        }
    }


    public byte[] getBitmap() {
        return bitmap;
    }

    public void drawBitmap(byte[] img) {
        // 遍历所有按钮，根据 img 中的内容更新按钮状态
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {

                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    Button button = buttons[row][col];
                    // 检查 img 中相应位置的像素状态
                    int byteIndex = row * rowBytes + col / 8;
                    int bitIndex = 7 - col % 8;
                    boolean isPixelOn = ((img[byteIndex] >> bitIndex) & 1) == 1;

                    // 根据像素状态设置按钮颜色和标签
                    if (isPixelOn) {
                        button.setBackgroundColor(buttonColor); // 如果像素点亮，则设置为指定颜色
                        button.setTag(buttonColor); // 更新状态为点亮
                    } else {
                        button.setBackgroundColor(Color.WHITE); // 否则设置为白色
                        button.setTag(Color.WHITE); // 更新状态为关闭
                    }
                }
            }
        }
        bitmap = img;
    }

    public void turnOffAllButtons() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    Button button = buttons[row][col];
                    button.setBackgroundColor(Color.WHITE);
                    button.setTag(Color.WHITE);
                }
            }
        }
    }

    public void turnOnAllButtons() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!((row == 0 && (col == 0 || col == 1 || col == COLS - 2 || col == COLS - 1))
                        || (row == 1 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 4 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 3 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 2 && (col == 0 || col == COLS - 1))
                        || (row == ROWS - 1 && (col == 0 || col == 1 || col == 2 || col == COLS - 3 || col == COLS - 2 || col == COLS - 1))
                )) {
                    Button button = buttons[row][col];
                    button.setBackgroundColor(buttonColor);
                    button.setTag(buttonColor);
                }
            }
        }
    }

    public interface OnBitmapUpdateListener {
        void onBitmapUpdated(byte[] bitmap);
    }
}



