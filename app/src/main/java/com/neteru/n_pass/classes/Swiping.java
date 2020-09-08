package com.neteru.n_pass.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;
import android.view.Window;

import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.adapters.CentralAdapter;
import com.neteru.n_pass.classes.models.Account;

import java.util.List;

/**
 * Created by Ark Noam on 22/09/2018.
 */

@SuppressWarnings("unused")
public class Swiping {
    private Context context;
    private List<Account> accountList;
    private CentralAdapter adapter;
    private Paint p;
    private Window window;
    private boolean verdict;
    private int color_1, color_2;

    private Swiping(Context ctx, List<Account> list, CentralAdapter a, Window w, boolean v, int c1, int c2){
        context = ctx;
        accountList = list;
        adapter = a;
        window = w;
        verdict = v;
        color_1 = c1;
        color_2 = c2;

        p = new Paint();
    }

    public static Swiping getInstance(Context ctx, List<Account> list, CentralAdapter a, Window w, boolean v, int c1, int c2){
        return new Swiping(ctx, list, a, w, v, c1, c2);
    }

    public ItemTouchHelper.SimpleCallback enableSwiping(){
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT){

                    final Account editedAccount = accountList.get(position);

                    adapter.editItem(editedAccount);

                } else {

                    final Account deletedAccount = accountList.get(position);
                    final int deletedPosition = position;
                    adapter.removeItem(position);
                    // showing snack bar with Undo option
                    Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.deleted, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // undo is selected, restore the deleted item
                            adapter.restoreItem(deletedAccount, deletedPosition);
                        }
                    })
                    .addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if(event != DISMISS_EVENT_ACTION){
                                adapter.deleteItem(deletedAccount);
                            }
                        }
                    });
                    snackbar.setActionTextColor(Color.YELLOW);
                    snackbar.show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();

                    if(dX > 0){

                        p.setColor(color_1);
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);

                        Drawable d = ContextCompat.getDrawable(context, R.mipmap.ic_delete_white_24dp);
                        icon = AppUtilities.drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float leftPosition = (float) iconWidth,
                              rightPosition = iconWidth * 2,
                              topPosition = itemView.getTop() + ((height - iconHeight) / 2),
                              bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);

                    } else {

                        p.setColor(color_2);
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);

                        Drawable d = ContextCompat.getDrawable(context, R.mipmap.ic_create_white_24dp);
                        icon = AppUtilities.drawableToBitmap(d);

                        int iconWidth = icon.getWidth();
                        int iconHeight = icon.getHeight();

                        float rightPosition = itemView.getRight() - iconWidth,
                              leftPosition = rightPosition - iconWidth,
                              topPosition = itemView.getTop() + ((height - iconHeight) / 2),
                              bottomPosition = topPosition + iconHeight;

                        RectF iconDest = new RectF(leftPosition, topPosition, rightPosition, bottomPosition);
                        c.drawBitmap(icon, null, iconDest, p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive);
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("swipingAct", true) && verdict;
            }
        };
    }

    private Window getWindow(){
        return window;
    }
}
