package com.neteru.n_pass.activities.prime.firstlaunch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.Typewriter;

public class FirstlaunchOneActivity extends AppCompatActivity {
    private EditText mdp, mdp_confirm;
    private ImageView icon;
    private Animation slideUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstlaunch_one);

        mdp = findViewById(R.id.editor_1);
        mdp_confirm = findViewById(R.id.editor_2);
        icon = findViewById(R.id.topIcon);

        Typewriter order = findViewById(R.id.order);
        order.animateText(getString(R.string.main_password));

        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        final RelativeLayout relativeLayout = findViewById(R.id.relative);
        relativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = relativeLayout.getRootView().getHeight() - relativeLayout.getHeight();
                if (heightDiff > dpToPx(FirstlaunchOneActivity.this, 200)) { // if more than 200 dp, it's probably a keyboard...
                    // ... do something here

                    icon.setVisibility(View.GONE);
                    icon.startAnimation(slideUp);

                }else {

                    icon.setVisibility(View.VISIBLE);
                    icon.startAnimation(slideUp);

                }
            }
        });

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mdp.getText().toString().isEmpty() && !mdp_confirm.getText().toString().isEmpty()){

                    if (mdp.getText().toString().equals(mdp_confirm.getText().toString())){

                        if (mdp.getText().toString().length() > 5){

                            String mdpHash = AppUtilities.getInstance(FirstlaunchOneActivity.this).hashIt(mdp.getText().toString());

                            if (mdpHash != null) {
                                Intent intent = new Intent(FirstlaunchOneActivity.this, FirstlaunchTwoActivity.class);
                                intent.putExtra("mdp", mdpHash);
                                startActivity(intent);
                            }

                        }else {
                            Toast.makeText(FirstlaunchOneActivity.this, R.string.short_mdp, Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(FirstlaunchOneActivity.this, R.string.no_conform_mdp, Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

}
