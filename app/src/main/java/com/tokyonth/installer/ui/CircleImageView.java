package com.tokyonth.installer.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class CircleImageView extends AppCompatImageView {

    //画笔
    private Paint mPaint;
    //圆形图片的半径
    private int mRadius;
    //图片的宿放比例
    private float mScale;

    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Bitmap getIconBitmap(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof AdaptiveIconDrawable) {
                Bitmap bitmap_icon = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap_icon);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap_icon;
            }
        } else {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //由于是圆形，宽高应保持一致
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mRadius = size / 2;
        setMeasuredDimension(size, size);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = null;
        mPaint = new Paint();
        Drawable drawable = getDrawable();

        if (null != drawable) {
            bitmap = getIconBitmap(drawable);
            //初始化BitmapShader，传入bitmap对象
            BitmapShader bitmapShader = null;
            if (bitmap != null) {
                bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                //计算缩放比例
                mScale = (mRadius * 2.0f) / Math.min(bitmap.getHeight(), bitmap.getWidth());
                Matrix matrix = new Matrix();
                matrix.setScale(mScale, mScale);
                bitmapShader.setLocalMatrix(matrix);
                mPaint.setShader(bitmapShader);
                //画圆形，指定好坐标，半径，画笔
                canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
            }
        } else {
            super.onDraw(canvas);
        }
    }


}

