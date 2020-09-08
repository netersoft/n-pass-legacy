package com.neteru.n_pass.activities.second;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.Connectivity;
import com.neteru.n_pass.classes.GMailSender;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.databases.DatabaseManager;

import net.sqlcipher.database.SQLiteDatabase;

public class MailActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);
        InitializeSQLCipher();

        TextView mail = findViewById(R.id.modify_mail);
        Button sendIt = findViewById(R.id.send_mdp_by_mail);

        reference = FirebaseDatabase.getInstance().getReference();

        mail.setText(AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getMail());

        sendIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MailActivity.this);
                builder.setTitle(R.string.reinit_title)
                        .setIcon(R.mipmap.n_pass_launcher)
                        .setMessage(R.string.reinit_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (Connectivity.getInstance(MailActivity.this).isOnline()) {

                                    reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {

                                                String mail = dataSnapshot.child("mail").getValue(String.class),
                                                        mdp = dataSnapshot.child("mdp").getValue(String.class);

                                                new mailXender(MailActivity.this).execute(mail, mdp);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                            Toast.makeText(MailActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();

                                        }

                                    });

                                } else {
                                    Toast.makeText(MailActivity.this, R.string.error_connection, Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).show();

            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.reinit_title);
        }
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.getWritableDatabase(AppUtilities.getDbKey(this));
        databaseManager.getReadableDatabase(AppUtilities.getDbKey(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    class mailXender extends AsyncTask<String, Void, Void> {
        private Context context;
        private LoadingDialog loadingDialog;
        private long result;

        mailXender(Context ctx) {
            context = ctx;
            loadingDialog = new LoadingDialog(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
            loadingDialog.setMsg(getString(R.string.reinit_in_progress));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.setMsg(getString(R.string.mail_sending));
                }
            }, AppUtilities.NB / 2);
        }

        @Override
        protected Void doInBackground(String... strings) {

            try {
                String newPassword = AppUtilities.generateRandomPassword(8),
                        newPasswordHash = AppUtilities.getInstance(context).hashIt(newPassword);
                GMailSender sender = new GMailSender(strings[0], strings[1]);

                sender.sendMail(getString(R.string.mail_subject), getString(R.string.mail_content) + newPassword,
                        strings[0], AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getMail());

                databaseManager = new DatabaseManager(MailActivity.this);
                databaseManager.getWritableDatabase(AppUtilities.getDbKey(MailActivity.this));
                databaseManager.getReadableDatabase(AppUtilities.getDbKey(MailActivity.this));

                long r = databaseManager.db_updateProfilMdp(AppUtilities.encrypt(newPasswordHash, AppUtilities.decryptProfilList(databaseManager.db_getProfil()).get(0).getKey())[0]);

                databaseManager.close();

                result = r;

            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
                result = -1;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loadingDialog.dismiss();

            if (result == 0) {
                Toast.makeText(MailActivity.this, R.string.mail_succes, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MailActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
            }

        }
    }
}