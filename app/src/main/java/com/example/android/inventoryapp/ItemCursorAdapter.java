package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Zohaib on 28/09/16.
 */
public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView textViewName = (TextView) view.findViewById(R.id.name);
        TextView textViewQuantity = (TextView) view.findViewById(R.id.quantity);
        TextView textViewPrice = (TextView) view.findViewById(R.id.price);
        final Button sellButton = (Button) view.findViewById(R.id.sell_button);
        sellButton.setTag(cursor.getLong(cursor.getColumnIndex(InventoryEntry._ID)));

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,
                        Long.parseLong(sellButton.getTag().toString()));
                cursor.moveToPosition(Integer.parseInt(sellButton.getTag().toString()) - 1);
                int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
                ContentValues values = new ContentValues();
                quantity--;
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                context.getContentResolver().update(uri, values, null, null);
            }
        });

        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME));
        String quantity = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE));

        textViewName.setText(name);
        textViewQuantity.setText(quantity);
        textViewPrice.setText(price);

    }
}
