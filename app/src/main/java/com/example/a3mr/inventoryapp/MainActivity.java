package com.example.a3mr.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import static com.example.a3mr.inventoryapp.data.ProductContract.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int Product_LOADER = 0;
    PCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( MainActivity.this, EditorActivity.class );
                startActivity( intent );
            }
        } );
        ListView productListView = findViewById( R.id.list );
        View emptyView = findViewById( R.id.empty_view );
        productListView.setEmptyView( emptyView );

        mCursorAdapter = new PCursorAdapter( this, null );
        productListView.setAdapter( mCursorAdapter );

        productListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView <?> adapterView, View view, int position, long id) {
                Intent intent = new Intent( MainActivity.this, EditorActivity.class );
                Uri currentPetUri = ContentUris.withAppendedId( ProductEntry.CONTENT_URI, id );
                intent.setData( currentPetUri );
                startActivity( intent );
            }
        } );
       getLoaderManager().initLoader( Product_LOADER, null, this );
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put( ProductEntry.COLUMN_PRODUCT_NAME, "Camera" );
        values.put( ProductEntry.COLUMN_PRODUCT_PRICE, 2000 );
        values.put( ProductEntry.COLUMN_PRODUCT_QUANTITY,10 );
        values.put( ProductEntry.COLUMN_SUPPLIER_NAME, "Egypt" );
        values.put( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, 567567567 );
        Uri newUri = getContentResolver().insert( ProductEntry.CONTENT_URI, values );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;

            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    @Override
    public Loader <Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader( this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor( data );
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        mCursorAdapter.swapCursor( null );
    }

    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete( ProductEntry.CONTENT_URI, null, null );
        Log.v( "CatalogActivity", rowsDeleted + " rows deleted from pet database" );
    }

    private void showDeleteAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( "Delete All Pets? " );
        builder.setNegativeButton( "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );
        builder.setPositiveButton( "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllPets();
            }
        } );
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}