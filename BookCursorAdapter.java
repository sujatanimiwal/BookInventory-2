package com.example.android.bookinventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookinventory.data.BookContract;

import org.w3c.dom.Text;

import java.util.concurrent.TimeoutException;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    //bindView to set the textviews
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView qty = (TextView) view.findViewById(R.id.qty);

        String book_name = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BooksEntry.COLUMN_BOOK_NAME));
        String book_price = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BooksEntry.COLUMN_BOOK_PRICE));
        final String book_qty = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BooksEntry.COLUMN_BOOK_QUANTITY));
        final String book_id = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BooksEntry._ID));

        name.setText(book_name);
        price.setText(context.getString(R.string.price_tag) + book_price);
        qty.setText(context.getString(R.string.qty_tag) + book_qty);

        Button sale_button = (Button) view.findViewById(R.id.sale);
        sale_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity Activity = (CatalogActivity) context;
                Activity.productSaleCount(Integer.valueOf(book_id), Integer.valueOf(book_qty));
            }
        });

        Button view_button = (Button) view.findViewById(R.id.view);
        view_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CatalogActivity Activity = (CatalogActivity) context;
                Activity.gotoViewDetails(Integer.parseInt(book_id));
            }
        });
    }
}
