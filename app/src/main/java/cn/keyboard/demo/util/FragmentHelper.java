package cn.keyboard.demo.util;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class FragmentHelper {
    public static void addFragment(AppCompatActivity activity, Fragment fragment, @IdRes int id) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.add(id, fragment).show(fragment).commit();
    }

    public static void replaceFragment(AppCompatActivity activity, Fragment fragment, @IdRes int id) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(id, fragment).show(fragment).commit();
    }

    public static void removeFragment(AppCompatActivity activity, Fragment fragment, @IdRes int id) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.remove(fragment).commit();
    }
}
