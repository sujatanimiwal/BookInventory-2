package com.example.android.bookinventory.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Libros.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_PETS = "CREATE TABLE " + BookContract.BooksEntry.TABLE_NAME + "("
                + BookContract.BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookContract.BooksEntry.COLUMN_BOOK_NAME + " TEXT NOT NULL, "
                + BookContract.BooksEntry.COLUMN_BOOK_PRICE + " INTEGER NOT NULL, "
                + BookContract.BooksEntry.COLUMN_BOOK_QUANTITY + " INTEGER DEFAULT 0, "
                + BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT NOT NULL, "
                + BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO + " INTEGER NOT NULL);";

        db.execSQL(CREATE_TABLE_PETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
