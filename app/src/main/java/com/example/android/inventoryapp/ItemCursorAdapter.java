package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryDbHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
/**
 * Created by Zohaib on 28/09/16.
 */
public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    private int currentId = 0;
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView tvname = (TextView) view.findViewById(R.id.name);
        TextView tvquantity = (TextView) view.findViewById(R.id.quantity);
        TextView tvprice = (TextView) view.findViewById(R.id.price);
        final Button sold = (Button) view.findViewById(R.id.sell_button);
        sold.setTag(cursor.getLong(0));

        //Log.v("Adapter", "cursors ID: " + cursor.getLong(0));

        sold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,
                        Long.parseLong(sold.getTag().toString()));
                Log.v("Adapter", "uri: " + uri.toString());
                Log.v("Adapter", "id: " + sold.getTag());
                cursor.moveToPosition(Integer.parseInt(sold.getTag().toString()));
                int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
                Log.v("Adapter", "quantity: " + quantity);
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                context.getContentResolver().update(uri,values, null, null);
            }
        });

        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
        String quantity = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));

        tvname.setText(name);
        tvquantity.setText("Quantity: " + quantity);
        tvprice.setText("Price: £" + price);

    }
}
