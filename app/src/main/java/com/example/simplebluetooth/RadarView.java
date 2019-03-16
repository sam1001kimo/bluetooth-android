package com.example.simplebluetooth;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {

    static ArrayList<float[]> data = new ArrayList<>();
    public static void AddPoint(float[] p) {
        data.add(p);
    }
    public static void ClearPoint(){
        data.clear();
    }

    static int bgColor = Color.BLACK;
    public static void SetBgColor(int c) {
        bgColor = c;
    }

    static int radarColor = Color.GREEN;
    public static void SetRadarColor(int c) {
        radarColor = c;
    }

    static int radarPointColor = Color.WHITE;
    public static void SetRadarPointColor(int c) {
        radarPointColor = c;
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        System.out.println("Canvas :" + canvas.getWidth() + "/" + canvas.getHeight() );
        canvas.drawColor(bgColor);
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(radarColor);
        p.setStrokeWidth(1);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(width/2, height, height*0.9f, p);
        canvas.drawCircle(width/2, height, height*0.45f, p);

        Paint p2 = new Paint();
        p2.setColor(radarPointColor);

        for(float[] point : data) {

            if(point == null || point.length < 3) {
                continue;
            }

            float ra = height/point[2];

            float r = 10;
            if(point.length > 3) {
                r = point[3]* ra;
            }

            float x = (width/2) + (point[0]*ra);
            float y = height - (point[1]*ra);

            canvas.drawCircle(x, y, r, p2);

        }

    }

}
