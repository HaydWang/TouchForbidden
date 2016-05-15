package com.droidrise.touchforbidden;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    ImageButton btnLock;
    boolean forbidden = false;
    WindowManager windowManager;
    LinearLayout topView;
    WindowManager.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: create forground service to hold top view
        createFloatView();

        finish();
    }

    private void createFloatView()
    {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        topView = (LinearLayout) inflater.inflate(R.layout.activity_topview, null, false);
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
                    params.flags =  WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    windowManager.updateViewLayout(topView, params);
                    btnLock.setImageResource(R.drawable.ic_lock_24);

                    forbidden = true;
                }
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

        params.width = 400;
        params.height = 150;

        topView.setOnTouchListener(new View.OnTouchListener()
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

        windowManager.addView(topView, params);
    }

    public final static int REQUEST_CODE = 100;

    // Not necessary if use TYPE_TOAST
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            createFloatView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                createFloatView();
            }
        }
    }

}
