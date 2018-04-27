package com.lxy.binding.annimation;

import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lxy.binding.R;
import com.lxy.binding.annimation.widget.HeartView;
import com.lxy.binding.annimation.widget.MovePathView;

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


//
//        Path path1 = new Path();
//        path1.moveTo(310, 0);
//
//        path1.lineTo(310, 400);
//        path1.lineTo(210, 500);
//        path1.lineTo(210, 600);
//        path1.lineTo(310, 700);
//        path1.lineTo(310, 1280);
//
//        MovePathView pathView = (MovePathView) findViewById(R.id.move_path_view);
//        pathView.setPath(path1);
//        pathView.startAnim(2000);
//


    }

    @Override
    protected void onResume() {
        super.onResume();

        // 模拟点击
        heartView.performClick();
    }
}
