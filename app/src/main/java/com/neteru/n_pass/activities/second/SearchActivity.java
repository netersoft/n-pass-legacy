package com.neteru.n_pass.activities.second;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.neteru.n_pass.R;
import com.neteru.n_pass.activities.prime.CentralActivity;
import com.neteru.n_pass.activities.prime.PortalActivity;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.Swiping;
import com.neteru.n_pass.classes.adapters.CentralAdapter;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Account;

import net.sqlcipher.database.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private List<Account> accountList = new ArrayList<>();
    private String globalQuery;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        InitializeSQLCipher();
        setResult(1);

        loadingDialog = new LoadingDialog(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        recyclerView = findViewById(R.id.recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        new LoadTask().execute();

        editor.putBoolean("operator", false).apply();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.action_search);

        SearchView search = (SearchView) mSearch.getActionView();
        search.setQueryHint(getResources().getString(R.string.search_title));
        search.setIconified(false);
        search.setIconifiedByDefault(true);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                loadData(query.toLowerCase());

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                loadData(newText.toLowerCase());

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void InitializeSQLCipher() { SQLiteDatabase.loadLibs(this); }

    @Override
    public void onBackPressed() {
        editor.putBoolean("searchOperator", false).apply();

        if (preferences.getBoolean("operator", false)){

            Intent i = new Intent(SearchActivity.this, CentralActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }
        super.onBackPressed();
    }

    private void getData(){
        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.getReadableDatabase(AppUtilities.getDbKey(this));
        accountList = AppUtilities.decryptAccountList(databaseManager.db_readAll());
        databaseManager.close();
    }

    private void loadData(final String q){
        List<Account> qAccount = new ArrayList<>(); globalQuery = q;

        if (!accountList.isEmpty()) {

            recyclerView.setVisibility(View.VISIBLE);
            for (Account account : accountList){

                    if (!q.isEmpty() && (account.getAccountTitle().toLowerCase().contains(q) ||
                            account.getUsername().toLowerCase().contains(q) ||
                            account.getNotes().toLowerCase().contains(q) ||
                            account.getType().toLowerCase().contains(q))) {

                        qAccount.add(account);

                    }

            }

            CentralAdapter adapter = new CentralAdapter(qAccount, R.layout.central_layout, SearchActivity.this, new CentralAdapter.CentralAdapterListener() {
                @Override
                public void refresh() {
                    getData();
                    loadData(q);
                    setResult(0);

                    editor.putBoolean("operator", true).apply();
                }

                @Override
                public void edit(Account account) {

                    setResult(0);
                    editor
                            .putBoolean("operator", true)
                            .putBoolean("searchOperator", false)
                            .apply();

                    Intent intent = new Intent(SearchActivity.this, AddAccountActivity.class);
                    intent.putExtra("task", 1);
                    intent.putExtra("title", account.getAccountTitle());
                    intent.putExtra("username", account.getUsername());
                    intent.putExtra("mail", account.getMail());
                    intent.putExtra("mdp", account.getMdp());
                    intent.putExtra("notes", account.getNotes());
                    intent.putExtra("type", account.getType());
                    intent.putExtra("key", account.getKey());
                    intent.putExtra("date", account.getDate());
                    startActivityForResult(intent, 4320);

                }
            });
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(Swiping.getInstance(SearchActivity.this, accountList, adapter, getWindow(), false, AppUtilities.COLORS[new Random().nextInt(AppUtilities.COLORS.length)],
                                                                                                                                                     AppUtilities.COLORS[new Random().nextInt(AppUtilities.COLORS.length)]).enableSwiping());
            itemTouchHelper.attachToRecyclerView(recyclerView);

        }else{
            recyclerView.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferences.getBoolean("searchOperator", false)){

            loadingDialog.dismiss();
            Intent intent = new Intent(SearchActivity.this, PortalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }else{
            editor.putBoolean("searchOperator", true).apply();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        editor.putBoolean("searchOperator", false).apply();

        if (preferences.getBoolean("operator", false)){

            Intent i = new Intent(SearchActivity.this, CentralActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

        }else {

            finish();

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 0){
            getData();
            loadData(globalQuery.isEmpty() ? "" : globalQuery);
        }
    }

    @SuppressLint("StaticFieldLeak")
    class LoadTask extends AsyncTask<String, Void, Void> {

        LoadTask(){}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
        }

        @Override
        protected Void doInBackground(String... strings) {
            getData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            loadingDialog.dismiss();

        }
    }
}
