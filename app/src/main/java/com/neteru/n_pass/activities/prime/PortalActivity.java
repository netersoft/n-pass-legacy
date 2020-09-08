package com.neteru.n_pass.activities.prime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.second.MailActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Profil;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.List;

public class PortalActivity extends AppCompatActivity {
    private EditText mdp;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);
        InitializeSQLCipher();

        mdp = findViewById(R.id.mdp);
        Button enter = findViewById(R.id.enter);
        TextView lost_mdp = findViewById(R.id.lost_mdp);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mdp.getText().toString().isEmpty()){

                    new progressAsync(PortalActivity.this).execute(mdp.getText().toString());

                }
            }
        });

        lost_mdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PortalActivity.this, MailActivity.class);
                startActivity(intent);

            }
        });
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
    }

    @SuppressLint("StaticFieldLeak")
    class progressAsync extends AsyncTask<String, Void, Void>{
        private Context context;
        private LoadingDialog loadingDialog;
        private boolean result;


        progressAsync(Context ctx){
            context = ctx;
            loadingDialog = new LoadingDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
            loadingDialog.setMsg(getString(R.string.loading));
            loadingDialog.setCancelable();
        }

        @Override
        protected Void doInBackground(String... strings) {

            DatabaseManager databaseManager = new DatabaseManager(context);
            databaseManager.getReadableDatabase(AppUtilities.getDbKey(PortalActivity.this));
            List<Profil> profil = AppUtilities.decryptProfilList(databaseManager.db_getProfil());
            databaseManager.close();

            String mdp_ = profil.get(0).getMdp(), mdpHash = AppUtilities.getInstance(context).hashIt(strings[0]);

            if (mdpHash != null){

                result = mdp_.equals(mdpHash);

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loadingDialog.dismiss();

            if (result){

                Intent intent = new Intent(context, CentralActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                editor
                        .putBoolean("centralOperator", false)
                        .putBoolean("addOperator", false)
                        .putBoolean("searchOperator", false)
                        .putBoolean("settingsOperator", false)
                        .apply();
                startActivity(intent);

            }else {

                mdp.setText("");
                editor.putInt("failCounter", preferences.getInt("failCounter", 0) + 1).apply();
                Toast.makeText(context, R.string.false_mdp, Toast.LENGTH_SHORT).show();

            }

        }

    }
}
