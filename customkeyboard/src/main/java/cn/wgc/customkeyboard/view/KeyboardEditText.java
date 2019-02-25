package cn.wgc.customkeyboard.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import cn.wgc.customkeyboard.R;

import java.lang.reflect.Method;
import java.util.List;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/14
 *     desc   : 自定义键盘，包含数字键盘，身份证键盘，密码键盘
 *     version: 1.0
 * </pre>
 */
public class KeyboardEditText extends AppCompatEditText implements View.OnTouchListener, View.OnFocusChangeListener {
    private Context context;
    private Keyboard keyboardNumber;
    private Keyboard keyboardNumberText;
    private Keyboard keyboardPwd;
    private Keyboard keyboardIDCard;
    private Keyboard defaultKeyboard;
    private KeyboardView keyboardView;
    private Drawable mIcEyeOpen;
    private Drawable mIcEyeClose;
    private boolean cipherEnable = true; //是否为密文
    private boolean isAnim = false; //控制隐藏动画防止用户快速点击动画重复
    private boolean enableInput = true; // 控制隐藏动画是还可点击键盘
    public static final int NUMBER_TYPE = 0; //数字键盘类型
    public static final int PWD_TYPE = 1; //密码键盘类型
    public static final int ID_CARD_TYPE = 2; //身份証键盘类型
    public static final int NUMBER_TEXT_TYPE = 3; //身份证有效期键盘类型
    private int type;
    private boolean textEnable;
    private ObjectAnimator showAnimator;
    private ObjectAnimator hideAnimator;

    public KeyboardEditText(Context context) {
        this(context, null);
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.KeyboardEditText);
        type = typedArray.getInt(R.styleable.KeyboardEditText_keyboardType, NUMBER_TYPE);
        textEnable = typedArray.getBoolean(R.styleable.KeyboardEditText_textEnable, true);
        typedArray.recycle();

        mIcEyeOpen = ContextCompat.getDrawable(context, R.drawable.keyboard_eye_open);
        mIcEyeClose = ContextCompat.getDrawable(context, R.drawable.keyboard_eye_close);
        keyboardPwd = new Keyboard(context, R.xml.keyboard_pwd);
        keyboardNumber = new Keyboard(context, R.xml.keyboard_num);
        keyboardNumberText = new Keyboard(context, R.xml.keyboard_num_text);
        keyboardIDCard = new Keyboard(context, R.xml.keyboard_id_card);

        setOnTouchListener(this);
        setOnFocusChangeListener(this);
        setFocusableInTouchMode(true); //初始化设置触摸事件可获取焦点,在每次触摸时就可避免每次去requestFocusFromTouch()
        setKeyType(type);
        setLongClickable(false);
    }

    public void setKeyType(final int type) {
        View decorView = ((AppCompatActivity) context).getWindow().getDecorView();
        FrameLayout contentParent = decorView.findViewById(android.R.id.content);
        if (contentParent.findViewById(R.id.view_keyboard) == null) {
            keyboardView = (KeyboardView) LayoutInflater.from(context).inflate(R.layout.view_content_keyboard, null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            keyboardView.setLayoutParams(layoutParams);
            contentParent.addView(keyboardView);
        } else {
            keyboardView = contentParent.findViewById(R.id.view_keyboard);
        }
        switch (type) {
            case ID_CARD_TYPE:
                defaultKeyboard = keyboardIDCard;
                break;
            case NUMBER_TEXT_TYPE:
                defaultKeyboard = keyboardNumberText;
                break;
            case PWD_TYPE:
                defaultKeyboard = keyboardPwd;
                break;
            case NUMBER_TYPE:
            default:
                if (!textEnable) {
                    keyboardNumber.getKeys().get(9).label = "";
                }
                defaultKeyboard = keyboardNumber;
                break;
        }
        keyboardView.setKeyboard(defaultKeyboard);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {
            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                if (!enableInput)
                    return;
                Editable editable = getText();
                int index = getSelectionStart();//光标位置
                switch (primaryCode) {
                    case Keyboard.KEYCODE_DELETE://回退
                        if (editable != null && editable.length() > 0) {
                            if (index > 0) {
                                editable.delete(index - 1, index);
                            }
                        }
                        break;
                    case 9995://重输
                        setText("");
                        break;
                    case 9994://左移
                        if (index > 0) {
                            setSelection(index - 1);
                        }
                        break;
                    case 9996://右移
                        if (index < editable.length()) {
                            setSelection(index + 1);
                        }
                        break;
                    case -50:
                        if (textEnable)
                            hideKeyboard();
                        break;
                    case -66:
                        getText().clear();
                        editable.insert(0, "长期有效");
                        hideKeyboard();
                        break;
                    case -4399:
                        List<Keyboard.Key> keys = keyboardPwd.getKeys();
                        for (int i = 0; i < keys.size(); i++) {
                            Keyboard.Key key = keys.get(i);
                            if (key.codes[0] == -4399) {
                                key.icon = cipherEnable ? mIcEyeClose : mIcEyeOpen;
                                keyboardPwd.getKeys().set(i, key);
                                setKeyType(type);
                                KeyboardEditText.this.cipherEnable = !cipherEnable;
                                setCipherEnable(cipherEnable);
                                invalidate();
                            }
                        }
                        break;
                    default:
                        editable.insert(index, Character.toString((char) primaryCode));
                        break;
                }
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeUp() {
            }
        });
        keyboardView.bringToFront(); //将键盘控件置于最顶端
    }

    private void hideKeyboard(boolean enableInput) {
        this.enableInput = enableInput;
        if (isAnim)
            return;
        isAnim = true;
        //android 9.0 的华为 荣耀手机 这里需要重置，不然输入到一半后隐藏动画出现重影 ,考效果的可以注释看下
        keyboardView.setKeyboard(defaultKeyboard);
        startHideAnimator(keyboardView);
    }

    public void hideKeyboard() {
        //控制隐藏键時還能輸入的問題
        hideKeyboard(false);
    }

    private void showKeyboard() {
        keyboardView.bringToFront(); //将键盘控件置于最顶端
        keyboardView.clearAnimation();
        notSystemSoftInput();
        if (keyboardView.getVisibility() == View.VISIBLE) {
            return;
        }
        keyboardView.setVisibility(View.VISIBLE);
        startShowAnimator(keyboardView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hideInput(context, this);
                requestFocus();
                showKeyboard();
                break;
        }
        return false;
    }

    /**
     * 强制隐藏输入法键盘
     *
     * @param context Context
     * @param view    EditText
     */
    public void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 屏蔽系统输入法
     */
    public void notSystemSoftInput() {
        ((Activity) getContext()).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        try {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.setAccessible(true);
            setShowSoftInputOnFocus.invoke(this, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            hideInput(context, this);
            setKeyType(type);
            showKeyboard();
        } else {
            if (keyboardView.getVisibility() == View.VISIBLE && !keyboardView.hasWindowFocus())
                hideKeyboard();
        }
    }

    protected void setCipherEnable(boolean cipherEnable) {
    }

    private void startShowAnimator(KeyboardView keyboardView) {
        showAnimator = ObjectAnimator.ofFloat(keyboardView, "translationY", keyboardView.getHeight(), 0);
        showAnimator.setInterpolator(new AccelerateInterpolator());
        showAnimator.setDuration(150);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
//        showAnimator.setStartDelay(delay);
        showAnimator.start();
    }


    private void startHideAnimator(final KeyboardView keyboardView) {
        hideAnimator = ObjectAnimator.ofFloat(keyboardView, "translationY", 0, keyboardView.getHeight());
        hideAnimator.setInterpolator(new LinearInterpolator());
        hideAnimator.setDuration(150);
        hideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                keyboardView.setVisibility(View.INVISIBLE);
                isAnim = false;
                enableInput = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
//        hideAnimator.setStartDelay(delay);
        hideAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (hideAnimator != null)
            hideAnimator.cancel();
        if (showAnimator != null)
            showAnimator.cancel();
    }
}
