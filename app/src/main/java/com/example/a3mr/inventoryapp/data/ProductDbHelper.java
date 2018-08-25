package com.example.a3mr.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.a3mr.inventoryapp.data.ProductContract.*;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ProductInventory.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super( context, DATABASE_NAME, null, DATABASE_VERSION );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_Product_TABLE = " CREATE TABLE " + ProductEntry.TABLE_NAME + "("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL  DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL  DEFAULT 0, "
                + ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT , "
                + ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " VARCHAR(20) NOT NULL DEFAULT 0);";
        db.execSQL( SQL_CREATE_Product_TABLE );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
