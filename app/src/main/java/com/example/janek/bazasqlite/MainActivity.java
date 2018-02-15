package com.example.janek.bazasqlite;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public PomocnikBD mPomocnikBD;
    public SQLiteDatabase mBD;
    public ListView lista;
    public SimpleCursorAdapter adapterBazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        lista = (ListView) findViewById(android.R.id.list);
        zapelnijListview();

        lista.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lista.setMultiChoiceModeListener(wyborWielu());

        mPomocnikBD=new PomocnikBD(this);
        mBD=mPomocnikBD.getWritableDatabase();


        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent zamiar = new Intent(MainActivity.this, EdycjaTelActivity.class);
                zamiar.putExtra(PomocnikBD.ID, id);
                startActivityForResult(zamiar, 0);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pasek_akcji, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent nowy = new Intent(this, EdycjaTelActivity.class);
        startActivity(nowy);
        return true;
    }

    private AbsListView.MultiChoiceModeListener wyborWielu()
    {
        return new AbsListView.MultiChoiceModeListener()
        {
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {}

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.pasek_usuwania, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                if(item.getItemId() == R.id.delete_multiple)
                {
                    long[] zaznaczone = lista.getCheckedItemIds();

                    for(int i = 0; i < zaznaczone.length; i++)
                    {
                        getContentResolver().delete(ContentUris.withAppendedId(MojProvider.URI_ZAWARTOSCI, zaznaczone[i]), null, null);
                    }
                    return true;
                }
                return false;
            }
        };
    }

    public void zapelnijListview (){

        getLoaderManager().initLoader(0, null, this);

        //utworzenie mapowania między kolumnami tabeli a kolumnami wyświetlanej listy
        String[] mapujZ = new String[]{PomocnikBD.KOLUMNA1, PomocnikBD.KOLUMNA2};
        int[] mapujDo = new int[]{R.id.etykieta1, R.id.etykieta2};

        //adapter wymaga aby wyniku zapytania znajdowała się kolumna _id
        adapterBazy = new SimpleCursorAdapter(this, R.layout.wiersz_listy, null, mapujZ, mapujDo);
        lista.setAdapter(adapterBazy);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projekcja = {PomocnikBD.ID, PomocnikBD.KOLUMNA1, PomocnikBD.KOLUMNA2, PomocnikBD.KOLUMNA3};
        CursorLoader loaderKursora = new CursorLoader(this, MojProvider.URI_ZAWARTOSCI, projekcja, null, null, null);
        return loaderKursora;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapterBazy.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapterBazy.swapCursor(null);
    }

}
