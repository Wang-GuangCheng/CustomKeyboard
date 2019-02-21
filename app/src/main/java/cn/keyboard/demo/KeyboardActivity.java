package cn.keyboard.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import cn.keyboard.demo.util.FragmentHelper;
import cn.wgc.customkeyboard.BaseKeyboardActivity;

import java.util.ArrayList;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class KeyboardActivity extends BaseKeyboardActivity implements View.OnClickListener {

    private TabLayout mTabKeyType;
    private ViewPager mViewPager;
    private int currentPosition = 1;
    private ArrayList<Fragment> mFragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_keyboard);
        super.onCreate(savedInstanceState);
        mTabKeyType = findViewById(R.id.tab_key_type);
        mViewPager = findViewById(R.id.view_pager);
        findViewById(R.id.btn_replace).setOnClickListener(this);
        FragmentHelper.addFragment(this, new PwdFragment(), R.id.fl_container);
        initAdapter();
    }

    @Override
    public void onClick(View v) {
        currentPosition++;
        if (currentPosition > 2)
            currentPosition = 0;
        FragmentHelper.replaceFragment(this,
                currentPosition == 0 ? new IDCardFragment() : currentPosition == 1 ? new PwdFragment() : new NumberFragment(),
                R.id.fl_container);
    }

    private void initAdapter() {
        mFragments = new ArrayList<>();
        mFragments.add(new IDCardFragment());
        mFragments.add(new PwdFragment());
        mFragments.add(new NumberFragment());
        ArrayList<String> titles = new ArrayList<>();
        titles.add("身份键盘");
        titles.add("密码键盘");
        titles.add("数字键盘");
        KeyboardFragmentAdapter adapter = new KeyboardFragmentAdapter(getSupportFragmentManager());
        adapter.setDatas(mFragments);
        adapter.setTitles(titles);
        mViewPager.setAdapter(adapter);
        mTabKeyType.setupWithViewPager(mViewPager);
    }
}
