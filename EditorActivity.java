package com.example.android.bookinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookinventory.data.BookContract;
import com.example.android.bookinventory.data.BookDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText mNameEditText, mPriceEditText, mQtyEditText, mSupNameEditText, mSupPhoneEditText;
    private Uri mcurrentUri;
    private boolean mBookHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //retrieving the uri from intent object
        Intent intent = getIntent();
        mcurrentUri = intent.getData();
        Log.e("URIIII", mcurrentUri+"");
        //condition check..whether its a add new book request or edit book request
        if (mcurrentUri == null) {
            setTitle(getString(R.string.add_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.edit_book));
            getLoaderManager().initLoader(CatalogActivity.BOOK_LOADER, null, this);
        }
        mNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mQtyEditText = (EditText) findViewById(R.id.edit_book_qty);
        mSupNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupPhoneEditText = (EditText) findViewById(R.id.edit_Supplier_phone_no);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQtyEditText.setOnTouchListener(mTouchListener);
        mSupNameEditText.setOnTouchListener(mTouchListener);
        mSupPhoneEditText.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void insertNewBook() {
        ContentValues values = new ContentValues();
        values.put(BookContract.BooksEntry.COLUMN_BOOK_NAME, mNameEditText.getText().toString().trim());
        values.put(BookContract.BooksEntry.COLUMN_BOOK_PRICE, Integer.parseInt(mPriceEditText.getText().toString().trim()));
        int qty = 0;// default quantity is zero

        if (!TextUtils.isEmpty(mQtyEditText.getText().toString().trim())) {
            qty = Integer.parseInt(mQtyEditText.getText().toString().trim());
        }
        values.put(BookContract.BooksEntry.COLUMN_BOOK_QUANTITY, qty);
        values.put(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME, mSupNameEditText.getText().toString().trim());
        values.put(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO, Integer.parseInt(mSupPhoneEditText.getText().toString().trim()));
        if ((mcurrentUri == null) &&
                TextUtils.isEmpty(mNameEditText.getText().toString().trim()) && TextUtils.isEmpty(mPriceEditText.getText().toString().trim()) &&
                TextUtils.isEmpty(mQtyEditText.getText().toString().trim()) && TextUtils.isEmpty(mSupNameEditText.getText().toString().trim()) && TextUtils.isEmpty(mSupPhoneEditText.getText().toString().trim())) {

            return;
        } else if (mcurrentUri == null) {
            BookDbHelper mDbHelper = new BookDbHelper(this);
            Uri uri = getContentResolver().insert(BookContract.BooksEntry.CONTENT_URI, values);
            Toast toast = new Toast(this);
            toast.makeText(this, "Book Saved", Toast.LENGTH_SHORT).show();
            mBookHasChanged = false;

        } else {
            int rowsAffected = getContentResolver().update(mcurrentUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_Failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                mBookHasChanged = false;
                Toast.makeText(this, getString(R.string.update_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                insertNewBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the booko hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book addition in database so hide the "Delete" menu item.
        if (mcurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookContract.BooksEntry._ID,
                BookContract.BooksEntry.COLUMN_BOOK_NAME,
                BookContract.BooksEntry.COLUMN_BOOK_QUANTITY,
                BookContract.BooksEntry.COLUMN_BOOK_PRICE,
                BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO};
        return new CursorLoader(this,
                mcurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int id= cursor.getColumnIndex(BookContract.BooksEntry._ID);
            Log.e("IDDD",id+"");
            int nameColumnIndex = cursor.getColumnIndex(BookContract.BooksEntry.COLUMN_BOOK_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookContract.BooksEntry.COLUMN_BOOK_PRICE);
            int qtyColumnIndex = cursor.getColumnIndex(BookContract.BooksEntry.COLUMN_BOOK_QUANTITY);
            int suppNameColumnIndex = cursor.getColumnIndex(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int suppPhoneColumnIndex = cursor.getColumnIndex(BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int qty = cursor.getInt(qtyColumnIndex);
            String supp_name = cursor.getString(suppNameColumnIndex);
            int supp_phone = cursor.getInt(suppPhoneColumnIndex);

            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQtyEditText.setText(Integer.toString(qty));
            mSupNameEditText.setText(supp_name);
            mSupPhoneEditText.setText(Integer.toString(supp_phone));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQtyEditText.setText("");
        mSupNameEditText.setText("");
        mSupPhoneEditText.setText("");
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (mcurrentUri != null) {
            int rowsAffected = getContentResolver().delete(mcurrentUri, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful), Toast.LENGTH_SHORT).show();

            }
        }
        finish();
    }
}
