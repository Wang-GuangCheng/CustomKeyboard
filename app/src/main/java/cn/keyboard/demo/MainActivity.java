package cn.keyboard.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import cn.wgc.customkeyboard.BaseKeyboardActivity;

/**
 * <pre>
 *     author : wgc
 *     time   : 2019/02/15
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MainActivity extends BaseKeyboardActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        findViewById(R.id.btn_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, KeyboardActivity.class));
            }
        });
    }
}
