package com.droidrise.touchforbidden;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checkDrawOverlayPermission();
        createFloatView();
    }

    Button buttonLock;
    boolean forbidden = false;
    WindowManager wm;
    Button btn_floatView;
    LinearLayout mInView;
    WindowManager.LayoutParams params;
    private void createFloatView()
    {
        btn_floatView  = new Button(getApplicationContext());
        btn_floatView.setText("Touch Forbidden");

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        mInView = (LinearLayout)inflater.inflate(R.layout.activity_topview, null, false);
        buttonLock = (Button)mInView.findViewById(R.id.button_temp);
        buttonLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forbidden) {
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    wm.updateViewLayout(mInView, params);
                    forbidden = false;

                    buttonLock.setText("Free");
                } else {
                    params.flags =  WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    wm.updateViewLayout(mInView, params);
                    forbidden = true;
                    buttonLock.setText("Frezzen");
                }
            }
        });

        wm = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();

        // 设置window type
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        /*
         * 如果设置为params.type = WindowManager.LayoutParams.TYPE_PHONE; 那么优先级会降低一些,
         * 即拉下通知栏不可见
         */

        params.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明

        // 设置Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        /*
         * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
         * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
         * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
         */

        // 设置悬浮窗的长得宽
        params.width = 400;
        params.height = 150;

        // 设置悬浮窗的Touch监听
        mInView.setOnTouchListener(new View.OnTouchListener()
        {
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        paramX = params.x;
                        paramY = params.y;
                        //params.flags =  WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        //wm.updateViewLayout(mInView, params);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        //params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        //        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        wm.updateViewLayout(mInView, params);
                        break;
                }
                return true;
            }
        });

        wm.addView(mInView, params);
        //isAdded = true;
    }

    public final static int REQUEST_CODE = 100;

    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            createFloatView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                createFloatView();
            }
        }
    }
}
