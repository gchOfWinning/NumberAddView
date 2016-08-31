package com.hxm.numberadd;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hxm.library.NumberAddView;

public class MainActivity extends Activity{

    private NumberAddView number;

    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);

        number = (NumberAddView) findViewById(R.id.number);

        number.setNumber(1);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                number.addNumber();
            }
        });

        findViewById(R.id.sub).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                number.subNumber();
            }
        });

        number.setOnValueChangedListener(new NumberAddView.ValueChangedListener() {

            @Override
            public void valueChanged(int value) {

                tv.setText(TextUtils.concat("current num:", String.valueOf(value)));
            }
        });
    }
}