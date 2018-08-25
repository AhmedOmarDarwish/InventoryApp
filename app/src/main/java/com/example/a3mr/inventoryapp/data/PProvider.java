package com.example.a3mr.inventoryapp.data;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.a3mr.inventoryapp.data.ProductContract.CONTENT_AUTHORITY;
import static com.example.a3mr.inventoryapp.data.ProductContract.PATH_Products;
import static com.example.a3mr.inventoryapp.data.ProductContract.ProductEntry;


@SuppressLint("Registered")
public class PProvider extends ContentProvider {
    public static final String LOG_TAG = PProvider.class.getSimpleName();
    private static final int Products = 1;
    private static final int Product_ID = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher( UriMatcher.NO_MATCH );

    static {
        sUriMatcher.addURI( CONTENT_AUTHORITY, PATH_Products, Products );
        sUriMatcher.addURI( CONTENT_AUTHORITY, PATH_Products + "/#", Product_ID );
    }

    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper( getContext() );
        return true;
    }

    Cursor cursor;

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match( uri );
        switch (match) {
            case Products:
                cursor = database.query( ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
            case Product_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                cursor = database.query( ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder );
                break;
            default:
                throw new IllegalArgumentException( "Cannot query unknown URI " + uri );

        }
        cursor.setNotificationUri( getContext().getContentResolver(), uri );
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case Products:
                return ProductEntry.CONTENT_LIST_TYPE;
            case Product_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException( "Unknown URI " + uri + " with match " + match );
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case Products:
                return InsertProduct( uri, values );
            default:
                throw new IllegalArgumentException( "Insertion is not supported for " + uri );
        }

    }

    private Uri InsertProduct(Uri uri, ContentValues values) {
        String name = values.getAsString( ProductEntry.COLUMN_PRODUCT_NAME );
        if (name == null) {
            throw new IllegalArgumentException( "Product requires a name" );
        }
        Integer price = values.getAsInteger( ProductEntry.COLUMN_PRODUCT_PRICE );
        if (price == null && price < 0) {
            throw new IllegalArgumentException( "Product requires valid price" );
        }
        Integer Quantity = values.getAsInteger( ProductEntry.COLUMN_PRODUCT_QUANTITY );
        if (Quantity == null && Quantity < 0) {
            throw new IllegalArgumentException( "Product requires valid quantity" );
        }
        Integer phonenumber = values.getAsInteger( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER );
        if (phonenumber == null) {
            throw new IllegalArgumentException( "Supplier requires valid phonenumber" );
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert( ProductEntry.TABLE_NAME, null, values );
        if (id == -1) {
            Log.e( LOG_TAG, "Failed to insert row for " + uri );
            return null;
        }
        getContext().getContentResolver().notifyChange( uri, null );

        return ContentUris.withAppendedId( uri, id );

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match( uri );
        switch (match) {
            case Products:
                return updateProduct( uri, values, selection, selectionArgs );
            case Product_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                return updateProduct( uri, values, selection, selectionArgs );
            default:
                throw new IllegalArgumentException( "Update is not supported for " + uri );
        }
    }


    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey( ProductEntry.COLUMN_PRODUCT_NAME )) {
            String name = values.getAsString( ProductEntry.COLUMN_PRODUCT_NAME );
            if (name == null) {
                throw new IllegalArgumentException( "Product requires a name" );
            }
        }

        if (values.containsKey( ProductEntry.COLUMN_PRODUCT_PRICE )) {
            Integer price = values.getAsInteger( ProductEntry.COLUMN_PRODUCT_PRICE );
            if (price == null && price < 0) {
                throw new IllegalArgumentException( "Product requires valid price" );
            }
        }

        if (values.containsKey( ProductEntry.COLUMN_PRODUCT_QUANTITY )) {
            Integer Quantity = values.getAsInteger( ProductEntry.COLUMN_PRODUCT_QUANTITY );
            if (Quantity == null && Quantity < 0) {
                throw new IllegalArgumentException( "Product requires valid quantity" );
            }
        }
        if (values.containsKey( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER )) {
            Integer phonenumber = values.getAsInteger( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER );
            if (phonenumber == null) {
                throw new IllegalArgumentException( "Supplier requires valid phonenumber" );
            }
        }
        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update( ProductEntry.TABLE_NAME, values, selection, selectionArgs );

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match( uri );
        switch (match) {
            case Products:
                rowsDeleted = database.delete( ProductEntry.TABLE_NAME, selection, selectionArgs );
                break;
            case Product_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf( ContentUris.parseId( uri ) )};
                rowsDeleted = database.delete( ProductEntry.TABLE_NAME, selection, selectionArgs );
                break;
            default:
                throw new IllegalArgumentException( "Deletion is not supported for " + uri );
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange( uri, null );
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }
}
