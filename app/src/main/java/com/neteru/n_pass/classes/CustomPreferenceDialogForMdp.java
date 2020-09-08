package com.neteru.n_pass.classes;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import com.neteru.n_pass.R;

/**
 * Created by Ark Noam on 14/09/2018.
 */
@SuppressWarnings("unused")
public class CustomPreferenceDialogForMdp  extends DialogPreference {

    private int mDialogLayoutResId = R.layout.mdp_modify_layout;

    public CustomPreferenceDialogForMdp(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogTitle(context.getString(R.string.mdp_portal_title));
        setPersistent(false);
        setDialogLayoutResource(mDialogLayoutResId);

    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }
}