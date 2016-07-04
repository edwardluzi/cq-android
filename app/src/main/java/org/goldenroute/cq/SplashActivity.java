package org.goldenroute.cq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initialize();
    }

    private void initialize() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                gotoMain();
            }
        }, SPLASH_DELAY_MILLIS);
    }

    private void gotoMain() {
        SplashActivity.this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
        SplashActivity.this.finish();
    }
}
