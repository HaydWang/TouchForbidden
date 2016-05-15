package com.droidrise.touchblock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by a22460 on 16/5/15.
 */
public class TopViewService extends Service {
    WindowManager windowManager;
    WindowManager.LayoutParams params;

    boolean forbidden = false;

    RelativeLayout topView;
    ImageView imageHandle;
    ImageButton btnLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: create forground service to hold top view
        createFloatView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void createFloatView() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        topView = (RelativeLayout) inflater.inflate(R.layout.activity_topview, null, false);

        btnLock = (ImageButton) topView.findViewById(R.id.button_temp);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forbidden) {
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    windowManager.updateViewLayout(topView, params);
                    btnLock.setImageResource(R.drawable.ic_unlock_24);

                    forbidden = false;
                } else {
                    params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    windowManager.updateViewLayout(topView, params);
                    btnLock.setImageResource(R.drawable.ic_lock_24);

                    forbidden = true;
                }
            }
        });

        imageHandle = (ImageView) topView.findViewById(R.id.image_handle);
        imageHandle.setOnTouchListener(new View.OnTouchListener() {
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        if (forbidden != true) {
                            paramX = params.x;
                            paramY = params.y;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        windowManager.updateViewLayout(topView, params);
                        break;
                }
                return true;
            }
        });

        windowManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;

        // TODO: set base on screen resolution
        params.width = 148;
        params.height = 96;

        windowManager.addView(topView, params);
    }
}
