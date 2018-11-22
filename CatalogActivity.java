package com.example.android.bookinventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookinventory.data.BookContract;
import com.example.android.bookinventory.data.BookDbHelper;
import com.example.android.bookinventory.data.BookProvider;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    BookCursorAdapter mCursorAdapter;
    public static final int BOOK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView bookView = (ListView) findViewById(R.id.book_listview);

        View book_emptyView = (View) findViewById(R.id.empty_view);
        bookView.setEmptyView(book_emptyView);

        mCursorAdapter = new BookCursorAdapter(this, null);

        bookView.setAdapter(mCursorAdapter);

        bookView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.e("sujata", "id_1: " + id);
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(BookContract.BooksEntry.CONTENT_URI, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(BOOK_LOADER, null, this);

    }

    public void productSaleCount(int id, int qty) {
        qty = qty - 1;
        if (qty >= 0) {
            ContentValues values = new ContentValues();
            values.put(BookContract.BooksEntry.COLUMN_BOOK_QUANTITY, qty);
            Uri updateUri = ContentUris.withAppendedId(BookContract.BooksEntry.CONTENT_URI, id);
            int rowsAffected = getContentResolver().update(updateUri, values, null, null);
            Toast.makeText(this, R.string.qty_reduced, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
        }
    }

    public void gotoViewDetails(int productID) {
        Log.e("id",productID+"");

        Intent intent = new Intent(CatalogActivity.this, ProductView.class);
        Uri uri = ContentUris.withAppendedId(BookContract.BooksEntry.CONTENT_URI, productID);
        intent.setData(uri);
        startActivity(intent);
    }


    private void insertBook() {
        ContentValues values = new ContentValues();
        values.put(BookContract.BooksEntry.COLUMN_BOOK_NAME, getString(R.string.dummy_book_name));
        values.put(BookContract.BooksEntry.COLUMN_BOOK_PRICE, getString(R.string.dummy_price));
        values.put(BookContract.BooksEntry.COLUMN_BOOK_QUANTITY, getString(R.string.dummy_qty));
        values.put(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME, getString(R.string.dummy_supplier));
        values.put(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO, getString(R.string.dummy_phone_no));

        //inserting the new book into database
        Uri uri = getContentResolver().insert(BookContract.BooksEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllBooks() {
        int rowsDeleted = getContentResolver().delete(BookContract.BooksEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookContract.BooksEntry._ID,
                BookContract.BooksEntry.COLUMN_BOOK_NAME,
                BookContract.BooksEntry.COLUMN_BOOK_PRICE,
                BookContract.BooksEntry.COLUMN_BOOK_QUANTITY};
        return new CursorLoader(this, BookContract.BooksEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
