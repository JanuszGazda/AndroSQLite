package com.example.janek.bazasqlite;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static com.example.janek.bazasqlite.PomocnikBD.TW_BAZY;

public class MojProvider extends ContentProvider {

    private PomocnikBD mPomocnikBD;
    private SQLiteDatabase mBD;

    //identyfikator (ang. authority) dostawcy
    private static final String IDENTYFIKATOR =
            "com.example.janek.bazasqlite.MojProvider";
    //stała – aby nie trzeba było wpisywać tekstu samodzielnie
    public static final Uri URI_ZAWARTOSCI = Uri.parse("content://"
            + IDENTYFIKATOR + "/" + PomocnikBD.NAZWA_TABELI);
    //stałe pozwalające zidentyfikować rodzaj rozpoznanego URI
    private static final int CALA_TABELA = 1;
    private static final int WYBRANY_WIERSZ = 2;
    //UriMacher z pustym korzeniem drzewa URI (NO_MATCH)
    private static final UriMatcher sDopasowanieUri =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //dodanie rozpoznawanych URI
        sDopasowanieUri.addURI(IDENTYFIKATOR, PomocnikBD.NAZWA_TABELI,
                CALA_TABELA);
        sDopasowanieUri.addURI(IDENTYFIKATOR, PomocnikBD.NAZWA_TABELI +
                "/#", WYBRANY_WIERSZ);
    }

    @Override
    public boolean onCreate() {
        mPomocnikBD=new PomocnikBD(getContext());
        mBD=mPomocnikBD.getWritableDatabase();
        mBD.execSQL(TW_BAZY);
        return true;
    }

    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs,
                        String sortOrder) {
        int typUri = sDopasowanieUri.match(uri);

        //otwieranie bazy danych
        mBD = mPomocnikBD.getReadableDatabase();

        Cursor kursor = null;
        switch (typUri) {
            case CALA_TABELA:
                kursor =  mBD.query(true, //distinct
                        PomocnikBD.NAZWA_TABELI, //tabela
                        new String[]{PomocnikBD.ID,PomocnikBD.KOLUMNA1,PomocnikBD.KOLUMNA2, PomocnikBD.KOLUMNA3, PomocnikBD.KOLUMNA4},
                        //kolumny
                        null, //where
                        null, //whereArgs - argumenty zastępujące "?" w where
                        null, //group by
                        null, //having
                        null, //order by
                        null); //limit
                break;
            case WYBRANY_WIERSZ:
                //umieszczenie danych w kursorze...
                kursor = mBD.query(false,
                        PomocnikBD.NAZWA_TABELI,
                        projection,
                        dodajIdDoSelekcji(selection,uri), selectionArgs, null, null, sortOrder, null, null);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " + uri);
        }
        //URI może być monitorowane pod kątem zmiany danych – tu jest
        //rejestrowane. Obserwator (którego trzeba zarejestrować
        //będzie powiadamiany o zmianie danych)
        try {
            kursor.setNotificationUri(getContext().
                    getContentResolver(), uri);

        } catch (NullPointerException e){}
        return kursor;
    }

    private String dodajIdDoSelekcji(String selekcja, Uri uri)
    {
        if (selekcja!=null && !selekcja.equals(""))
            selekcja = selekcja + " and " + PomocnikBD.ID + "="
                    + uri.getLastPathSegment();
        else
            selekcja = PomocnikBD.ID + "=" +
                    uri.getLastPathSegment();
        return selekcja;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //czy wiersz czy cała tabela i otworzenie bazy
        int typUri = sDopasowanieUri.match(uri);
        //otwieranie magazynu
        mBD = mPomocnikBD.getWritableDatabase();

        long idDodanego = 0;
        switch (typUri) {
            case CALA_TABELA:
                //zapisanie do magazynu – np. insert do bazy...
                idDodanego = mBD.insert(mPomocnikBD.NAZWA_TABELI, null, values);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " +
                        uri);
        }
        //powiadomienie o zmianie danych (->np. odświeżenie listy)
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(PomocnikBD.NAZWA_TABELI + "/" + idDodanego);
    }

    @Override
    public int delete(Uri uri, String selection,
                      String[] selectionArgs) {
        int typUri = sDopasowanieUri.match(uri);
        //otwieranie magazynu
        mBD = mPomocnikBD.getWritableDatabase();

        int liczbaUsunietych = 0;
        switch (typUri) {
            case CALA_TABELA:
                liczbaUsunietych = mBD.delete(PomocnikBD.NAZWA_TABELI,
                        selection, //WHERE
                        selectionArgs); //usuwanie rekordów
                break;
            case WYBRANY_WIERSZ:        //usuwanie rekordu (może się nie udać)
                try{
                    liczbaUsunietych = mBD.delete(mPomocnikBD.NAZWA_TABELI, dodajIdDoSelekcji(selection, uri), selectionArgs );
                } catch(Exception e){
                    System.out.println("Did not delete single row!");
                }
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return liczbaUsunietych;
    }


    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int typUri = sDopasowanieUri.match(uri);
        //otwieranie magazynu
        mBD = mPomocnikBD.getWritableDatabase();

        int liczbaZaktualizowanych = 0;
        switch (typUri) {
            case CALA_TABELA:
                liczbaZaktualizowanych = mBD.update(PomocnikBD.NAZWA_TABELI, values, selection, selectionArgs);
                break;
            case WYBRANY_WIERSZ:
                liczbaZaktualizowanych = mBD.update(PomocnikBD.NAZWA_TABELI, values, dodajIdDoSelekcji(selection, uri), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " +
                        uri);
        } //powiadomienie o zmianie danych
        getContext().getContentResolver().notifyChange(uri, null);
        return liczbaZaktualizowanych;
    }
}