package com.neteru.n_pass.activities.prime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.second.AddAccountActivity;
import com.neteru.n_pass.activities.second.SearchActivity;
import com.neteru.n_pass.activities.second.SettingsActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.Swiping;
import com.neteru.n_pass.classes.Typewriter;
import com.neteru.n_pass.classes.adapters.CentralAdapter;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Account;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CentralActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private LinearLayout noAccountMsg;
    private FloatingActionButton floatingActionButton;
    private Animation slideUp;
    private SwipeRefreshLayout refreshLayout;
    private LoadingDialog loadingDialog;
    private final static int REQUEST_READ_PERMISSION = 0, REQUEST_WRITE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);
        //Initialisation de la BDD
        InitializeSQLCipher();

        //Instanciation des outils et des éléments de la vue
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        noAccountMsg = findViewById(R.id.noAccountMsg);
        recyclerView = findViewById(R.id.recycler);
        refreshLayout = findViewById(R.id.swipeRefresh);

        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        loadingDialog = new LoadingDialog(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        floatingActionButton = findViewById(R.id.fab_1);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putBoolean("centralOperator", false).apply();
                Intent intent = new Intent(CentralActivity.this, AddAccountActivity.class);
                intent.putExtra("task",0);
                intent.putExtra("source",0);
                startActivityForResult(intent, AppUtilities.NB);

            }
        });

        refreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Rechargement
                new LoadTask(CentralActivity.this).execute();
            }
        });

        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0)
                    floatingActionButton.hide();
                else
                    floatingActionButton.show();

            }
        });

        //Lecteur de notifications
        if (preferences.getBoolean("secureNotif", true)){
            if (preferences.getInt("failCounter", 0) > 0){
                showSecurityNotif();
                editor.putInt("failCounter", 0);
                editor.apply();
            }
        }

        //Chargement des données
        new LoadTask(CentralActivity.this).execute();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void InitializeSQLCipher() {
        SQLiteDatabase.loadLibs(this);
    }

    public List<Account> getData(boolean open){
        //Récupération des datas
        List<Account> accountList;
        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.getReadableDatabase(AppUtilities.getDbKey(this));
        if (open){
            accountList = AppUtilities.decryptAccountList(databaseManager.db_readAll());
        }else {
            accountList = databaseManager.db_readAll();
        }
        databaseManager.close();

        return accountList;
    }

    public void showSecurityNotif(){
        //Création des notifications
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Secure Notifications")
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.security_notif_msg, preferences.getInt("failCounter", 0)))
                .setSmallIcon(R.mipmap.n_pass_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.n_pass_launcher))
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getResources().getString(R.string.app_name)))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getResources().getString(R.string.security_notif_msg, preferences.getInt("failCounter", 0))));

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        manager.notify((int) Calendar.getInstance().getTimeInMillis(), builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.central_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_add:
                Intent i1 = new Intent(CentralActivity.this, AddAccountActivity.class);
                i1.putExtra("task",0);
                i1.putExtra("source",0);
                editor.putBoolean("centralOperator", false).apply();
                startActivityForResult(i1, AppUtilities.NB);
                break;

            case R.id.action_lock:
                Intent i2 = new Intent(CentralActivity.this, PortalActivity.class);
                i2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                editor.putBoolean("centralOperator", false).apply();
                startActivity(i2);
                break;

            case R.id.action_search:
                Intent i3 = new Intent(CentralActivity.this, SearchActivity.class);
                editor.putBoolean("centralOperator", false).apply();
                startActivityForResult(i3, AppUtilities.NB);
                break;

            case R.id.action_import:
                ActivityCompat.requestPermissions(CentralActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_PERMISSION);
                break;

            case R.id.action_save:
                ActivityCompat.requestPermissions(CentralActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                break;

            case R.id.action_settings:
                Intent i4 = new Intent(CentralActivity.this, SettingsActivity.class);
                editor.putBoolean("centralOperator", false).apply();
                startActivityForResult(i4, AppUtilities.NB);
                break;

            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(CentralActivity.this);
                builder.setTitle("\n");

                LayoutInflater factory = LayoutInflater.from(CentralActivity.this);

                @SuppressLint("InflateParams")
                View aboutView = factory.inflate(R.layout.about_view, null);

                Typewriter appName = aboutView.findViewById(R.id.AboutTxtView_1);
                TextView version = aboutView.findViewById(R.id.AboutTxtView_2);
                ImageView aboutMsg = aboutView.findViewById(R.id.aboutMsg);
                ImageView aboutRate = aboutView.findViewById(R.id.aboutRate);
                ImageView aboutShare = aboutView.findViewById(R.id.aboutShare);

                appName.animateText(getResources().getString(R.string.app_name));
                version.setText(R.string.version_num);

                aboutMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        editor.putBoolean("centralOperator", false).apply();
                        toPlayStore();

                    }
                });

                aboutRate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        editor.putBoolean("centralOperator", false).apply();
                        toPlayStore();

                    }
                });

                aboutShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toShare();
                    }
                });

                builder
                        .setView(aboutView)
                        .show();

                break;

            case R.id.action_share:
                toShare();
                break;

            case R.id.action_evaluate:
                editor.putBoolean("centralOperator", false).apply();
                toPlayStore();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toShare(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_txt)+getPackageName()+"\n\n");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)));
    }

    private void toPlayStore(){
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }else{

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("CAM", Arrays.toString(grantResults));
        switch (requestCode) {
            case REQUEST_READ_PERMISSION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importFromFile();
                } else {

                    Toast.makeText(CentralActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case REQUEST_WRITE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToFile();
                } else {

                    Toast.makeText(CentralActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }


            }
        }
    }

    public void importFromFile(){
        scan(new File(Environment.getExternalStorageDirectory()+"/n-pm"));
    }

    public void scan(File f){
        File[] files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".n.pm");
            }
        });

        if (files != null && files.length > 0){

            getFileDialog(files);

        }else{

            new AlertDialog.Builder(this)
                    .setTitle(R.string.any_file_title)
                    .setIcon(R.mipmap.n_pass_launcher)
                    .setMessage(R.string.any_file_msg)
                    .setPositiveButton(R.string.ok, null)
                    .show();

        }
    }

    public void getFileDialog(final File[] files){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(CentralActivity.this);
        builderSingle.setIcon(R.drawable.n_pass_launcher);
        builderSingle.setTitle(R.string.choose_file);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CentralActivity.this, android.R.layout.select_dialog_singlechoice);
        for (File ff: files){
            String[] temp = ff.toString().split("/");
            arrayAdapter.add(temp[temp.length - 1]);
        }

        builderSingle
                .setNegativeButton(R.string.cancel, null)
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String fileName = arrayAdapter.getItem(which);
                        final File filePath = files[which];

                        new AlertDialog.Builder(CentralActivity.this)
                                .setMessage(getResources().getString(R.string.restoration_msg, fileName))
                                .setTitle(R.string.restoration_title)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.to_restore, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,int which) {

                                        StringBuilder text = new StringBuilder();

                                        try {
                                            BufferedReader br = new BufferedReader(new FileReader(filePath));
                                            String line;

                                            while ((line = br.readLine()) != null) {
                                                text.append(line);
                                            }
                                            br.close();

                                            String restOption = preferences.getString("restaurationOption","0");

                                            if (restOption.equals("0")){

                                                parseNRestore(0, text.toString());

                                            }else {

                                                parseNRestore(1, text.toString());

                                            }

                                        }
                                        catch (IOException e) {

                                            Toast.makeText(CentralActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .show();

                    }
                })
                .show();
    }

    public void parseNRestore(int i, String str){
        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.getWritableDatabase(AppUtilities.getDbKey(this));

        if (i == 1){

            databaseManager.clearAccountTable();

        }

        String[] A = str.split("=");
        for (String s: A){
            String[] B = s.split("&");

            databaseManager.db_insertValue(new Account(
                    B[0],
                    B[1],
                    B[2],
                    B[3],
                    B[4],
                    B[5],
                    B[6],
                    B[7]
            ));


        }

        Toast.makeText(CentralActivity.this, R.string.restoration_done, Toast.LENGTH_SHORT).show();

        databaseManager.close();

        new LoadTask(CentralActivity.this).execute();
    }

    public void saveToFile(){

        new saveTask(CentralActivity.this).execute();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editor.putBoolean("centralOperator", false).apply();
    }

    @Override
    public boolean onSupportNavigateUp() {
        editor.putBoolean("centralOperator", false).apply();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferences.getBoolean("centralOperator", false)){

            if (loadingDialog.isShowing()){ loadingDialog.dismiss(); }
            Intent intent = new Intent(CentralActivity.this, PortalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }else{
            editor.putBoolean("centralOperator", true).apply();
        }

        if (preferences.getBoolean("floattingBut",true)){
            floatingActionButton.show();
        }else {
            floatingActionButton.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 0){
            new LoadTask(CentralActivity.this).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class saveTask extends AsyncTask<String, Void, Void>{
        private Context context;
        private int result;
        private String fileName;

        saveTask(Context ctx){
            context = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
            loadingDialog.setMsg(getString(R.string.backup_in_progress));
        }

        @Override
        @SuppressWarnings("unused")
        protected Void doInBackground(String... strings) {

            List<Account> listToSave = getData(false);

            if(!listToSave.isEmpty()) {

                StringBuilder builder = new StringBuilder();

                for (Account a : listToSave) {
                    String strData = a.getAccountTitle() + "&" + a.getUsername() + "&" + a.getMail() + "&" + a.getType() + "&" + a.getMdp() + "&" + a.getNotes() + "&" + a.getDate() + "&" + a.getKey() + "=";
                    builder.append(strData);
                }

                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss", Locale.US);
                fileName = "Backup-" + dateFormat.format(date) + ".n.pm";

                File file = new File(Environment.getExternalStorageDirectory() + "/n-pm");
                if (!file.exists()) {
                    boolean mkdirsTask = file.mkdirs();
                }

                try {
                    File gpxfile = new File(file, fileName);
                    FileWriter writer = new FileWriter(gpxfile);
                    writer.append(builder);
                    writer.flush();
                    writer.close();
                    result = 0;

                } catch (Exception e) {
                    e.printStackTrace();
                    result = -1;
                }

            }else {

                result = -2;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (loadingDialog.isShowing()){ loadingDialog.dismiss(); }

            if (result == 0){
                Toast.makeText(context, getString(R.string.saved_in_npm) + fileName, Toast.LENGTH_LONG).show();
            }else if(result == -1){
                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, R.string.anything_to_save, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class LoadTask extends AsyncTask<String, Void, Void>{
        private Context context;
        private List<Account> accountL;

        LoadTask(Context ctx){
            context = ctx;

            accountL = new ArrayList<>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!refreshLayout.isRefreshing()) {
                loadingDialog.show();
                loadingDialog.setCancelable();
                loadingDialog.setMsg(getResources().getString(R.string.loading));
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
            
            accountL = getData(true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!accountL.isEmpty()) {

                CentralAdapter adapter = new CentralAdapter(accountL, R.layout.central_layout, CentralActivity.this, new CentralAdapter.CentralAdapterListener() {
                    @Override
                    public void refresh() {
                        new LoadTask(context).execute();
                    }

                    @Override
                    public void edit(Account account) {

                        editor.putBoolean("centralOperator", false).apply();
                        Intent intent = new Intent(CentralActivity.this, AddAccountActivity.class);
                        intent.putExtra("task", 1);
                        intent.putExtra("source", 0);
                        intent.putExtra("title", account.getAccountTitle());
                        intent.putExtra("username", account.getUsername());
                        intent.putExtra("mail", account.getMail());
                        intent.putExtra("mdp", account.getMdp());
                        intent.putExtra("notes", account.getNotes());
                        intent.putExtra("type", account.getType());
                        intent.putExtra("key", account.getKey());
                        intent.putExtra("date", account.getDate());
                        startActivityForResult(intent, AppUtilities.NB);

                    }
                });
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(Swiping.getInstance(CentralActivity.this, accountL, adapter, getWindow(), true, AppUtilities.COLORS[new Random().nextInt(AppUtilities.COLORS.length)],
                                                                                                                                                      AppUtilities.COLORS[new Random().nextInt(AppUtilities.COLORS.length)]).enableSwiping());
                itemTouchHelper.attachToRecyclerView(recyclerView);

                noAccountMsg.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

            }else {

                noAccountMsg.setVisibility(View.VISIBLE);
                noAccountMsg.startAnimation(slideUp);
                recyclerView.setVisibility(View.GONE);

            }

            if (refreshLayout.isRefreshing()) { refreshLayout.setRefreshing(false); }

            if (loadingDialog.isShowing()) { loadingDialog.dismiss(); }

        }
    }
}
