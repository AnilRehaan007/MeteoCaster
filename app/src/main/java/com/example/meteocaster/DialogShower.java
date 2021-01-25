package com.example.meteocaster;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;

import java.util.Timer;
import java.util.TimerTask;

public class DialogShower {
    private final int layout;
    private int animation=0;
    private final Context context;
    private int timing=2000;

    public DialogShower(int layout, Context context) {
        this.layout = layout;
        this.context = context;
    }

    public DialogShower(int layout, int animation, Context context) {
        this.layout = layout;
        this.animation = animation;
        this.context = context;
    }

    public DialogShower(int layout, int animation, Context context, int timing) {
        this.layout = layout;
        this.animation = animation;
        this.context = context;
        this.timing = timing;
    }

    public final void showDialog()
    {
        Dialog dialog;
        if(animation==0)
        {
            dialog=new Dialog(context);
        }
        else
        {
            dialog=new Dialog(context,animation);
        }
        dialog.setContentView(layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        Window window=dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        dialog.show();

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
                timer.cancel();
            }
        },timing);

    }
}
