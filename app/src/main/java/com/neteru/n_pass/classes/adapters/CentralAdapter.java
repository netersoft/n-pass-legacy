package com.neteru.n_pass.classes.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.AppUtilities;
import com.neteru.n_pass.classes.LoadingDialog;
import com.neteru.n_pass.classes.databases.DatabaseManager;
import com.neteru.n_pass.classes.models.Account;

import java.util.List;

import static com.neteru.n_pass.classes.AppUtilities.COLORS;
import static com.neteru.n_pass.classes.AppUtilities.getDigitFromString;
import static com.neteru.n_pass.classes.AppUtilities.getFirstLetters;

/**
 * Created by Ark Noam on 13/09/2018.
 */
public class CentralAdapter extends RecyclerView.Adapter<CentralAdapter.MyViewHolder> {
    private Context context;
    private int rowLayout;
    private List<Account> accountList;
    private Animation slideUp, slideDown;
    private CentralAdapterListener listener;
    private SharedPreferences preferences;

    class MyViewHolder extends RecyclerView.ViewHolder{
        private RelativeLayout centralTop;
        private LinearLayout centralBottom;
        private LinearLayout usernameLayout;
        private LinearLayout mailLayout;
        private LinearLayout mdpLayout;
        private LinearLayout notesLayout;
        private ImageView centralImg;
        private FrameLayout centralEditFrame;
        private FrameLayout centralDeleteFrame;
        private FrameLayout centralCopyUsername;
        private FrameLayout centralCopyMail;
        private FrameLayout centralCopyMdp;
        private FrameLayout centralCopyNotes;
        private TextView centralTitleTxt;
        private TextView centralEditTxt;
        private TextView centralDeleteTxt;
        private TextView centralCopyUsernameTxt;
        private TextView centralCopyMailTxt;
        private TextView centralCopyNotesTxt;
        private TextView centralCopyMdpTxt;
        private TextView centralUsernameTxt;
        private TextView centralMailTxt;
        private TextView centralNotesTxt;
        private TextView centralMdpTxt;
        private TextView centralDate;

        MyViewHolder(View view){
            super(view);

            centralTop = view.findViewById(R.id.central_top);
            centralBottom = view.findViewById(R.id.central_bottom);
            centralImg = view.findViewById(R.id.central_img);
            centralTitleTxt = view.findViewById(R.id.central_title_txt);
            centralEditFrame = view.findViewById(R.id.central_edit_frame);
            centralDeleteFrame = view.findViewById(R.id.central_delete_frame);
            centralCopyUsername = view.findViewById(R.id.central_copy_username);
            centralCopyMdp = view.findViewById(R.id.central_copy_mdp);
            centralCopyMail = view.findViewById(R.id.central_copy_mail);
            centralCopyNotes = view.findViewById(R.id.central_copy_notes);
            centralEditTxt = view.findViewById(R.id.central_edit_txt);
            centralDeleteTxt = view.findViewById(R.id.central_delete_txt);
            centralCopyUsernameTxt = view.findViewById(R.id.central_copy_username_txt);
            centralCopyMdpTxt = view.findViewById(R.id.central_copy_mdp_txt);
            centralCopyMailTxt = view.findViewById(R.id.central_copy_mail_txt);
            centralCopyNotesTxt = view.findViewById(R.id.central_copy_notes_txt);
            centralUsernameTxt = view.findViewById(R.id.central_username);
            centralMailTxt = view.findViewById(R.id.central_mail);
            centralMdpTxt = view.findViewById(R.id.central_mdp);
            centralNotesTxt = view.findViewById(R.id.central_notes);
            usernameLayout = view.findViewById(R.id.central_username_layout);
            mailLayout = view.findViewById(R.id.central_mail_layout);
            mdpLayout = view.findViewById(R.id.central_mdp_layout);
            notesLayout = view.findViewById(R.id.central_notes_layout);
            centralDate = view.findViewById(R.id.central_date);
        }

    }

    public CentralAdapter(List<Account> accounts, int layout, Context ctx, CentralAdapterListener l){
        accountList = accounts;
        rowLayout = layout;
        context = ctx;
        listener = l;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Account account = accountList.get(position);

        switch (account.getType()){
            case "facebook":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_facebook));
                break;

            case "twitter":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_twitter));
                break;

            case "google":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_google));
                break;

            case "instagram":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_instagram));
                break;

            case "whatsapp":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_whatsapp));
                break;

            case "telegram":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_telegram));
                break;

            case "email":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email));
                break;

            case "youtube":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_youtube));
                break;

            case "linkedin":
                holder.centralImg.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_linkedin));
                break;

            default:
                TextDrawable drawable = TextDrawable.builder()
                        .buildRound(getFirstLetters(account.getAccountTitle(), false), COLORS[getDigitFromString(account.getAccountTitle())]);
                holder.centralImg.setImageDrawable(drawable);

        }

        String accountTitleTxt = account.getAccountTitle().substring(0, 1).toUpperCase() +
                                (account.getAccountTitle().length() > 15 ?
                                 account.getAccountTitle().substring(1,18).toLowerCase()+"..." :
                                 account.getAccountTitle().substring(1).toLowerCase());
        holder.centralTitleTxt.setText(accountTitleTxt);

        Drawable drawable_1 = ContextCompat.getDrawable(context, R.mipmap.ic_create_black_24dp);
        if (drawable_1 != null) {
            drawable_1 = DrawableCompat.wrap(drawable_1);
            DrawableCompat.setTint(drawable_1, ContextCompat.getColor(context, R.color.colorAccent));
            DrawableCompat.setTintMode(drawable_1, PorterDuff.Mode.SRC_IN);
            holder.centralEditTxt.setCompoundDrawablesWithIntrinsicBounds(drawable_1, null, null, null);
        }

        Drawable drawable_2 = ContextCompat.getDrawable(context, R.mipmap.ic_delete_black_24dp);
        if (drawable_2 != null) {
            drawable_2 = DrawableCompat.wrap(drawable_2);
            DrawableCompat.setTint(drawable_2, ContextCompat.getColor(context, R.color.colorAccent));
            DrawableCompat.setTintMode(drawable_2, PorterDuff.Mode.SRC_IN);
            holder.centralDeleteTxt.setCompoundDrawablesWithIntrinsicBounds(drawable_2, null, null, null);
        }

        setCopyDrawableColor(holder.centralCopyUsernameTxt);
        setCopyDrawableColor(holder.centralCopyMailTxt);
        setCopyDrawableColor(holder.centralCopyNotesTxt);
        setCopyDrawableColor(holder.centralCopyMdpTxt);

        holder.centralUsernameTxt.setText(account.getUsername());
        holder.centralMailTxt.setText(account.getMail());

        if (preferences.getBoolean("hidePassword", false)){

            StringBuilder mask = new StringBuilder();
            for (int i = 0; i < account.getMdp().length(); i++){ mask.append("*"); }
            holder.centralMdpTxt.setText(mask.toString());

        }else {
            holder.centralMdpTxt.setText(account.getMdp());
        }

        holder.centralNotesTxt.setText(account.getNotes());
        holder.centralDate.setText(context.getResources().getString(R.string.added_on, account.getDate()));

        if (account.getUsername().isEmpty()){
            holder.usernameLayout.setVisibility(View.GONE);
        }

        if (account.getMail().isEmpty()){
            holder.mailLayout.setVisibility(View.GONE);
        }

        if (account.getMdp().isEmpty()){
            holder.mdpLayout.setVisibility(View.GONE);
        }

        if (account.getNotes().isEmpty()){
            holder.notesLayout.setVisibility(View.GONE);
        }

        holder.centralTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.centralBottom.getVisibility() == View.VISIBLE){

                    holder.centralBottom.setVisibility(View.GONE);
                    holder.centralBottom.startAnimation(slideDown);

                }else {

                    holder.centralBottom.setVisibility(View.VISIBLE);
                    holder.centralBottom.startAnimation(slideUp);

                }
            }
        });

        holder.centralEditFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.edit(account);
            }
        });

        if (preferences.getBoolean("deleteBut", true)){
            holder.centralDeleteFrame.setVisibility(View.VISIBLE);
        }else {
            holder.centralDeleteFrame.setVisibility(View.GONE);
        }

        holder.centralDeleteFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(context)
                        .setTitle(R.string.deletion_title)
                        .setIcon(R.mipmap.n_pass_launcher)
                        .setMessage(context.getResources().getString(R.string.are_u_sure_to_delete, account.getAccountTitle()))
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                new deleteTask(context, account).execute();

                            }
                        })
                        .show();

            }
        });

        holder.centralCopyMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy(account.getMail(), account.getAccountTitle());
            }
        });

        holder.centralCopyMdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy(account.getMdp(), account.getAccountTitle());
            }
        });

        holder.centralCopyNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy(account.getNotes(), account.getAccountTitle());
            }
        });

        holder.centralCopyUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copy(account.getUsername(), account.getAccountTitle());
            }
        });
    }

    private void copy(String info, String title){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("N-Password Manager Info - "+ title, info);

        if (clipboardManager == null){

            Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
            return;
        }

        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show();
    }

    private void setCopyDrawableColor(TextView t){

        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_content_copy_black_24dp);
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.colorAccent));
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            t.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public void editItem(Account a){
        listener.edit(a);
    }

    public void removeItem(int position) {
        accountList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, accountList.size());
    }

    public void deleteItem(Account a){
        new deleteTask(context, a).execute();
    }

    public void restoreItem(Account account, int position) {
        accountList.add(position, account);
        // notify item added by position
        notifyItemInserted(position);
    }

    public interface CentralAdapterListener{
        void refresh();
        void edit(Account account);
    }

    @SuppressLint("StaticFieldLeak")
    class deleteTask extends AsyncTask<String, Void, Void>{
        private Context context;
        private LoadingDialog loadingDialog;
        private Account account;

        deleteTask(Context ctx, Account a){
            context = ctx;
            account = a;

            loadingDialog = new LoadingDialog(ctx);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loadingDialog.show();
            loadingDialog.setCancelable();
            loadingDialog.setMsg(context.getString(R.string.deletion_in_progress));
        }

        @Override
        protected Void doInBackground(String... strings) {

            DatabaseManager databaseManager = new DatabaseManager(context);
            databaseManager.getWritableDatabase(AppUtilities.getDbKey(context));
            databaseManager.db_removeValue(AppUtilities.cryptAccount(new Account(
                    account.getAccountTitle(),
                    account.getUsername(),
                    account.getMail(),
                    account.getType(),
                    account.getMdp(),
                    account.getNotes(),
                    account.getDate(),
                    account.getKey())));
            databaseManager.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (loadingDialog.isShowing()){ loadingDialog.dismiss(); }

            listener.refresh();
        }
    }
}
