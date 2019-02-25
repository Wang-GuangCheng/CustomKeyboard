# <center> CustomKeyboard </center> #
# 1、普通的数字键盘和身份证键盘使用 #

    <cn.wgc.customkeyboard.view.KeyboardEditText
            android:id="@+id/et_phone"
            android:background="@color/white"
            android:layout_width="match_parent"
            android:layout_height="45dp"
            android:hint="请输入手机号码"
            android:padding="5dp"
            app:textEnable="false"
            app:keyboardType="number"
            android:gravity="center_vertical"
            android:maxLength="11"
            android:layout_marginLeft="30dp"
            android:layout_marginRight="30dp"
            android:layout_marginTop="25dp"/>

    <cn.wgc.customkeyboard.view.KeyboardEditText
            android:background="@color/white"
            android:layout_width="match_parent"
            android:layout_height="45dp"
            android:hint="请输入身份号码"
            android:padding="5dp"
            app:keyboardType="idCard"
            android:gravity="center_vertical"
            android:maxLength="18"
            android:layout_marginLeft="30dp"
            android:layout_marginRight="30dp"
            android:layout_marginTop="25dp"/>



|可控属性|属性介绍 | 值 |
|---|---|---|
|keyboardType|控制键盘类型默认数字键盘 | number、idCard、numberText |
|textEnable|数字键盘是否带文字键盘，点击隐藏键盘 | true、false|

# 2、带密码框的密码键盘使用方式 #

    <cn.wgc.customkeyboard.view.PwdEditText
            android:id="@+id/et_pwd"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginLeft="30dp"
            android:layout_marginRight="30dp"
            android:layout_marginTop="25dp"
            android:background="@null"
            android:cursorVisible="false"
            app:keyboardType="pwd"
            app:isCursorEnable="true"
            app:pwdColor="@color/colorAccent"/>

|可控属性|属性介绍 | 值或类型 |
|---|---|---|
|keyboardType|控制键盘类型默认数字键盘 | pwd |
|roundRadius|  密码的大小  | dimension|
|pwdSize|  密码字体大小  | dimension|
|pwdLength|  密码长度  | integer|
|rectRadius|  密码框圆角的大小  | dimension|
|cursorHeight|  光标大小  | dimension|
|cursorFlashTime|  光标刷新时间  | integer|
|isCursorEnable|  自定义光标是否开启  | boolean|
|cipherEnable|  密文是否开启  | boolean|
|cursorColor|  光标颜色  | color|
|pwdColor|  密码的颜色  | color|
|backColor|  EditText背景颜色  | color|


# 3、点击外部隐藏键盘或者点击物理返回键隐藏键盘 #

使用你的基类Activity继承BaseKeyboardActivity 如：

    public class YourBaseActivity extends BaseKeyboardActivity {
    
	}


或者在你的基类Activity添加如下操作
    public class BaseKeyboardActivity extends AppCompatActivity {

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


效果图
https://i.imgur.com/1mTboRd.gif


Gradle使用方法 

    allprojects {
    	repositories {
        maven { url 'https://jitpack.io' }
    	}
	}

    dependencies {
    implementation 'com.github.Wang-GuangCheng:CustomKeyboard:1.0'
	}
