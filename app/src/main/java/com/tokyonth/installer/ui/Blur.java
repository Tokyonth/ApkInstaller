package com.tokyonth.installer.ui;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.util.HashMap;
import java.util.Map;

public class Blur {
    /**
     * 此类为高斯模糊图像处理类
     * @author GaoChrishao
     * @Version 1.1
     */

    public static Map<Object,Bitmap> bkgMaps=new HashMap<>();
    public static double brightness=-0.1;


    /**
     * 模糊处理Bitmap
     * @param sentBitmap
     * @param radius
     * @param canReuseInBitmap
     * @return
     */
    public static Bitmap blurBitmap(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {
        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        //图片宽度和高度
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];

        //得到颜色矩阵
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;


        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];


        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;  //获取红色部分的值
                sir[1] = (p & 0x00ff00) >> 8;   //获取绿色部分的值
                sir[2] = (p & 0x0000ff);        //获取蓝色部分的值
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return setBitmapBrightness(bitmap,brightness);
    }


    /**
     * 更具layout的位置以及大小，从已经模糊处理过的图片中截取所需要的部分
     * @param fromView
     * @param toView
     * @param radius
     * @param scaleFactor
     * @param roundCorner
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void cutBluredBitmap(View fromView, View toView, int radius, float scaleFactor, float roundCorner) {
        // 获取View的截图

        if (radius < 1 || radius > 26) {
            scaleFactor = 8;
            radius = 2;
        }

       if(bkgMaps.get(fromView)==null){
          initBkg(fromView,radius,scaleFactor);
       }


        int top,left;
        int[] location=new int[2];
        toView.getLocationInWindow(location);
        left=location[0];
        top=location[1];
        if(toView.getWidth()>0&&toView.getHeight()>0){
            Bitmap overlay = Bitmap.createBitmap((int) (toView.getWidth() / scaleFactor), (int) (toView.getHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);
            canvas.translate(-left / scaleFactor, -top / scaleFactor);
            canvas.scale(1 / scaleFactor, 1 / scaleFactor);
            Paint paint = new Paint();
            paint.setFlags(Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(bkgMaps.get(fromView), 0, 0, paint);
            RoundedBitmapDrawable bdr= RoundedBitmapDrawableFactory.create(Resources.getSystem(),overlay);
            bdr.setCornerRadius(roundCorner);
            toView.setBackground(bdr);
        }




    }


    /**
     * 初始化，将原始图片处理成模糊后的图片
     * @param fromView
     * @param radius
     * @param scaleFactor
     */
    static void initBkg(View fromView,int radius, float scaleFactor){
        BitmapDrawable bd=(BitmapDrawable) fromView.getBackground();
        Bitmap bkg1=bd.getBitmap();
        fromView.destroyDrawingCache();
        fromView.setDrawingCacheEnabled(true);
        int height = (int) (fromView.getDrawingCache().getHeight());  //屏幕高度
        int width = (int) (fromView.getDrawingCache().getWidth());    //屏幕宽度

        //精确缩放到指定大小
        Bitmap bkg_origin= Bitmap.createScaledBitmap(bkg1,(int)(width/scaleFactor),(int)(height/scaleFactor), true);
        bkg_origin.setConfig(Bitmap.Config.ARGB_8888);
        Bitmap bkg=Bitmap.createScaledBitmap(blurBitmap(bkg_origin,radius,true),width,height,true);
        bkgMaps.put(fromView,bkg);
        //Log.e("Blur","高斯模糊背景图片:"+bkg.getWidth()+","+bkg.getHeight());
    }

    public static void initBkgWithResieze(View fromView,Bitmap bitmap, int width, int height){
        if(bitmap!=null){
            Bitmap bkg=Bitmap.createScaledBitmap(bitmap,width,height,true);
            bkgMaps.put(fromView,bkg);
        }
    }



    //调节亮度
    public static Bitmap setBitmapBrightness(Bitmap bitmap, double depth)
    {
        Bitmap bm = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), bitmap.getConfig());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for(int row=0; row<height; row++){
            for(int col=0; col<width; col++){
                int pixel = bitmap.getPixel(col, row);// ARGB
                int red = Color.red(pixel); // same as (pixel >> 16) &0xff
                int green = Color.green(pixel); // same as (pixel >> 8) &0xff
                int blue = Color.blue(pixel); // same as (pixel & 0xff)
                int alpha = Color.alpha(pixel); // same as (pixel >>> 24)
                double gray = (0.3 * red + 0.59 * green + 0.11 * blue);
                red += (depth * gray);
                if(red > 255) {
                    red = 255;
                }else if(red<0){
                    red=0;
                }

                green += (depth * gray);
                if(green > 255) {
                    green = 255;
                }else if(green<0){
                    green=0;
                }

                blue += (depth * gray);
                if(blue > 255) {
                    blue = 255;
                }
                else if(blue<0){
                    blue=0;
                }
                bm.setPixel(col, row, Color.argb(alpha, red, green, blue));
            }
        }
        return bm;
    }


    /**
     * 此类为控件模糊处理类
     *
     */
    public static class BlurLayout{
        private int positionX,positionY;
        private View layoutView,layoutBkg;

        //毛玻璃效果参数,可以动态修改
        private int RoundCorner=50;
        private int radius=10;
        private int scaleFactor=26;

        public BlurLayout(final View layoutView, final View layoutBkg){
            positionX=positionY=0;
            layoutView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    int position[]=new int[2];
                    layoutView.getLocationInWindow(position);
                    if(positionX!=position[0]||positionY!=position[1]){
                        Blur.cutBluredBitmap(layoutBkg,layoutView,radius,scaleFactor,RoundCorner);
                        positionX=position[0];
                        positionY=position[1];
                    }
                    return true;
                }
            });
        }

        public void setLayoutView(View layoutView) {
            this.layoutView = layoutView;
        }

        public void setLayoutBkg(View layoutBkg) {
            this.layoutBkg = layoutBkg;
        }

        public void setRoundCorner(int roundCorner) {
            RoundCorner = roundCorner;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public void setScaleFactor(int scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        public void reSetPositions(){
            positionX=0;
            positionY=0;
        }
    }


    //此函数为全局亮度调节，必须在最开始调用个，后期调用可能会造成亮度不统一
    public static void setBrightness(double newBrightness){
        if(newBrightness<-1||newBrightness>1){

        }else{
            brightness=newBrightness;
        }
    }

    public static void destroy(View fromView){
        if(bkgMaps.get(fromView)!=null){
            bkgMaps.remove(fromView);
            Log.e("GGG","释放缓存");
        }

    }
}
