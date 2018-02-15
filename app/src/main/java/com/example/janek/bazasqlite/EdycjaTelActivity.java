package com.example.janek.bazasqlite;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EdycjaTelActivity extends Activity {
    private long mIdWiersza;
    private EditText mProducentEdycja;
    private EditText mModelEdycja;
    private EditText mAndroidEdycja;
    private EditText mWWWEdycja;
    private Button zapisz;
    private Button anuluj;
    private Button WWW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edycja_tel);
        // odczytanie referencji pól tekstowych na podstawie id
        mProducentEdycja = (EditText) findViewById(R.id.prodPole);
        mModelEdycja = (EditText) findViewById(R.id.modelPole);
        mAndroidEdycja = (EditText) findViewById(R.id.wersjaPole);
        mWWWEdycja = (EditText) findViewById(R.id.wwwPole);
        mIdWiersza = -1;
        if (savedInstanceState != null)
            mIdWiersza =
                    savedInstanceState.getLong(PomocnikBD.ID);
        else {
            Bundle tobolek = getIntent().getExtras();
            if (tobolek != null)
                mIdWiersza = tobolek.getLong(PomocnikBD.ID);
        }
        if (mIdWiersza != -1)
                wypelnijPola();

        // ustawienie obsługi kliknięcia przycisków: Zapisz, Anuluj, WWW
        zapisz = (Button) findViewById(R.id.zapisz);
        anuluj = (Button) findViewById(R.id.anuluj);
        WWW = (Button) findViewById(R.id.wwwPrzycisk);

        zapisz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kliknieciePrzyciskuZapisz();
            }
        });

        anuluj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kliknieciePrzyciskuAnuluj();
            }
        });

        WWW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mWWWEdycja.getText().toString().equals(""))
                {
                    String adres = mWWWEdycja.getText().toString();

                    if(!adres.startsWith("http://") && !adres.startsWith("https://"))
                    {
                        adres = "http://" + adres;
                    }

                    Intent zamiarPrzegladarki = new Intent("android.intent.action.VIEW", Uri.parse(adres));
                    startActivity(zamiarPrzegladarki);
                }

                else
                {
                    Toast.makeText(getApplicationContext(), "Nie podano adresu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(PomocnikBD.ID, mIdWiersza);
    }
    private boolean sprawdzNapisy() {
        return !(mProducentEdycja.getText().toString().equals("")
                || mModelEdycja.getText().toString().equals("")
                || mAndroidEdycja.getText().toString().equals("")
                || mWWWEdycja.getText().toString().equals(""));
    }

    private void wypelnijPola() {
        String projekcja[] = {PomocnikBD.KOLUMNA1, PomocnikBD.KOLUMNA2, PomocnikBD.KOLUMNA3, PomocnikBD.KOLUMNA4};
        Cursor kursorTel = getContentResolver().query(ContentUris.withAppendedId(MojProvider.URI_ZAWARTOSCI,
                        mIdWiersza), projekcja, null, null, null);
        kursorTel.moveToFirst();

        mProducentEdycja.setText(kursorTel.getString(kursorTel.getColumnIndexOrThrow(PomocnikBD.KOLUMNA1)));
        mModelEdycja.setText(kursorTel.getString(kursorTel.getColumnIndexOrThrow(PomocnikBD.KOLUMNA2)));
        mAndroidEdycja.setText(kursorTel.getString(kursorTel.getColumnIndexOrThrow(PomocnikBD.KOLUMNA3)));
        mWWWEdycja.setText(kursorTel.getString(kursorTel.getColumnIndexOrThrow(PomocnikBD.KOLUMNA4)));
        kursorTel.close();

    }
    private void kliknieciePrzyciskuZapisz() {
        if (sprawdzNapisy()) {
            ContentValues wartosci = new ContentValues();
            wartosci.put(PomocnikBD.KOLUMNA1,
                    mProducentEdycja.getText().toString());
            wartosci.put(PomocnikBD.KOLUMNA2,
                    mModelEdycja.getText().toString());
            wartosci.put(PomocnikBD.KOLUMNA3,
                    mAndroidEdycja.getText().toString());
            wartosci.put(PomocnikBD.KOLUMNA4,
                    mWWWEdycja.getText().toString());

            if (mIdWiersza == -1) {
                Uri uriNowego = getContentResolver().insert(
                        MojProvider.URI_ZAWARTOSCI, wartosci);
                mIdWiersza = Integer.parseInt(
                        uriNowego.getLastPathSegment());
            }
            else {
                getContentResolver().update(ContentUris.withAppendedId(MojProvider.URI_ZAWARTOSCI, mIdWiersza), wartosci, null, null);
            }
            setResult(RESULT_OK);
            finish();
        }
        else Toast.makeText(this,
                getString(R.string.wypelnij_pola_komunikat),
                Toast.LENGTH_SHORT).show();
    }
    private void kliknieciePrzyciskuAnuluj() {
        setResult(RESULT_CANCELED);
        finish();
    }
}