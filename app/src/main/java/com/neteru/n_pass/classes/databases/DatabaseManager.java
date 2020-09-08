package com.neteru.n_pass.classes.databases;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;

import android.preference.PreferenceManager;
import android.util.Log;

import com.j256.ormlite.cipher.android.apptools.OrmLiteSqliteOpenHelper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.neteru.n_pass.classes.models.Account;
import com.neteru.n_pass.classes.models.Profil;

import java.util.List;

/**
 * Created by Ark Noam on 12/09/2018.
 */

@SuppressWarnings("unused")
public class DatabaseManager extends OrmLiteSqliteOpenHelper {

    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "N_PasswordApp.db";
    private final static String TAG = "DB_MANAGER";
    private Context context;

    public DatabaseManager(Context ctx){
        super(ctx, DB_NAME, null, DB_VERSION);

        context = ctx;
    }

    @Override
    protected String getPassword() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("key", null);
    }

    @Override
    public ConnectionSource getConnectionSource() {
        return super.getConnectionSource();
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {

            TableUtils.createTable(connectionSource, Profil.class);
            TableUtils.createTable(connectionSource, Account.class);

        }catch (Exception e){
            Log.e(TAG, "Erreur lors de la création de la Table - "+e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {

            TableUtils.dropTable(connectionSource, Profil.class, true);
            TableUtils.dropTable(connectionSource, Account.class, true);

            TableUtils.createTable(connectionSource, Profil.class);
            TableUtils.createTable(connectionSource, Account.class);

        }catch (Exception e){
            Log.e(TAG, "Erreur lors de la mise à jour de la Table - "+e);
        }
    }

    public void clearAccountTable(){
        try {

            TableUtils.clearTable(getConnectionSource(), Account.class);

        }catch (Exception e){
            Log.e(TAG, "Erreur lors de la purge de la Table - "+e);
        }
    }

    public void db_setProfil(Profil profil){
        try {
            getDao(Profil.class).create(profil);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la création du profil - "+e);
        }
    }

    public List<Profil> db_getProfil(){
        try {

            return getDao(Profil.class).queryForAll();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la récupération des infos du profil - "+e);
            return null;

        }
    }

    public long db_updateProfilMdp(String mdp){
        try {

            Dao<Profil, Integer> dao = getDao(Profil.class);
            UpdateBuilder<Profil, Integer> ub = dao.updateBuilder();

            ub.updateColumnValue("mdp", mdp);
            ub.where().eq("id", 4320);

            ub.update();

            return 0;

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la mise à jour du profil - "+e);
            return -1;
        }
    }

    public void db_updateProfilMail(String mail){
        try {

            Dao<Profil, Integer> dao = getDao(Profil.class);
            UpdateBuilder<Profil, Integer> ub = dao.updateBuilder();

            ub.updateColumnValue("mail", mail);
            ub.where().eq("id", 4320);

            ub.update();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la mise à jour du profil - "+e);
        }
    }

    public void db_insertValue(Account account){
        try {

            getDao(Account.class).create(account);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de l'insertion de l'objet dans la Table - "+e);
        }
    }

    public List<Account> db_readAll(){
        try {

            return getDao(Account.class).queryForAll();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture de toute la Table - "+e);
            return null;
        }
    }

    public List<Account> db_readByUsername(String username){
        try {

            return getDao(Account.class).queryForEq("username", username);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture par nom d'utilisateur de la Table - "+e);
            return null;
        }
    }

    public List<Account> db_readByMail(String mail){
        try {

            return getDao(Account.class).queryForEq("mail", mail);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture par mail de la Table - "+e);
            return null;
        }
    }

    public List<Account> db_readByNature(String type){
        try {

            return getDao(Account.class).queryForEq("type", type);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture par type de compte de la Table - "+e);
            return null;
        }
    }

    public long db_updateValueTitle(Account account){
        try {

            Dao<Account, Integer> dao = getDao(Account.class);
            UpdateBuilder<Account, Integer> ub = dao.updateBuilder();

            ub.updateColumnValue("accountTitle", account.getAccountTitle());
            ub.where().eq("type", account.getType())
                      .and()
                      .eq("username", account.getUsername())
                      .and()
                      .eq("mail", account.getMail());

            ub.update();

            return account.getId();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la mise à jour dans la Table - "+e);
            return -1;
        }
    }

    public void db_removeValue(Account account){
        try {

            Dao<Account, Integer> dao = getDao(Account.class);
            DeleteBuilder<Account, Integer> ub = dao.deleteBuilder();

            ub.where().eq("accountTitle", account.getAccountTitle())
                      .and()
                      .eq("type", account.getType())
                      .and()
                      .eq("username", account.getUsername())
                      .and()
                      .eq("mail", account.getMail())
                      .and()
                      .eq("date", account.getDate())
                      .and()
                      .eq("notes", account.getNotes())
                      .and()
                      .eq("mdp", account.getMdp());
            ub.delete();

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la suppression dans la Table - "+e);
        }
    }

    public List<Account> db_searchByAccountTitleAndTypeAndUsername(String q){
        try {

            Dao<Account, Integer> dao = getDao(Account.class);
            QueryBuilder<Account, Integer> qb = dao.queryBuilder();

            qb.where().like("accountTitle", "%"+q+"%").or().like("type", "%"+q+"%").or().like("username", "%"+q+"%");
            PreparedQuery<Account> pq = qb.prepare();

            return dao.query(pq);

        }catch (Exception e){

            Log.e(TAG, "Erreur lors de la lecture par description de la Table - "+e);
            return null;
        }
    }
}
