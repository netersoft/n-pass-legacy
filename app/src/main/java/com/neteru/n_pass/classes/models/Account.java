package com.neteru.n_pass.classes.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Ark Noam on 12/09/2018.
 */

@SuppressWarnings("unused")
@DatabaseTable(tableName = "accounts")
public class Account {

    @DatabaseField(generatedId = true)
    private long id;
    @DatabaseField(columnName = "accountTitle", canBeNull = false)
    private String accountTitle;
    @DatabaseField(columnName = "username")
    private String username;
    @DatabaseField(columnName = "mail")
    private String mail;
    @DatabaseField(columnName = "type", canBeNull = false)
    private String type;
    @DatabaseField(columnName = "mdp", canBeNull = false)
    private String mdp;
    @DatabaseField(columnName = "notes")
    private String notes;
    @DatabaseField(columnName = "key", canBeNull = false)
    private String key;
    @DatabaseField(columnName = "date", canBeNull = false)
    private String date;

    public Account(){}

    public Account(String account, String username, String mail, String type, String mdp, String notes, String date,String key){
        this.accountTitle = account;
        this.username = username;
        this.mail = mail;
        this.type = type;
        this.mdp = mdp;
        this.notes = notes;
        this.key = key;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public String getUsername() {
        return username;
    }

    public String getMail() {
        return mail;
    }

    public String getType() {
        return type;
    }

    public String getNotes() {
        return notes;
    }

    public String getMdp() {
        return mdp;
    }

    public String getKey() {
        return key;
    }

    public String getDate() {
        return date;
    }
}
