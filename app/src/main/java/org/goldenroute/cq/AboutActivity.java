package org.goldenroute.cq;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView textView = (TextView) findViewById(R.id.text_view_about_info);
        assert textView != null;
        textView.setMovementMethod(new LinkMovementMethod());
        textView.setText(Html.fromHtml(readRawTextFile(R.raw.about), new ResourceImageGetter(), null));
        Linkify.addLinks(textView, Linkify.ALL);
    }

    public String readRawTextFile(int id) {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(id)));
        StringBuilder text = new StringBuilder();
        String line;

        try {
            while ((line = bufferedReader.readLine()) != null) text.append(line);
        } catch (IOException e) {
            return "";
        }

        return text.toString();
    }

    private class ResourceImageGetter implements Html.ImageGetter {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            switch (source) {
                case "boat.png": {
                    drawable = ContextCompat.getDrawable(AboutActivity.this, R.drawable.boat);
                    break;
                }
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            return drawable;
        }
    }
}
