package cn.keyboard.demo;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class KeyboardFragmentAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> datas = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();

    public KeyboardFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public ArrayList<Fragment> getDatas() {
        return datas;
    }

    public void setDatas(ArrayList<Fragment> datas) {
        this.datas = datas;
    }

    public ArrayList<String> getTitles() {
        return titles;
    }

    public void setTitles(ArrayList<String> titles) {
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int i) {
        return datas.size() == 0 ? null : datas.get(i);
    }

    @Override
    public int getCount() {
        return datas.size() == 0 ? null : datas.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.size() == 0 ? null : titles.get(position);
    }
}
