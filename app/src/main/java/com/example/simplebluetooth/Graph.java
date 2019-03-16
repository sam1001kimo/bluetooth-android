package com.example.simplebluetooth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;


public class Graph extends AppCompatActivity {

    ViewGroup radar;
    Factory factory=new Factory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RadarView.ClearPoint();
        for(int i =0;i<Factory.x.length;i++){
            RadarView.AddPoint(new float[] {Factory.x[i], Factory.y[i], factory.max(),0.3f});
        }


        setContentView(R.layout.graph);
        radar = findViewById(R.id.radar);
    }
}
