package com.neteru.n_pass.activities.prime.firstlaunch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.prime.CentralActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.Typewriter;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Profil;

import net.sqlcipher.database.SQLiteDatabase;

public class FirstlaunchTwoActivity extends AppCompatActivity {
    private EditText mail;
    private String mdp;
    private SharedPreferences preferences;
    private DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstlaunch_two);
        new initDBTask(this).execute();

        mdp = getIntent().getStringExtra("mdp");
        mail = findViewById(R.id.editor_1);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Typewriter order = findViewById(R.id.order);
        order.animateText(getString(R.string.recovery_mail));

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mail.getText().toString().isEmpty()){

                    if (AppUtilities.validateMail(mail.getText().toString())){

                        new progressDialog(FirstlaunchTwoActivity.this).execute(mail.getText().toString());

                    }else {
                        Toast.makeText(FirstlaunchTwoActivity.this, R.string.invalid_mail, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.getWritableDatabase(AppUtilities.getDbKey(this));
    }

    @SuppressLint("StaticFieldLeak")
    class progressDialog extends AsyncTask<String, Void, Void>{
        private Context context;
        private LoadingDialog loadingDialog;

        progressDialog(Context ctx){
            context = ctx;
            loadingDialog = new LoadingDialog(ctx);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
            loadingDialog.setMsg(getString(R.string.init));

        }

        @Override
        protected Void doInBackground(String... strings) {

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("mail", strings[0]);
            editor.putBoolean("firstLaunch", false);
            editor.apply();

            databaseManager.db_setProfil(AppUtilities.cryptProfil(new Profil(mdp, strings[0], "")));
            databaseManager.close();

            loadingDialog.dismiss();

            Intent intent = new Intent(context, CentralActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class initDBTask extends AsyncTask<String, Void, Void>{
        private Context context;
        private LoadingDialog loadingDialog;

        initDBTask(Context ctx){
            context = ctx;
            loadingDialog = new LoadingDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
        }

        @Override
        protected Void doInBackground(String... strings) {
            InitializeSQLCipher();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loadingDialog.dismiss();

        }
    }
}
