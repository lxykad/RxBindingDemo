package com.lxy.binding;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lxy.binding.annimation.AnimatorActivity;

/**
 * @author a
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // 常见用法
    public void click1(View view) {
        startActivity(new Intent(this, DoubleClickActivity.class));
    }

    //操作符
    public void clickOperator(View view) {
        startActivity(new Intent(this, OperatorActivity.class));
    }

    /**
     * rx高级进阶
     *
     * @param view
     */
    public void clickNew(View view) {
        startActivity(new Intent(view.getContext(), RxActivity.class));
    }

    public void clickAnimation(View view) {
        startActivity(new Intent(view.getContext(), AnimatorActivity.class));
    }
}
