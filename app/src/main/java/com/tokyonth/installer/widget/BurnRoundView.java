package com.tokyonth.installer.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;

import com.tokyonth.installer.R;
import com.tokyonth.installer.utils.BitmapUtils;

@SuppressLint("Recycle")
public class BurnRoundView extends View {

    private int width, height;
    private int burnColor;
    private boolean isBurn;
    private Bitmap burnSrc;
    private Context context;
    private Paint overlayPaint;
    private Paint mPaint;

    public BurnRoundView(Context context) {
        super(context);
        this.context = context;
    }

    public BurnRoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(attrs);
    }

    public BurnRoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BurnRoundView);
        int imageId = array.getResourceId(R.styleable.BurnRoundView_burnSrc, 0);
        int color = array.getColor(R.styleable.BurnRoundView_burnColor, Color.RED);
        overlayPaint = new Paint();
        mPaint = new Paint();
        ConversionPic(imageId, color);
        isBurn = array.getBoolean(R.styleable.BurnRoundView_isBurn, true);
        if (isBurn) {
            burnColor = color & 0x60FFFFFF;
        } else {
            burnColor = color;
        }
    }

    private void ConversionPic(int imageId, int color) {
        burnSrc = BitmapUtils.getBitmapFromDrawable(context, imageId);
        overlayPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
    }

    public void setBurnSrc(int burnSrc, int color) {
        ConversionPic(burnSrc, setColor(color, true));
        invalidate();
    }

    public int setColor(int color, boolean isBurn) {
        this.isBurn = isBurn;
        if (isBurn) {
            burnColor = color & 0x60FFFFFF;
        } else {
            burnColor = color;
        }
        invalidate();
        return color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置画笔的样式，空心STROKE
        mPaint.setStyle(Paint.Style.FILL);
        //设置抗锯齿
        mPaint.setAntiAlias(true);
        mPaint.setColor(burnColor);
        //width >> 1 与 height >> 1为圆心位置
        canvas.drawBitmap(burnSrc, (width >> 1) - (burnSrc.getWidth() >> 1), (height >> 1) - (burnSrc.getHeight() >> 1), overlayPaint);
        canvas.drawCircle(width >> 1, height >> 1, (float) (getMeasuredWidth()/2.5),mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        width = measureWidth(widthMeasureSpec);
        height = measureHeight(heightMeasureSpec);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 72;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = 72;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

}
