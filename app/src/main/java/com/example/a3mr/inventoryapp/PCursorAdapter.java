package com.example.a3mr.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.a3mr.inventoryapp.data.ProductContract.ProductEntry;

public class PCursorAdapter extends CursorAdapter {
    public PCursorAdapter(Context context, Cursor c) {
        super( context, c, 0 );
    }
    TextView instock;
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from( context ).inflate( R.layout.list_item, parent, false );
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productname = view.findViewById( R.id.product_name );
        TextView productprice = view.findViewById( R.id.product_price );
        TextView productquantity = view.findViewById( R.id.product_quantity );
        instock = view.findViewById( R.id.stockAvailable );
        ImageButton buttonSell = view.findViewById( R.id.sell);

        int rowID = cursor.getColumnIndex( ProductEntry._ID );
        int nameColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_NAME );
        int priceColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_PRICE );
        int quantityColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_QUANTITY );

        final int currentRowID = cursor.getInt( rowID );
        String name = cursor.getString( nameColumnIndex );
        int price = cursor.getInt( priceColumnIndex );
        final int quantity = cursor.getInt( quantityColumnIndex );
        productquantity.setText( Integer.toString( quantity ) );
        productname.setText( name );
        productprice.setText( Integer.toString( price ) );

       if(quantity == 0) {
            instock.setText( R.string.out_of_stock );
        }else {
           instock.setText( R.string.in_stock );
       }


        buttonSell.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId( ProductEntry.CONTENT_URI, currentRowID );
                ProductQuantity( context, uri, quantity );

            }
        } );
    }

    private void ProductQuantity(Context context, Uri currentUri, int currentQuantity) {
        if (currentQuantity > 0) {
            currentQuantity -= 1;
            ContentValues values = new ContentValues();
            values.put( ProductEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity );
            int updatedRow = context.getContentResolver().update( currentUri, values, null, null );

        } else {
            Toast.makeText( context, "Can't sell this not available for now", Toast.LENGTH_SHORT ).show();
        }
    }
}