package cn.wgc.customkeyboard;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import cn.wgc.customkeyboard.view.KeyboardEditText;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/15
 *     desc   : 用于控制点击EditText外部隐藏键盘的基类
 *     version: 1.0
 * </pre>
 */
public class BaseActivity extends AppCompatActivity {

    private int start;
    private FrameLayout contentParent;
    private KeyboardView keyboardView;
    private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            initKeyboardParameters();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initKeyboardView();
        initKeyboardParameters();
    }

    private void initKeyboardView() {
        View decorView = getWindow().getDecorView();
        contentParent = decorView.findViewById(android.R.id.content);
        //初始化fragment中的键盘参数
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
    }

    private void initKeyboardParameters() {
        //初始化activity中的键盘参数
        if (contentParent.findViewById(R.id.view_keyboard) != null) {
            keyboardView = contentParent.findViewById(R.id.view_keyboard);
            keyboardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int[] position = new int[2];
                    keyboardView.getLocationInWindow(position);
                    start = position[1];
                    Log.d("wgc", "  start  " + start);
                    //此处用addOnGlobalLayoutListener在MTK，高通较低端的处理器6.0+的版本上会有问题，拿到的高度是屏幕的
                    // 最大高度，所以判断布局后值小于屏幕的最大高度才移除监听
                    if (start < getScreenHeight()) {
                        keyboardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
                    }
                }
            });
        }
    }

    /**
     * 获取点击事件
     */
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //这里的条件是控制自定义的键盘
        if (start > 0 && ev.getY() > start) {
            return super.dispatchTouchEvent(ev);
        }
        //这里不能用Down事件
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            View view = getCurrentFocus();
            if (isShouldHideKeyBord(view, ev)) {
                //隐藏自定义键盘
                if (view instanceof KeyboardEditText) {
                    ((KeyboardEditText) view).hideKeyboard();
                }
                //隐藏系统键盘
                hideSoftInput(view.getWindowToken());
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 判定当前是否需要隐藏
     */
    protected boolean isShouldHideKeyBord(View v, MotionEvent ev) {
        if ((v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            return !(ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    //获取屏幕的高度
    private int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = dm.heightPixels;
        return screenHeight;
    }


    @Override
    public void onBackPressed() {
        //点击返回隐藏键盘
        if (keyboardView != null && keyboardView.getVisibility() == View.VISIBLE) {
            keyboardView.setVisibility(View.INVISIBLE);
            return;
        }
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销注册fragment事件
        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
    }
}
