package com.example.doctorlogin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    EditText Docname;
    EditText DocReg;
    Button enter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        Docname = (EditText) findViewById(R.id.editTextText);
        DocReg =   (EditText) findViewById(R.id.editTextText2);
        enter = (Button) findViewById(R.id.button);

    }

    public void updateText(View view){

        System.out.println("Button clicked");

    }
}