package org.goldenroute.cq;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MessageBox {

    public static void show(Context context, String message, Buttons buttons, DialogInterface.OnClickListener clickListener) {
        if (buttons == Buttons.YesNo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(message);
            builder.setPositiveButton("Yes", clickListener);
            builder.setNegativeButton("No", clickListener);
            builder.show();
        }
    }

    public static void prompt(Context context, String prompt, final OnClickListener clickListener) {
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View view = layoutInflater.inflate(R.layout.dialog_prompt, null);
        final TextView textView = (TextView) view.findViewById(R.id.text_view_prompt);
        final EditText editText = (EditText) view.findViewById(R.id.edit_text_input);

        textView.setText(prompt);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clickListener.onClick(dialog, id, editText.getText().toString());
                    }
                });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        clickListener.onClick(dialog, id, null);
                    }
                });
        builder.show();
    }

    public enum Buttons {
        None,
        YesNo
    }

    public interface OnClickListener {
        void onClick(DialogInterface dialog, int which, String userInput);
    }
}
