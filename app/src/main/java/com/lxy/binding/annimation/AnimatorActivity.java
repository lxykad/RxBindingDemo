package com.lxy.binding.annimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lxy.binding.R;
import com.lxy.binding.annimation.widget.HeartView;

/**
 * @author a
 */
public class AnimatorActivity extends AppCompatActivity {

    private HeartView heartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animator);

        heartView = (HeartView) findViewById(R.id.heart_view);

        heartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                heartView.startAnim(1500);
            }
        });

    }
}
