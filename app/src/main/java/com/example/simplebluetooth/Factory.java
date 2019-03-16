package com.example.simplebluetooth;

public  class  Factory {

    public static float[] x = new float[37];
    public static float[] y = new float[37];

    public void setX(int a, float x) {
        this.x[a] = x;
    }

    public float getX(int x) {
        return this.x[x];
    }

    public void setY(int b, float y) {
        this.y[b] = y;
    }

    public float getY(int y) {
        return this.y[y];
    }

    public float[] getallx() {
        return x;
    }

    public float[] getally() {
        return y;
    }

    public float max() {
        float flt = 0;
        for (int i = 0; i < x.length; i++) {
            if (Math.abs(x[i]) > flt) {
                flt = Math.abs(x[i]);
            }
            if (Math.abs(y[i]) > flt) {
                flt = Math.abs(y[i]);
            }
        }
        return flt;
    }
}
