package cn.keyboard.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PwdFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View  view= getActivity().getLayoutInflater().inflate(R.layout.fragment_pwd, container, false);
        return view;
    }
}
