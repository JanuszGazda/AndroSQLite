package com.example.janek.bazasqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PomocnikBD extends SQLiteOpenHelper
{
    public final static int WERSJA_BAZY = 1;
    public final static String ID = "_id";
    public final static String NAZWA_BAZY = "nazwa_bazy";
    public final static String NAZWA_TABELI = "nazwa_tabeli ";
    public final static String KOLUMNA1 = "nazwa_kolumny_1";
    public final static String KOLUMNA2 = "nazwa_kolumny_2";
    public final static String KOLUMNA3 = "nazwa_kolumny_3";
    public final static String KOLUMNA4 = "nazwa_kolumny_4";
    public final static String TW_BAZY = "CREATE TABLE IF NOT EXISTS " + NAZWA_TABELI +
            "("+ID+" integer primary key autoincrement, " +
            KOLUMNA1+" text not null,"+
            KOLUMNA2+" text," +
            KOLUMNA3+" text," +
            KOLUMNA4+" text);";
    private static final String KAS_BAZY = "DROP TABLE IF EXISTS "+NAZWA_TABELI;

    public PomocnikBD(Context context)
    {
        super(context,NAZWA_BAZY,null,WERSJA_BAZY);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
       db.execSQL(TW_BAZY);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //aktualizacja bazy do nowej wersji, etc.
        db.execSQL(KAS_BAZY);
        onCreate(db);
    }
}