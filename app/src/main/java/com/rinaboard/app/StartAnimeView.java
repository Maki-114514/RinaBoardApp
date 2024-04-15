package com.rinaboard.app;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.util.LinkedList;

public class StartAnimeView extends GridLayout {
    private static final int ROWS = 6;
    private static final int COLS = 6;
    public static final int CELLS = ROWS * COLS;
    private static final int GRID_SPACING = 5;
    private byte count = -1;
    private LinearLayout[][] linearLayouts = new LinearLayout[ROWS][COLS];
    private TextView[][][] textViews = new TextView[ROWS][COLS][2];
    private OnStartAnimeClickedListener onStartAnimeClickedListener;

    public StartAnimeView(Context context) {
        super(context);
        init();
    }

    public StartAnimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StartAnimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setRowCount(ROWS);
        setColumnCount(COLS);
        setUseDefaultMargins(true);  // 使用默认的间距

        for (int i = 0; i < CELLS; i++) {
            addTextViews(i / COLS, i % COLS, "Text1", "Text2", Color.BLACK);
        }
    }

    public void setOnStartAnimeClickedListener(@NonNull OnStartAnimeClickedListener listener) {
        this.onStartAnimeClickedListener = listener;
    }

    private void addTextViews(int row, int col, String text1, String text2, int textColor) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = 0;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.rowSpec = GridLayout.spec(row, 1f);
        layoutParams.columnSpec = GridLayout.spec(col, 1f);
        layoutParams.setMargins(GRID_SPACING, GRID_SPACING, GRID_SPACING, GRID_SPACING);

        linearLayouts[row][col] = new LinearLayout(getContext());
        linearLayouts[row][col].setOrientation(LinearLayout.VERTICAL);

        textViews[row][col][0] = new TextView(getContext());
        textViews[row][col][0].setText(text1);
        textViews[row][col][0].setTextColor(textColor);
        textViews[row][col][0].setBackgroundColor(Color.WHITE);
        textViews[row][col][0].setGravity(Gravity.CENTER);
        textViews[row][col][0].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        textViews[row][col][1] = new TextView(getContext());
        textViews[row][col][1].setText(text2);
        textViews[row][col][1].setTextColor(textColor);
        textViews[row][col][1].setBackgroundColor(Color.WHITE);
        textViews[row][col][1].setGravity(Gravity.CENTER);
        textViews[row][col][1].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        linearLayouts[row][col].addView(textViews[row][col][0]);
        linearLayouts[row][col].addView(textViews[row][col][1]);

        addView(linearLayouts[row][col], layoutParams);
    }

    public void setStartAnimeView(@NonNull LinkedList<StartAnime> startAnimeLinkedList) {
        count = -1;  // 初始化为-1
        for (StartAnime element : startAnimeLinkedList) {
            if (count >= CELLS - 1) {
                break;
            }
            count++;

            int row = count / COLS;
            int col = count % COLS;
            linearLayouts[row][col].setVisibility(VISIBLE);
            linearLayouts[row][col].setClickable(true);
            linearLayouts[row][col].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    System.out.println("The linearLayout at (" + row + ", " + col + ") is clicked");
                    if (onStartAnimeClickedListener != null) {
                        onStartAnimeClickedListener.onStartAnimeClicked(row * COLS + col);
                    }
                }
            });
            if (element == null) {
                continue;
            }

            if (element instanceof Expression) {
                Expression expression = (Expression) element;
                setText(row, col, "表情", expression.getText());
            } else if (element instanceof ExpressionOnBoard) {
                ExpressionOnBoard expression = (ExpressionOnBoard) element;
                setText(row, col, "板上表情", expression.getText());
            } else if (element instanceof DelayMicroSeconds) {
                DelayMicroSeconds delayMicroSeconds = (DelayMicroSeconds) element;
                setText(row, col, "延时毫秒", delayMicroSeconds.getText());
            } else if (element instanceof DelaySeconds) {
                DelaySeconds delaySeconds = (DelaySeconds) element;
                setText(row, col, "延时秒", delaySeconds.getText());
            }
        }

        // 设置超出部分的LinearLayout为不可见且不可点击
        for (int i = count + 1; i < CELLS; i++) {
            int row = i / COLS;
            int col = i % COLS;
            linearLayouts[row][col].setVisibility(INVISIBLE);
            linearLayouts[row][col].setClickable(false);
        }
    }

    public void appendStartAnime(StartAnime anime) {
        if (count < CELLS - 1 && anime != null) {
            count++;

            int row = count / COLS;
            int col = count % COLS;
            linearLayouts[row][col].setVisibility(VISIBLE);
            linearLayouts[row][col].setClickable(true);
            linearLayouts[row][col].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    System.out.println("The linearLayout at (" + row + ", " + col + ") is clicked");
                    if (onStartAnimeClickedListener != null) {
                        onStartAnimeClickedListener.onStartAnimeClicked(row * COLS + col);
                    }
                }
            });

            if (anime instanceof Expression) {
                Expression expression = (Expression) anime;
                setText(row, col, "表情", expression.getText());
            } else if (anime instanceof ExpressionOnBoard) {
                ExpressionOnBoard expression = (ExpressionOnBoard) anime;
                setText(row, col, "板上表情", expression.getText());
            } else if (anime instanceof DelayMicroSeconds) {
                DelayMicroSeconds delayMicroSeconds = (DelayMicroSeconds) anime;
                setText(row, col, "延时毫秒", delayMicroSeconds.getText());
            } else if (anime instanceof DelaySeconds) {
                DelaySeconds delaySeconds = (DelaySeconds) anime;
                setText(row, col, "延时秒", delaySeconds.getText());
            }
        }
    }

    public void changeStartAnime(StartAnime anime, int position) {
        if (position < 0 || position >= CELLS - 1) {
            return;
        }
        int row = position / COLS;
        int col = position % COLS;
        if (linearLayouts[row][col].getVisibility() == VISIBLE) {
            if (anime instanceof Expression) {
                Expression expression = (Expression) anime;
                setText(row, col, "表情", expression.getText());
            } else if (anime instanceof ExpressionOnBoard) {
                ExpressionOnBoard expression = (ExpressionOnBoard) anime;
                setText(row, col, "板上表情", expression.getText());
            } else if (anime instanceof DelayMicroSeconds) {
                DelayMicroSeconds delayMicroSeconds = (DelayMicroSeconds) anime;
                setText(row, col, "延时毫秒", delayMicroSeconds.getText());
            } else if (anime instanceof DelaySeconds) {
                DelaySeconds delaySeconds = (DelaySeconds) anime;
                setText(row, col, "延时秒", delaySeconds.getText());
            }
        }
    }

    public void deleteStartAnime() {
        if (count >= 0 && count < CELLS) {
            int row = count / COLS;
            int col = count % COLS;
            linearLayouts[row][col].setVisibility(INVISIBLE);
            count--;
        }
    }

    public void setText(int row, int col, String text1, String text2) {
        if (isValidCell(row, col)) {
            textViews[row][col][0].setText(text1);
            textViews[row][col][1].setText(text2);
        }
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public interface OnStartAnimeClickedListener {
        void onStartAnimeClicked(int position);
    }
}
