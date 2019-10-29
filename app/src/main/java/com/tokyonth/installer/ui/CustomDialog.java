package com.tokyonth.installer.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokyonth.installer.R;

public class CustomDialog extends Dialog {

    private Button yesBtn;
    private Button noBtn;
    private TextView titleTv;
    private TextView messageTv;
    private View line_v;
    private View line_h;
    private LinearLayout ll, show_cust;
    private String title;
    private String message;
    private String yesStr;
    private String noStr;
    private Context context;

    private View cust_view;

    //  接口监听
    private onNoOnclickListener noOnclickListener;
    private onYesOnclickListener yesOnclickListener;

    public interface onYesOnclickListener {
        void onYesClick();
    }

    public interface onNoOnclickListener {
        void onNoClick();
    }

    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            noStr = str;
        }
        this.noOnclickListener = onNoOnclickListener;
    }

    public void setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
    }

    //构造方法
    public CustomDialog(Context context) {
        super(context, R.style.CustomDialog);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        //按空白处不能取消动画
        //setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();

    }



    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick();
                    dismiss();
                }
            }
        });
        //设置取消按钮被点击后，向外界提供监听
       noBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (noOnclickListener != null) {
                   noOnclickListener.onNoClick();
                   //  dismiss();
               }
           }
       });

    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        if (cust_view != null) {
            show_cust.addView(cust_view);
            messageTv.setVisibility(View.GONE);
        }

        //如果用户自定了title和message
        if (title != null) {
            titleTv.setText(title);
        }
        if (message != null) {
            messageTv.setText(message);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yesBtn.setVisibility(View.VISIBLE);
            yesBtn.setText(yesStr);
        }
        if (noStr != null) {
            noBtn.setVisibility(View.VISIBLE);
            noBtn.setText(noStr);
        }

        if (yesStr != null && noStr != null) {
            line_v.setVisibility(View.VISIBLE);
        }

        if (yesStr == null && noStr == null) {
            ll.setVisibility(View.GONE);
            line_h.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        yesBtn = findViewById(R.id.yes);
        noBtn = findViewById(R.id.no);

        titleTv = findViewById(R.id.title);
        messageTv = findViewById(R.id.message);
        line_v = findViewById(R.id.line_v);
        line_h = findViewById(R.id.line_h);
        ll = findViewById(R.id.column);

        show_cust = findViewById(R.id.cust_view);

        Window dialogWindow = getWindow();
        if (dialogWindow != null) {
            dialogWindow.setGravity(Gravity.CENTER);//设置窗口位置
           // dialogWindow.setWindowAnimations(R.style.dialogWindowAnim); //设置窗口进出动画

            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            DisplayMetrics d = context.getResources().getDisplayMetrics(); // 获取屏幕宽、高
            if (lp != null) {
                lp.width = (int) (d.widthPixels * 0.7); // 设置为屏幕宽度
            }
            dialogWindow.setAttributes(lp);
        }
    }

    //为外界设置一些public 公开的方法，来向自定义的dialog传递值
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCustView(View view) {
        cust_view = view;
    }

}
