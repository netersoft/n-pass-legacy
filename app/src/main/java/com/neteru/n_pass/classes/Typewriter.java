package com.neteru.n_pass.classes;

import android.content.Context;
import android.os.Handler;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by Ark Noam on 15/07/2018.
 */
@SuppressWarnings("unused")
public class Typewriter extends AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 75; //Default 500ms delay

    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Typewriter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex <= mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }

}
