package com.neteru.n_pass.activities.second;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.prime.PortalActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.CustomPreferenceDialogForMdp;
import com.neteru.n_pass.classes.MdpDialogPreferenceFragment;
import com.neteru.n_pass.classes.databases.DatabaseManager;

import net.sqlcipher.database.SQLiteDatabase;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new mPreferenceFragmentCompat()).commit();
        setResult(1);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
    }

    public static class mPreferenceFragmentCompat extends PreferenceFragmentCompat
    {
        private Activity activity;
        private DatabaseManager databaseManager;
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s.equals("mail")){

                    String mail = sharedPreferences.getString("mail","");

                    databaseManager.db_updateProfilMail(AppUtilities.encrypt(mail,
                            AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getKey())[0]);
                    databaseManager.close();

                }else if(s.equals("hidePassword") || s.equals("deleteBut") || s.equals("swipingAct")){

                    if (getActivity() != null)
                    getActivity().setResult(0);

                }
            }
        };

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey);

            if (getActivity() != null) {activity = getActivity();}

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            prefs.registerOnSharedPreferenceChangeListener(listener);

            Preference privacy_policy = findPreference("privacy_policy");

            if (privacy_policy != null) {
                privacy_policy.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(androidx.preference.Preference preference) {

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://n-password-manager.flycricket.io/privacy.html")));

                        return false;
                    }
                });
            }
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            InitializeSQLCipher();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {

            DialogFragment dialogFragment = null;
            if (preference instanceof CustomPreferenceDialogForMdp) {
                // Preference
                dialogFragment = MdpDialogPreferenceFragment.getInstance(activity, preference.getKey());
            }


            if (dialogFragment != null) {

                if (getFragmentManager() == null) return;

                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), "CustomPreferenceDialogForMdp");

            } else {

                super.onDisplayPreferenceDialog(preference);
            }

        }

        private void InitializeSQLCipher() {
            SQLiteDatabase.loadLibs(activity);
            databaseManager = new DatabaseManager(getActivity());
            databaseManager.getWritableDatabase(AppUtilities.getDbKey(getActivity()));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferences.getBoolean("settingsOperator", false)){

            Intent intent = new Intent(SettingsActivity.this, PortalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }else{
            editor.putBoolean("settingsOperator", true).apply();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putBoolean("settingsOperator", false).apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        editor.putBoolean("settingsOperator", false).apply();

        finish();
        return true;
    }
}
