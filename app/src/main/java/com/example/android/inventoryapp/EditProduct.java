package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.IOException;

public class EditProduct extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private EditText nameEditText, quantityEditText, priceEditText, supplierEditText;
    private Uri currentItemUri, imageUri;
    private Button receivedShipmentButton, trackSaleButton, orderFromSupplierButton, deleteButton,
            chooseImageButton;
    private ImageView itemImageView;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        Intent intent = getIntent();

        currentItemUri = intent.getData();

        receivedShipmentButton = (Button) findViewById(R.id.shipment_received);
        trackSaleButton = (Button) findViewById(R.id.track_sale);
        orderFromSupplierButton = (Button) findViewById(R.id.order_from_supplier);
        deleteButton = (Button) findViewById(R.id.delete_item);
        chooseImageButton = (Button) findViewById(R.id.choose_image_button);
        itemImageView = (ImageView) findViewById(R.id.item_image_view);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent;

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            }
        });

        if (currentItemUri == null) {
            setTitle(getString(R.string.add_item_text));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
            hideEditFunctons();
        } else {
            setTitle(getString(R.string.edit_item_text));
            setButtonListeners();
            getLoaderManager().initLoader(1, null, this);
        }

        // Find all relevant views that we will need to read user input from
        nameEditText = (EditText) findViewById(R.id.name_editable);
        quantityEditText = (EditText) findViewById(R.id.quantity_editable);
        priceEditText = (EditText) findViewById(R.id.price_editable);
        supplierEditText = (EditText) findViewById(R.id.supplier_editable);

    }

    private void setButtonListeners() {
        receivedShipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shipmentDialog();
            }
        });

        trackSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saleTrackDialog();
            }
        });

        orderFromSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderFromSupplierIntent();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCurrentItem();
            }
        });
    }

    private void deleteCurrentItem() {

        getContentResolver().delete(currentItemUri,null, null);
        finish();
        return;
    }

    private void hideEditFunctons() {
        receivedShipmentButton.setVisibility(View.GONE);
        trackSaleButton.setVisibility(View.GONE);
        orderFromSupplierButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        itemImageView.setVisibility(View.GONE);
    }

    private void orderFromSupplierIntent() {

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        String[] supplierAddress = {""};
        String[] projection = {InventoryEntry.COLUMN_ITEM_SUPPLIER};
        Cursor cursor = getContentResolver().query(currentItemUri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            supplierAddress[0] = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER));
        }
        intent.putExtra(Intent.EXTRA_EMAIL, supplierAddress);
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.item_request_intent_text));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.supply_request_string));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void shipmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a value");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String resultString = input.getText().toString();
                int shipmentValueToAdd = Integer.parseInt(resultString);
                String[] projection = {InventoryEntry.COLUMN_ITEM_QUANTITY};
                Cursor currentStockCursor = getContentResolver().query(currentItemUri, projection, null, null, null);
                int currentStock = 0;
                if (currentStockCursor != null) {
                    try {
                        currentStockCursor.moveToNext();
                        currentStock = Integer.parseInt(currentStockCursor.getString(currentStockCursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY)));
                    } finally {
                        currentStockCursor.close();
                    }
                } else {
                    shipmentValueToAdd = 0;
                }
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, shipmentValueToAdd + currentStock);
                getContentResolver().update(currentItemUri, values, null, null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saleTrackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a value");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String resultString = input.getText().toString();
                int shipmentValueToSubtract = Integer.parseInt(resultString);
                String[] projection = {InventoryEntry.COLUMN_ITEM_QUANTITY};
                Cursor currentStockCursor = getContentResolver().query(currentItemUri, projection, null, null, null);
                int currentStock = 0;
                if (currentStockCursor != null) {
                    try {
                        currentStockCursor.moveToNext();
                        currentStock = Integer.parseInt(currentStockCursor.getString(currentStockCursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY)));
                    } finally {
                        currentStockCursor.close();
                    }
                } else {
                    shipmentValueToSubtract = 0;
                }
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, currentStock - shipmentValueToSubtract);
                getContentResolver().update(currentItemUri, values, null, null);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
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
                || TextUtils.isEmpty(priceEditText.getText().toString().trim())
                || TextUtils.isEmpty(supplierEditText.getText().toString().trim())
                || !android.util.Patterns.EMAIL_ADDRESS.matcher(supplierEditText.getText().toString().trim()).matches()
                || TextUtils.isEmpty(imageUri.toString())) {
            Toast.makeText(this, R.string.incomplete_entered_text, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String nameString = nameEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String imageUriString = imageUri.toString();
        String supplierString = supplierEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imageUriString);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, supplierString);


        Uri resultRowUri = null;
        int updated = -1;
        if (currentItemUri == null) {
            // Insert a new row for item in the database, returning the ID of that new row.
            resultRowUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
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
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_SUPPLIER};

        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int itemNameIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int itemQuantityIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int itemPriceIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int itemImageIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            int itemSupplierIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);

            nameEditText.setText(cursor.getString(itemNameIndex));
            quantityEditText.setText(cursor.getString(itemQuantityIndex));
            priceEditText.setText(cursor.getString(itemPriceIndex));

            imageUri = Uri.parse(cursor.getString(itemImageIndex));

            Bitmap imageBitmap = null;

            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                Log.e("EditProduct", "Error: " + e);
            }

            itemImageView.setImageBitmap(imageBitmap);
            supplierEditText.setText(cursor.getString(itemSupplierIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.clearComposingText();
        quantityEditText.clearComposingText();
        priceEditText.clearComposingText();
        supplierEditText.clearComposingText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }
}
