package com.neteru.n_pass.classes.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Ark Noam on 15/09/2018.
 */

@SuppressWarnings("unused")
@DatabaseTable(tableName = "profil")
public class Profil {

    @DatabaseField(columnName = "id", canBeNull = false)
    private int id;
    @DatabaseField(columnName = "mdp", canBeNull = false)
    private String mdp;
    @DatabaseField(columnName = "mail", canBeNull = false)
    private String mail;
    @DatabaseField(columnName = "key", canBeNull = false)
    private String key;

    public Profil(){}

    public Profil(String mdp_, String mail_, String key_){
        id = 4320;
        mdp = mdp_;
        mail = mail_;
        key = key_;
    }

    public int getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getMdp() {
        return mdp;
    }

    public String getKey() { return key; }
}
