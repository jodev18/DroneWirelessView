package dev.jojo.agilus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        h = new Handler(this.getMainLooper());

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent goLogin = new Intent(SplashActivity.this,LoginActivity.class);
                startActivity(goLogin);
                finish();
            }
        },1800);


    }
}
