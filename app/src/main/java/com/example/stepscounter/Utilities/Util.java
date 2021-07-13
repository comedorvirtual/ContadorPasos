package com.example.stepscounter.Utilities;

public class Util {
    public static int pasos;
    public static int calorias;

    public Util ()
    {
        pasos = 0;
        calorias = 0;
    }

    public static int getPasos() {
        return pasos;
    }

    public static void setPasos(int pasos) {
        Util.pasos = pasos;
    }

    public static void sumPaso()
    {
        Util.pasos++;
    }


    public static int getCalorias() {
        return calorias;
    }

    public static void setCalorias(int calorias) {
        Util.calorias = calorias;
    }
}
