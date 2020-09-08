package com.neteru.n_pass.activities.second;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.prime.CentralActivity;
import com.neteru.n_pass.activities.prime.PortalActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Account;

import net.sqlcipher.database.SQLiteDatabase;

public class AddAccountActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private EditText account_name, account_username, account_mail, account_mdp, account_notes;
    private String title, username, mail, mdp, type, notes, key, date;
    private Button add_account, generate_mdp;
    private TextView show_types_txt;
    private FrameLayout show_types;
    private LinearLayout types_box;
    private Animation slideUp, slideDown;
    private String radioValue;
    private DatabaseManager databaseManager;
    private LoadingDialog loadingDialog;
    private int task;
    private String[] radioValueList;
    private AppCompatRadioButton[] radioButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);
        InitializeSQLCipher();
        setResult(1);

        loadingDialog = new LoadingDialog(AddAccountActivity.this);
        preferences = PreferenceManager.getDefaultSharedPreferences(AddAccountActivity.this);
        editor = preferences.edit();

        account_name = findViewById(R.id.account_name);
        account_username = findViewById(R.id.account_username);
        account_mail = findViewById(R.id.account_mail);
        account_mdp = findViewById(R.id.account_mdp);
        account_notes = findViewById(R.id.account_notes);

        AppCompatRadioButton facebook = findViewById(R.id.facebook);
        AppCompatRadioButton google = findViewById(R.id.google);
        AppCompatRadioButton instagram = findViewById(R.id.instagram);
        AppCompatRadioButton twitter = findViewById(R.id.twitter);
        AppCompatRadioButton whatsapp = findViewById(R.id.whatsapp);
        AppCompatRadioButton telegram = findViewById(R.id.telegram);
        AppCompatRadioButton email = findViewById(R.id.email);
        AppCompatRadioButton youtube = findViewById(R.id.youtube);
        AppCompatRadioButton linkedin = findViewById(R.id.linkedin);
        AppCompatRadioButton other = findViewById(R.id.other_types);

        radioValueList = new String[]{"facebook", "google", "instagram", "twitter", "whatsapp", "other", "telegram", "email", "youtube", "linkedin"};
        radioButtons = new AppCompatRadioButton[]{facebook, google, instagram, twitter, whatsapp, other, telegram, email, youtube, linkedin};

        add_account = findViewById(R.id.account_add);
        generate_mdp = findViewById(R.id.generate_mdp);

        show_types_txt = findViewById(R.id.show_account_types_txt);

        show_types = findViewById(R.id.show_account_types);

        types_box = findViewById(R.id.account_types);

        slideUp = AnimationUtils.loadAnimation(AddAccountActivity.this, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(AddAccountActivity.this, R.anim.slide_down);

        task = getIntent().getIntExtra("task", 0);

        String label;
        if (task == 0){

            label = getString(R.string.add_mdp_title);
            radioChecking(5);

        }else {

            label = getString(R.string.edit_title);
            title = getIntent().getStringExtra("title");
            username = getIntent().getStringExtra("username");
            mail = getIntent().getStringExtra("mail");
            mdp = getIntent().getStringExtra("mdp");
            type = getIntent().getStringExtra("type");
            notes = getIntent().getStringExtra("notes");
            key = getIntent().getStringExtra("key");
            date = getIntent().getStringExtra("date");

            add_account.setText(R.string.to_save);
            setEditionDefaultValue();

        }

        editor.putBoolean("operator", false).apply();

        masterClick();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(label);
        }
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(AddAccountActivity.this);
    }

    @Override
    public void onBackPressed() {
        editor.putBoolean("addOperator", false).apply();

        if (preferences.getBoolean("operator", false) && getIntent().hasExtra("source")){

            Intent i = new Intent(AddAccountActivity.this, CentralActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }
        super.onBackPressed();
    }

    public void setEditionDefaultValue(){

        for (int i = 0; i < radioValueList.length; i++){
            if (type.equals(radioValueList[i])){
                radioChecking(i);
            }
        }

        account_name.setText(title);
        account_username.setText(username);
        account_mail.setText(mail);
        account_mdp.setText(mdp);
        account_notes.setText(notes);

    }

    public void masterClick(){

        setDrawable(R.mipmap.ic_expand_less_black_18dp);

        show_types.setClickable(true);
        show_types.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (types_box.getVisibility() == View.VISIBLE){

                    types_box.setVisibility(View.GONE);
                    types_box.startAnimation(slideDown);
                    setDrawable(R.mipmap.ic_expand_less_black_18dp);

                }else {

                    types_box.setVisibility(View.VISIBLE);
                    types_box.startAnimation(slideUp);
                    setDrawable(R.mipmap.ic_expand_more_black_18dp);

                }
            }
        });

        for (int i = 0; i < radioButtons.length; i++){

            final int y = i;
            radioButtons[y].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    radioChecking(y);
                }
            });

        }

        generate_mdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account_mdp.setText(AppUtilities.getInstance(AddAccountActivity.this).generatePassword());
            }
        });

        add_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!account_name.getText().toString().isEmpty() &&
                        !account_mdp.getText().toString().isEmpty()){

                        new addAccountTask(AddAccountActivity.this).execute();

                }else {

                    Toast.makeText(AddAccountActivity.this, R.string.fill_the_fields, Toast.LENGTH_SHORT).show();

                }
            }
        });

        generate_mdp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddAccountActivity.this);
                CharSequence[] sequences = new CharSequence[]{getString(R.string.uppercase),getString(R.string.lowercase),getString(R.string.numbers),getString(R.string.symbols)};
                boolean[] checked = new boolean[]{
                        preferences.getBoolean("gen_majuscules",true),
                        preferences.getBoolean("gen_minuscules",true),
                        preferences.getBoolean("gen_chiffres",true),
                        preferences.getBoolean("gen_symboles",false)};

                builder
                        .setTitle(R.string.mdp_gen_title)
                        .setIcon(R.mipmap.n_pass_launcher)
                        .setMultiChoiceItems(sequences, checked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                                switch (i){
                                    case 0:
                                        if (b){
                                            editor.putBoolean("gen_majuscules", true);
                                        }else {
                                            editor.putBoolean("gen_majuscules", false);
                                        }
                                        break;

                                    case 1:
                                        if (b){
                                            editor.putBoolean("gen_minuscules", true);
                                        }else {
                                            editor.putBoolean("gen_minuscules", false);
                                        }
                                        break;

                                    case 2:
                                        if (b){
                                            editor.putBoolean("gen_chiffres", true);
                                        }else {
                                            editor.putBoolean("gen_chiffres", false);
                                        }
                                        break;

                                    case 3:
                                        if (b){
                                            editor.putBoolean("gen_symboles", true);
                                        }else {
                                            editor.putBoolean("gen_symboles", false);
                                        }
                                        break;
                                }
                                editor.apply();

                            }
                        })
                        .show();

                return false;
            }
        });
    }

    public void insertValue(){
        databaseManager.db_insertValue(AppUtilities.cryptAccount(new Account(
                account_name.getText().toString(),
                account_username.getText().toString(),
                account_mail.getText().toString(),
                radioValue,
                account_mdp.getText().toString(),
                account_notes.getText().toString(),
                AppUtilities.getDate(),
                "")));

        databaseManager.close();
        editor.putBoolean("operator", true).apply();
        setResult(0);
    }

    public void radioChecking(int indice){

        for (int i = 0; i < radioButtons.length; i++) {
            if (i == indice){
                radioButtons[i].setChecked(true);
                radioValue = radioValueList[i];
            }else {
                radioButtons[i].setChecked(false);
            }
        }

    }

    public void setDrawable(int res){
        Drawable drawable = ContextCompat.getDrawable(AddAccountActivity.this, res);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(AddAccountActivity.this, R.color.colorPrimary));
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            show_types_txt.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferences.getBoolean("addOperator", false)){

            if (loadingDialog.isShowing()){ loadingDialog.dismiss(); }
            Intent intent = new Intent(AddAccountActivity.this, PortalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }else{
            editor.putBoolean("addOperator", true).apply();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        editor.putBoolean("addOperator", false).apply();

        if (preferences.getBoolean("operator", false) && getIntent().hasExtra("source")){

            Intent i = new Intent(AddAccountActivity.this, CentralActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }else {

            finish();

        }

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    class addAccountTask extends AsyncTask<String, Void, Void>{
        private Context context;

        addAccountTask(Context ctx){
            context = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
            loadingDialog.setMsg(getString(R.string.be_patient));
        }

        @Override
        protected Void doInBackground(String... strings) {

            databaseManager = new DatabaseManager(context);
            databaseManager.getWritableDatabase(AppUtilities.getDbKey(AddAccountActivity.this));
            databaseManager.getReadableDatabase(AppUtilities.getDbKey(AddAccountActivity.this));

            if (task == 0){

                insertValue();

            }else{

                databaseManager.db_removeValue(AppUtilities.cryptAccount(new Account(title, username, mail, type, mdp, notes, date, key)));
                insertValue();

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (task == 0){

                account_name.setText("");
                account_username.setText("");
                account_mail.setText("");
                account_mdp.setText("");
                account_notes.setText("");

                Toast.makeText(AddAccountActivity.this, R.string.mdp_added, Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(AddAccountActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
            }

            loadingDialog.dismiss();
        }
    }
}
