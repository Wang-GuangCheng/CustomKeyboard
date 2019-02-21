package cn.wgc.customkeyboard.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import cn.wgc.customkeyboard.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/14
 *     desc   : 密码控件
 *     version: 1.0
 * </pre>
 */

public class  PwdEditText extends KeyboardEditText {

    private final int PWD_LENGTH = 8;//密码长度
    private int mWidth; //宽度
    private int mHeight;//高度
    private Paint mPwdPaint; //密码画笔
    private Paint mRectPaint; //密码框画笔
    private int mInputLength; //输入的密码长度
    private OnInputFinishListener mOnInputFinishListener;//输入结束监听
    private String text = "";
    private int mTextWidth;
    private int mTextHeight;
    private int cursorPosition;//光标位置
    private int cursorWidth = dp2px(2);//光标粗细
    private int cursorHeight;//光标长度
    private int cursorColor;//光标颜色
    private int pwdColor;//光标颜色
    private int backColor;//背景颜色
    private boolean isCursorShowing = false;//光标是否正在显示
    private boolean isCursorEnable = true;//是否开启光标
    private boolean isInputComplete;//是否输入完毕
    private long cursorFlashTime;//光标闪动间隔时间
    private boolean cipherEnable;//是否开启密文
    private float radius; //圆形密码的大小
    private float rectRadius; //密码框的圆角大小
    private int pwdSize; //密码字体大小
    private int mRectWidth;

    private Timer timer;
    private TimerTask timerTask;

    public PwdEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(attrs);
        initPaint();
    }

    public PwdEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(attrs);
        initPaint();
    }

    private void initAttribute(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.PwdEditText);
        cursorColor = typedArray.getColor(R.styleable.PwdEditText_cursorColor, Color.GRAY);
        backColor = typedArray.getColor(R.styleable.PwdEditText_backColor, Color.WHITE);
        pwdColor = typedArray.getColor(R.styleable.PwdEditText_pwdColor, Color.GRAY);
        isCursorEnable = typedArray.getBoolean(R.styleable.PwdEditText_isCursorEnable, true);
        radius = typedArray.getDimensionPixelSize(R.styleable.PwdEditText_roundRadius, dp2px(7));
        rectRadius = typedArray.getDimensionPixelSize(R.styleable.PwdEditText_rectRadius, dp2px(7));
        pwdSize = typedArray.getDimensionPixelSize(R.styleable.PwdEditText_pwdSize, sp2px(12));
        cursorHeight = typedArray.getDimensionPixelSize(R.styleable.PwdEditText_cursorHeight, dp2px(15));
        cipherEnable = typedArray.getBoolean(R.styleable.PwdEditText_cipherEnable, true);
        cursorFlashTime = typedArray.getInteger(R.styleable.PwdEditText_cursorFlashTime, 500);
        typedArray.recycle();
        setLongClickable(false);
    }


    private void initPaint() {
        mPwdPaint = new Paint();
        mPwdPaint.setColor(pwdColor);
        mPwdPaint.setStyle(Paint.Style.FILL);
        mPwdPaint.setAntiAlias(true);
        mPwdPaint.setTextSize(pwdSize);

        String num = "8";
        Rect rect = new Rect();
        mPwdPaint.getTextBounds(num, 0, num.length(), rect);
        //文字宽
        mTextWidth = rect.width();
        //文字高
        mTextHeight = rect.height();

        // 初始化密码框
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setColor(Color.LTGRAY);
        mRectPaint.setAntiAlias(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        timer = new Timer();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        int height = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                break;
            case MeasureSpec.EXACTLY:
                //指定大小，宽度 = 指定的大小
                width = MeasureSpec.getSize(widthMeasureSpec);
                //密码框大小等于 (宽度 - 密码框间距 *(密码位数 - 1)) / 密码位数
                height = (width) / PWD_LENGTH;
                break;
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        mWidth = getWidth();
        mHeight = getHeight();

        // 计算每个密码框宽度
        mRectWidth = mWidth / PWD_LENGTH;

        drawPwdRect(canvas);
        drawPwd(canvas);
        drawCursor(canvas, new Paint());
    }

    private void drawPwd(Canvas canvas) {
        // 绘制密码
        for (int i = 0; i < mInputLength; i++) {
            int cx = mRectWidth / 2 + mRectWidth * i;
            int cy = mHeight / 2;
            if (cipherEnable) {
                canvas.drawCircle(cx, cy, radius, mPwdPaint);
            } else {
                canvas.drawText(text.substring(i, i + 1), cx - mTextWidth / 2, mHeight / 2 + mTextHeight / 2, mPwdPaint);
            }
        }
    }

    private void drawPwdRect(Canvas canvas) {

        // 绘制白底和密码框
        Paint paint = new Paint();
        paint.setColor(backColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(0, 0, mWidth, mHeight,rectRadius, rectRadius, paint);
            canvas.drawRoundRect(0, 0, mWidth, mHeight, rectRadius, rectRadius, mRectPaint);
        } else {
            canvas.drawRect(0, 0, mWidth, mHeight, paint);
            canvas.drawRect(0, 0, mWidth, mHeight, mRectPaint);
        }

        for (int i = 0; i < PWD_LENGTH; i++) {
            int startX = mRectWidth * i;
            if (i == 0) continue;
            canvas.drawLine(startX, 0, startX, mHeight, mRectPaint);
        }
    }


    /**
     * 绘制光标
     */
    private void drawCursor(Canvas canvas, Paint paint) {
        paint.setColor(cursorColor);
        paint.setStrokeWidth(cursorWidth);
        paint.setStyle(Paint.Style.FILL);
        if (!isCursorShowing && isCursorEnable && !isInputComplete && hasFocus()) {
            canvas.drawLine(mRectWidth * cursorPosition + mRectWidth / 2,
                    (mHeight - cursorHeight) / 2,
                    mRectWidth * cursorPosition + mRectWidth / 2,
                    mHeight - (mHeight - cursorHeight) / 2,
                    paint);
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start,
                                 int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        this.mInputLength = text.toString().length();
        if (text == null) {
            this.text = "";
        } else {
            this.text = text.toString();
        }
        if (lengthBefore < lengthAfter) {
            if (cursorPosition >= 8) {
                cursorPosition = 8;
            } else {
                cursorPosition++;
            }
        } else {
            if (cursorPosition <= 0) {
                cursorPosition = 0;
            } else {
                cursorPosition--;
            }
        }
        if(text.toString().isEmpty()){
            cursorPosition = 0;
        }
        isInputComplete = (mInputLength == PWD_LENGTH ? true : false);
        invalidate();
        if (mInputLength == PWD_LENGTH && mOnInputFinishListener != null) {
            mOnInputFinishListener.onInputFinish(text.toString(), mInputLength);
        } else if (mInputLength < PWD_LENGTH && mOnInputFinishListener != null) {
            mOnInputFinishListener.onInputUnfinished(mInputLength);
        }
    }


    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //保证光标始终在最后
        if (selStart == selEnd) {
            setSelection(getText().length());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorFlashTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    public interface OnInputFinishListener {
        /**
         * 密码输入结束监听
         *
         * @param password
         */
        void onInputFinish(String password, int length);

        void onInputUnfinished(int length);
    }

    /**
     * 设置输入完成监听
     *
     * @param onInputFinishListener
     */
    public void setOnInputFinishListener(
            OnInputFinishListener onInputFinishListener) {
        this.mOnInputFinishListener = onInputFinishListener;
    }

    @Override
    public void setCipherEnable(boolean cipherEnable) {
        this.cipherEnable = cipherEnable;
        invalidate();
    }

    public boolean isCipherEnable() {
        return cipherEnable;
    }


}
