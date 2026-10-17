package com.sipaman.maintenance;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class LoadingDialog {

    private final Dialog dialog;

    public LoadingDialog(Context context) {

        dialog = new Dialog(context);

        dialog.setContentView(R.layout.dialog_loading);

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        if (dialog.isShowing())
            dialog.dismiss();
    }
}