package com.droidrise.touchblock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by a22460 on 16/5/15.
 */
public class TopViewService extends Service {
    static private SharedPreferences prefs;
    int bubbleX, bubbleY;

    WindowManager windowManager;
    WindowManager.LayoutParams params;

    boolean forbidden = false;

    RelativeLayout topView;
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

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        bubbleX = prefs.getInt("bubble_x", 240);
        bubbleY = prefs.getInt("bubble_y", 240);

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
        btnLock = (ImageButton) topView.findViewById(R.id.button_lock);

        topView.setOnTouchListener(new View.OnTouchListener() {
            boolean moved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ToastUtils.showToast(getApplicationContext(), getString(R.string.screen_locked));

                        moved = false;
                        btnLock.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blinking_action));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moved = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        btnLock.clearAnimation();
                        if (!moved) {
                            ToastUtils.showToast(getApplicationContext(), getString(R.string.screen_locked));
                            ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.6f, 1.0f, 0.6f,
                                    100, 100);
                            scaleAnimation.setDuration(300);
                            scaleAnimation.setRepeatCount(5);
                            scaleAnimation.setRepeatMode(Animation.REVERSE);
                            btnLock.startAnimation(scaleAnimation);
                        }
                        break;
                }

                return true;
            }
        });

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forbidden) {
                    btnLock.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_action));
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    params.alpha = 0.8f;
                    windowManager.updateViewLayout(topView, params);
                    btnLock.setImageResource(R.drawable.ic_lock_open_orange_24dp);
                    btnLock.setBackgroundResource(R.drawable.shape);
                    forbidden = false;
                    Toast.makeText(getApplicationContext(), R.string.screen_unlocked, Toast.LENGTH_SHORT).show();
                } else {
                    btnLock.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.re_rotate_action));
                    params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    params.alpha = 0.5f;
                    windowManager.updateViewLayout(topView, params);
                    //btnLock.setImageResource(R.mipmap.ic_launcher);
                    btnLock.setImageDrawable(null);
                    btnLock.setBackgroundResource(R.mipmap.ic_launcher);
                    forbidden = true;
                    Toast.makeText(getApplicationContext(), R.string.screen_locked, Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnLock.setOnTouchListener(new View.OnTouchListener() {
            boolean moved = false;
            int lastX, lastY;
            int paramX, paramY;

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnLock.setPressed(true);
                        moved = false;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();

                            paramX = params.x;
                            paramY = params.y;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        moved = true;
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x = paramX + dx;
                        params.y = paramY + dy;
                        windowManager.updateViewLayout(topView, params);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnLock.setPressed(false);
                        if (!moved) {
                            btnLock.performClick();
                        } else {
                            bubbleX = params.x;
                            bubbleY = params.y;
                            prefs.edit().putInt("bubble_x", bubbleX).apply();
                            prefs.edit().putInt("bubble_y", bubbleY).apply();
                        }
                        break;
                }
                return true;
            }
        });

        windowManager = (WindowManager) getApplicationContext().getSystemService(
                Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.x = bubbleX;
        params.y = bubbleY;
        params.alpha = 0.8f;

        windowManager.addView(topView, params);
    }
}
