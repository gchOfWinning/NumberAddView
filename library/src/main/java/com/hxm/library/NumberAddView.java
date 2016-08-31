package com.hxm.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class NumberAddView extends View {

    private Paint paint;

    private int textColor;
    private int textSize;
    private int animDuration;
    private MoveSize moveSize;
    private int maxTextLength;

    private enum MoveSize{

        LARGE,
        SMALL;
    }

    private String curValue = "", nextValue = "";

    private int chatWidth,chatHeight;

    private int minWidth,minHeight;

    private float progress = 0;

    private ValueAnimator animator;

    private int curLen,nextLen;
    private int nextScrollLen,curScrollLen,NoScrollLen;

    private boolean isAdd;

    private ValueChangedListener listener;

    private int xPadding = 2;

    public interface ValueChangedListener{

        void valueChanged(int value);
    }

    public NumberAddView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init(attrs);
    }

    private void init(AttributeSet attrs) {

        TypedArray typeArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.NumberAddView, 0, 0);

        try {
            this.textColor = typeArray.getColor(R.styleable.NumberAddView_textColor, Color.GRAY);
            this.textSize = typeArray.getDimensionPixelSize(R.styleable.NumberAddView_textSize, 40);
            this.animDuration = typeArray.getDimensionPixelOffset(R.styleable.NumberAddView_animDuration,400);

            int sizeIndex = typeArray.getInt(R.styleable.NumberAddView_moveSize, 0);
            this.moveSize = MoveSize.values()[sizeIndex];

            this.maxTextLength = typeArray.getInt(R.styleable.NumberAddView_maxTextLength, 6);
        }finally {

            typeArray.recycle();
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(this.textColor);
        paint.setTextSize(this.textSize);
        paint.setStrokeWidth(3);

        Rect rect = new Rect();
        paint.getTextBounds("8", 0, 1, rect);
        this.chatWidth = rect.width();
        this.chatHeight = rect.height();

        if(moveSize==MoveSize.LARGE)
            minHeight = chatHeight * 5;
        else
            minHeight = chatHeight * 3;

        minWidth = chatWidth*maxTextLength;

        initAnimator();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = 0, heightSize = 0;

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        switch (widthMode){

            case MeasureSpec.EXACTLY:

                widthSize = Math.max(minWidth, width);
                break;

            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:

                widthSize = minWidth;
                break;
        }

        switch (heightMode){

            case MeasureSpec.EXACTLY:

                heightSize = Math.max(minHeight, height);
                break;

            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:

                heightSize = minHeight;
                break;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (NoScrollLen > 0) {

            paint.setAlpha(255);

            for(int i=0;i<curLen - curScrollLen;i++){

                int x = (chatWidth+xPadding)*i;

                canvas.drawText(curValue,i,i+1,x,chatHeight*(moveSize==MoveSize.LARGE?3:2),paint);
            }
        }

        if(curLen<=curValue.length()){

            paint.setAlpha((int) (255 * (1 - progress)));

            for(int i=Math.max(0,curLen-curScrollLen);i<curLen;i++){

                int x = (chatWidth+xPadding)*i;

                int addY = (int) ((moveSize==MoveSize.LARGE?(3 - 2 * progress):(2 - progress))*chatHeight);
                int subY = (int) ((moveSize==MoveSize.LARGE?(3 + 2 * progress):(2 + progress))*chatHeight);

                canvas.drawText(curValue,i,i+1,x,isAdd?addY:subY,paint);
            }
        }

        paint.setAlpha((int) (255 * progress));

        for(int i=Math.max(0,nextLen-nextScrollLen);i<nextLen;i++){

            int x = (chatWidth+xPadding)*i;

            int addY = (int) ((moveSize==MoveSize.LARGE?(5 - 2 * progress):(3 - progress))*chatHeight);
            int subY = (int) ((moveSize==MoveSize.LARGE?(1 + 2 * progress):1 + progress)*chatHeight);

            canvas.drawText(nextValue,i, Math.min(i+1, nextLen),x,isAdd?addY:subY, paint);
        }
    }

    private void setNumber(String value){

        if(TextUtils.isEmpty(value))
            return;

        setNumber(Integer.parseInt(value));
    }

    public void setNumber(int value){

        if(value<1)
            throw new IllegalArgumentException("value less than 1 are not accepted");

        if (animator.isRunning())
            return;

        isAdd = true;

        this.curValue = "";
        this.nextValue = String.valueOf(value);

        setParams(true);

        if (!animator.isStarted())
            animator.start();
    }

    public void addNumber() {

        if (animator.isRunning())
            return;

        isAdd = true;

        int value = TextUtils.isEmpty(curValue)?0:Integer.parseInt(curValue);

        nextValue = String.valueOf(++value);

        setParams(false);

        if (!animator.isStarted())
            animator.start();
    }

    public void subNumber(){

        if (animator.isRunning())
            return;

        if(TextUtils.isEmpty(curValue))
            return;

        int value = TextUtils.isEmpty(curValue)?0:Integer.parseInt(curValue);

        value--;

        nextValue = value==0?"":String.valueOf(value);

        isAdd = false;

        setParams(false);

        if (!animator.isStarted())
            animator.start();
    }

    private void setParams(boolean setDirect){

        curLen = TextUtils.isEmpty(curValue)?0:curValue.length();
        nextLen = TextUtils.isEmpty(nextValue)?0:nextValue.length();

        nextScrollLen = setDirect?nextLen:getScrollLenByStr(isAdd ? nextValue : curValue);

        curScrollLen = Math.min(nextScrollLen, curLen);

        nextScrollLen = Math.min(nextLen, nextScrollLen);

        NoScrollLen = curLen - curScrollLen;
    }

    private void initAnimator(){

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(this.animDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                progress = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                curValue = nextValue;

                if(listener!=null)
                    listener.valueChanged(TextUtils.isEmpty(curValue)?0:Integer.parseInt(curValue));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private int getScrollLenByStr(String nextValue) {

        if(TextUtils.isEmpty(nextValue))
            return 0;

        int value = Integer.parseInt(nextValue);

        int lenNeedScroll = 1;

        while (value > 9 && value % 10 == 0) {

            lenNeedScroll++;

            value = value / 10;
        }

        return lenNeedScroll;
    }

    public int getCurrentValue(){

        return TextUtils.isEmpty(curValue)?0:Integer.parseInt(curValue);
    }

    public void setOnValueChangedListener(ValueChangedListener listener){

        this.listener = listener;
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();

        bundle.putString("curValue", curValue);

        bundle.putParcelable("superState", super.onSaveInstanceState());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if(state instanceof Bundle){

            Bundle bundle = (Bundle) state;

            curValue = bundle.getString("curValue");

            setNumber(curValue);

            state = bundle.getParcelable("superState");
        }

        super.onRestoreInstanceState(state);
    }
}