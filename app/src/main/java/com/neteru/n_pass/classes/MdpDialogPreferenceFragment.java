package com.neteru.n_pass.classes;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.PreferenceDialogFragmentCompat;

import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.databases.DatabaseManager;

import net.sqlcipher.database.SQLiteDatabase;

public class MdpDialogPreferenceFragment extends PreferenceDialogFragmentCompat {

    private EditText old_mdp, new_mdp, confirm_mdp;
    private DatabaseManager databaseManager;
    private Context context;

    private MdpDialogPreferenceFragment(Context context){
        this.context = context;
    }

    public static MdpDialogPreferenceFragment getInstance(Context context, String key){
        MdpDialogPreferenceFragment fragment = new MdpDialogPreferenceFragment(context);
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        InitializeSQLCipher();

        old_mdp = view.findViewById(R.id.old_mdp);
        new_mdp = view.findViewById(R.id.new_mdp);
        confirm_mdp = view.findViewById(R.id.confirm_new_mdp);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            // get value from editFields, do whatever you want here :)
            // you can acces them through mView variable very easily

            if (!old_mdp.getText().toString().isEmpty() &&
                    !new_mdp.getText().toString().isEmpty() &&
                    !confirm_mdp.getText().toString().isEmpty()){

                if (AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getMdp().equals(AppUtilities.getInstance(context).hashIt(old_mdp.getText().toString()))){

                    if (new_mdp.getText().toString().equals(confirm_mdp.getText().toString())){

                        long result = databaseManager.db_updateProfilMdp(AppUtilities.encrypt(AppUtilities.getInstance(context).hashIt(new_mdp.getText().toString()),
                                AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getKey())[0]);
                        databaseManager.close();

                        if (result == 0)
                            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(context, R.string.no_conform_mdp, Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(context, R.string.old_mdp_false, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(context);
        databaseManager = new DatabaseManager(context);
        databaseManager.getWritableDatabase(AppUtilities.getDbKey(context));
        databaseManager.getReadableDatabase(AppUtilities.getDbKey(context));
    }
}
