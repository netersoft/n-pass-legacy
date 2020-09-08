package com.neteru.n_pass;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.neteru.n_pass.activities.prime.firstlaunch.FirstlaunchOneActivity;
import com.neteru.n_pass.activities.prime.PortalActivity;
import com.neteru.n_pass.classes.AppUtilities;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SplashActivity.this);
                Intent intent;

                if (preferences.getBoolean("firstLaunch", true)){

                    if (!preferences.getBoolean("generateKey", false)){

                        preferences.edit()
                                .putString("key", AppUtilities.generateRandomPassword(AppUtilities.NB))
                                .putBoolean("generateKey", true)
                                .apply();

                    }
                    intent = new Intent(SplashActivity.this, FirstlaunchOneActivity.class);

                }else{

                    intent = new Intent(SplashActivity.this, PortalActivity.class);

                }

                startActivityForResult(intent, 4320);

            }
        }, 1000);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        finish();
    }
}
