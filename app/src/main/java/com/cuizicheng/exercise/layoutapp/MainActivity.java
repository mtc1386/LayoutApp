package com.cuizicheng.exercise.layoutapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    static int[] ids = new int[]{
            R.drawable.cat1,
            R.drawable.cat2,
            R.drawable.cat3,
            R.drawable.cat4,
            R.drawable.cat5,
            R.drawable.cat6,
            R.drawable.cat7,
            R.drawable.cat8,
            R.drawable.cat9
    };

    static int[] girl_ids = new int[]{
            R.drawable.girl1,
            R.drawable.girl2,
            R.drawable.girl3,
            R.drawable.girl4,
            R.drawable.girl5,
            R.drawable.girl6,
            R.drawable.girl7,
            R.drawable.girl8,
            R.drawable.girl9,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Layout layout = LayoutParser.parserDemo();
            Log.d("layout", layout.toString() + ",," + Math.hypot(0f, 0f));
            ((LayoutView) findViewById(R.id.lView)).setLayout(layout);
            layout.setImgResourceIds(girl_ids);
        } catch (JSONException e) {
            Log.d("layout", "error duraing parser");
            e.printStackTrace();
        }

    }
}
