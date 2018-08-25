package com.example.a3mr.inventoryapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import static com.example.a3mr.inventoryapp.data.ProductContract.*;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks <Cursor> {
    private static final int EXISTING_Product_LOADER = 0;
    private Uri mCurrentProductUri;
    EditText pname;
    EditText pprice;
    EditText pquantity;
    EditText suppliername;
    EditText supplierphonenumber;
    ImageButton call;
    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_editor );
        call=findViewById( R.id.callsupplier );
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            setTitle( getString( R.string.editor_activity_title_new_product ) );
            invalidateOptionsMenu();
        } else {
            setTitle( getString( R.string.editor_activity_title_edit ) );
            getLoaderManager().initLoader( EXISTING_Product_LOADER, null, this );
        }
        pname = findViewById( R.id.pname );
        pprice = findViewById( R.id.pprice );
        pquantity = findViewById( R.id.pquantity );
        suppliername = findViewById( R.id.sname );
        supplierphonenumber = findViewById( R.id.sphonenumber );

        pname.setOnTouchListener( mTouchListener );
        pprice.setOnTouchListener( mTouchListener );
        pquantity.setOnTouchListener( mTouchListener );
        suppliername.setOnTouchListener( mTouchListener );
        supplierphonenumber.setOnTouchListener( mTouchListener );
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 String phone = supplierphonenumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phone)) {
                    Uri uri = Uri.parse("tel:" + phone);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(EditorActivity.this,"Invalid phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void savePet() {
        String productname = pname.getText().toString().trim();
        String producprice = pprice.getText().toString().trim();
        String producquantity = pquantity.getText().toString().trim();
        String suppliername = this.suppliername.getText().toString().trim();
        String supplierphone = supplierphonenumber.getText().toString().trim();


        if (mCurrentProductUri == null &&
                TextUtils.isEmpty( productname ) && TextUtils.isEmpty( producprice ) &&
                TextUtils.isEmpty( producquantity ) && TextUtils.isEmpty( suppliername ) && TextUtils.isEmpty( supplierphone )) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put( ProductEntry.COLUMN_PRODUCT_NAME, productname );
        values.put( ProductEntry.COLUMN_PRODUCT_PRICE, producprice );
        values.put( ProductEntry.COLUMN_PRODUCT_QUANTITY, producquantity );
        values.put( ProductEntry.COLUMN_SUPPLIER_NAME, suppliername );
        values.put( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierphone );

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert( ProductEntry.CONTENT_URI, values );

            if (newUri == null) {

                Toast.makeText( this, getString( R.string.error_saving ),
                        Toast.LENGTH_SHORT ).show();
            } else {

                Toast.makeText( this, getString( R.string.product_saved ),
                        Toast.LENGTH_SHORT ).show();
            }
        } else {

            int rowsAffected = getContentResolver().update( mCurrentProductUri, values, null, null );
            if (rowsAffected == 0) {
                Toast.makeText( this, getString( R.string.editor_update_product_failed ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText( this, getString( R.string.editor_update_product_successful ),
                        Toast.LENGTH_SHORT ).show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.editor_menu, menu );
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu( menu );
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem( R.id.action_delete );
            menuItem.setVisible( false );
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask( EditorActivity.this );
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask( EditorActivity.this );
                            }
                        };
                showUnsavedChangesDialog( discardButtonClickListener );
                return true;
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog( discardButtonClickListener );
    }

    @Override
    public Loader <Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader( this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null );
    }


    @Override
    public void onLoadFinished(Loader <Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_NAME );
            int priceColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_PRICE );
            int quantityColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_PRODUCT_QUANTITY );
            int snameColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_SUPPLIER_NAME );
            int sphoneColumnIndex = cursor.getColumnIndex( ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER );

            String name = cursor.getString( nameColumnIndex );
            int price = cursor.getInt( priceColumnIndex );
            int quantity = cursor.getInt( quantityColumnIndex );
            String sname = cursor.getString( snameColumnIndex );
            String phone = cursor.getString( sphoneColumnIndex );

            pname.setText( name );
            pprice.setText( Integer.toString( price ) );
            pquantity.setText( Integer.toString( quantity ) );
            suppliername.setText( sname );
            supplierphonenumber.setText( phone );

        }
    }

    @Override
    public void onLoaderReset(Loader <Cursor> loader) {
        pname.setText( "" );
        pprice.setText( "" );
        pquantity.setText( "" );
        suppliername.setText( "" );
        supplierphonenumber.setText( "" );
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.unsaved_changes_dialog_msg );
        builder.setPositiveButton( R.string.discard, discardButtonClickListener );
        builder.setNegativeButton( R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage( R.string.delete_dialog_msg );
        builder.setPositiveButton( R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        } );
        builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        } );

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete( mCurrentProductUri, null, null );
            if (rowsDeleted == 0) {
                Toast.makeText( this, getString( R.string.editor_delete_product_failed ),
                        Toast.LENGTH_SHORT ).show();
            } else {
                Toast.makeText( this, getString( R.string.editor_delete_product_successful ),
                        Toast.LENGTH_SHORT ).show();
            }
            finish();
        }
    }
}
