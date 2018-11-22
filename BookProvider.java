package com.example.android.bookinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.bookinventory.data.BookContract.BooksEntry;

import static com.example.android.bookinventory.data.BookContract.BooksEntry.*;
import static com.example.android.bookinventory.data.BookContract.CONTENT_AUTHORITY;
import static com.example.android.bookinventory.data.BookContract.PATH_BOOKS;

public class BookProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the  tabbooksle
     */
    private static final int BOOKS = 100;

    /**
     * URI matcher code for the content URI for a single book in the books table
     */
    private static final int BOOKS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PATH_BOOKS + "/#", BOOKS_ID);

    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    BookDbHelper bookdb;

    @Override
    public boolean onCreate() {
        bookdb = new BookDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        BookDbHelper mDbHelper = new BookDbHelper(getContext());
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS: //for whole database
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case BOOKS_ID: //for a single book in database
                cursor = database.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.e("URI msg", "URI is: " + uri);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case BOOKS_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updatePet(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }
        if (values.containsKey(COLUMN_BOOK_NAME)) {
            String name = values.getAsString(COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book must have a name");
            }
        }
        if (values.containsKey(COLUMN_BOOK_PRICE)) {
            Integer price = values.getAsInteger(COLUMN_BOOK_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Book must have a valid price");
            }
        }
        if (values.containsKey(COLUMN_BOOK_QUANTITY)) {
            Integer quantity = values.getAsInteger(COLUMN_BOOK_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Book must have a valid Quantity");
            }
        }
        if (values.containsKey(COLUMN_BOOK_SUPPLIER_NAME)) {
            String sup_name = values.getAsString(COLUMN_BOOK_SUPPLIER_NAME);
            if (sup_name == null) {
                throw new IllegalArgumentException("Book must have a supplier's name");
            }
        }
        if (values.containsKey(COLUMN_BOOK_SUPPLIER_PHONE_NO)) {
            Integer sup_phone_no = values.getAsInteger(COLUMN_BOOK_SUPPLIER_PHONE_NO);
            if (sup_phone_no < 0) {
                throw new IllegalArgumentException("Book must have a valid Supplier's phone number");
            }
        }
        BookDbHelper mDbHelper = new BookDbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(BooksEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        BookDbHelper mDbHelper = new BookDbHelper(getContext());
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                rowsDeleted = db.delete(BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOKS_ID:
                // Delete a single row given by the ID in the URI
                selection = BooksEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(BooksEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return CONTENT_LIST_TYPE;
            case BOOKS_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        String name = values.getAsString(COLUMN_BOOK_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Book must have a name");
        }
        Integer price = values.getAsInteger(COLUMN_BOOK_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Book must have a valid price");
        }
        Integer quantity = values.getAsInteger(COLUMN_BOOK_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Book must have a valid Quantity");
        }
        String sup_name = values.getAsString(COLUMN_BOOK_SUPPLIER_NAME);
        if (sup_name == null) {
            throw new IllegalArgumentException("Book must have a supplier's name");
        }
        Integer sup_phone_no = values.getAsInteger(COLUMN_BOOK_SUPPLIER_PHONE_NO);
        if (sup_phone_no < 0) {
            throw new IllegalArgumentException("Book must have a valid Supplier's phone number");
        }

        BookDbHelper mDbHelper = new BookDbHelper(getContext());
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(TABLE_NAME, null, values);
        if (id == -1) {
            Log.e("insertion error", "insertion not possible");
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
}
