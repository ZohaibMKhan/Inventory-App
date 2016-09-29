package com.example.android.inventoryapp;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

public class EditProduct extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    private EditText nameEditText, quantityEditText, priceEditText;
    private Uri currentItemUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Intent intent = getIntent();

        currentItemUri = intent.getData();

        if (currentItemUri == null) {
            setTitle(getString(R.string.add_item_text));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_item_text));
            getLoaderManager().initLoader(1, null, this);
        }

        // Find all relevant views that we will need to read user input from
        nameEditText= (EditText) findViewById(R.id.name_editable);
        quantityEditText = (EditText) findViewById(R.id.quantity_editable);
        priceEditText = (EditText) findViewById(R.id.price_editable);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveItem();
                // Exit activity
                finish();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(EditProduct.this);
                return true;
            default:
                return false;
        }
    }

    private void saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space

        int weight = 0;
        if (TextUtils.isEmpty(nameEditText.getText().toString().trim())
        || TextUtils.isEmpty(quantityEditText.getText().toString().trim())
        || TextUtils.isEmpty(priceEditText.getText().toString().trim())) {
            Toast.makeText(this, R.string.incomplete_entered_text, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String nameString = nameEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, priceString);

        Uri resultRowUri = null;
        int updated = -1;
        if (currentItemUri == null) {
            // Insert a new row for pet in the database, returning the ID of that new row.
            resultRowUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
        } else {
            updated = getContentResolver().update(currentItemUri, values, null, null);
        }

        // Show a toast message depending on whether or not the insertion was successful
        if (resultRowUri == null && updated == -1) {
            Toast.makeText(this, getString(R.string.error_saving_item), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_item_saved_successful), Toast.LENGTH_SHORT).show();
        }
    }







    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.edit_product_menu, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE};

        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int itemNameIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            int itemQuantityIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);
            int itemPriceIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);


            nameEditText.setText(cursor.getString(itemNameIndex));
            quantityEditText.setText(cursor.getString(itemQuantityIndex));
            priceEditText.setText(cursor.getString(itemPriceIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.clearComposingText();
        quantityEditText.clearComposingText();
        priceEditText.clearComposingText();
    }
}
