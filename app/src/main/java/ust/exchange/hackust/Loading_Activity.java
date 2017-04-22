package ust.exchange.hackust;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

import com.wang.avi.AVLoadingIndicatorView;
import com.wang.avi.indicators.BallTrianglePathIndicator;

public class Loading_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_);
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                startAnim();
            }

            public void onFinish() {
                endAnim();
            }
        }.start();
    }
    void startAnim() {
        AVLoadingIndicatorView avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.show();
    }

    void endAnim() {
        AVLoadingIndicatorView avi = (AVLoadingIndicatorView) findViewById(R.id.avi);
        avi.hide();
        Intent intent = new Intent(this, Location_Checker_Screen.class);
        startActivity(intent);
    }
}
