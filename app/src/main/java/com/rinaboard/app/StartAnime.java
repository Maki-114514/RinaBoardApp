package com.rinaboard.app;

public abstract class StartAnime {
    private String text;
    private String type;

    public StartAnime(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

class Expression extends StartAnime {
    private byte[] bitmap;

    public Expression(){
        super("Expression", "");
    }

    public Expression(String bitmapName, byte[] bitmap) {
        super("Expression", bitmapName);
        this.bitmap = bitmap;
    }

    public void setBitmap(byte[] bitmap){
        this.bitmap = bitmap;
    }
    public byte[] getBitmap() {
        return bitmap;
    }
}

class ExpressionOnBoard extends StartAnime {
    public ExpressionOnBoard(){
        super("ExpressionOnBoard", "");
    }
    public ExpressionOnBoard(String bitmapName) {
        super("ExpressionOnBoard", bitmapName);
    }
}

class DelayMicroSeconds extends StartAnime {
    private short ms;

    public DelayMicroSeconds(){
        super("DelayMicroSeconds", String.valueOf(0) + "ms");
        this.ms = 0;
    }
    public DelayMicroSeconds(short ms) {
        super("DelayMicroSeconds", String.valueOf(ms) + "ms");
        this.ms = ms;
    }

    public short getMs() {
        return ms;
    }

    public void setMs(short ms) {
        this.ms = ms;
    }
}

class DelaySeconds extends StartAnime {
    private byte s;

    public DelaySeconds(){
        super("DelaySeconds", String.valueOf(0) + "s");
        this.s = 0;
    }

    public DelaySeconds(byte s) {
        super("DelaySeconds", String.valueOf(s) + "s");
        this.s = s;
    }

    public byte getSeconds() {
        return s;
    }

    public void setS(byte s) {
        this.s = s;
    }
}