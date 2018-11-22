package com.example.android.bookinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookinventory.data.BookContract;

import java.util.Currency;

public class ProductView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    TextView mNameEditText, mPriceEditText, mQtyEditText, mSupNameEditText, mSupPhoneEditText;
    private Uri mcurrentUri;
    private int quantity = 0;
    private int phone_no;
    private boolean mBookHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        //retrieving the uri from intent object
        Intent intent = getIntent();
        mcurrentUri = intent.getData();
        Log.e("URI", mcurrentUri+"");
        mNameEditText = (TextView) findViewById(R.id.name);
        mPriceEditText = (TextView) findViewById(R.id.price);
        mQtyEditText = (TextView) findViewById(R.id.qty);
        mSupNameEditText = (TextView) findViewById(R.id.sup_name);
        mSupPhoneEditText = (TextView) findViewById(R.id.sup_phone);

        Button delete_button = (Button) findViewById(R.id.delete);
        Button qty_inc = (Button) findViewById(R.id.inc_qty);
        Button qty_dec = (Button) findViewById(R.id.dec_qty);
        Button call_sup = (Button) findViewById(R.id.call_sup);

        //setting up the functionality of delete, call supplier, + and - buttons
        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
        call_sup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSupplier(v);
            }
        });

        qty_inc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQty(v);
            }
        });

        qty_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementQty(v);
            }
        });
        getLoaderManager().initLoader(CatalogActivity.BOOK_LOADER, null, this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {BookContract.BooksEntry._ID,
                BookContract.BooksEntry.COLUMN_BOOK_NAME,
                BookContract.BooksEntry.COLUMN_BOOK_QUANTITY,
                BookContract.BooksEntry.COLUMN_BOOK_PRICE,
                BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookContract.BooksEntry.COLUMN_BOOK_SUPPLIER_PHONE_NO};
        return new CursorLoader(this, mcurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
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

            quantity = qty;
            phone_no = supp_phone;
            Log.e("Name", name);

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
                // User clicked the "Delete" button, so delete the pet.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
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

    public void callSupplier(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(getString(R.string.tel) + phone_no));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public void incrementQty(View view) {
        quantity = quantity + 1;
        changeQuantity(quantity);
    }

    /**
     * This method is called when the minus button is clicked.
     */
    public void decrementQty(View view) {
        if (quantity <= 0) {
            // Show an error message as a toast
            Toast.makeText(this, R.string.neg_not_allowed, Toast.LENGTH_SHORT).show();
            // Exit this method early because there's nothing left to do
            return;
        }
        quantity = quantity - 1;
        changeQuantity(quantity);
    }

    private void changeQuantity(int number) {
        mQtyEditText.setText("" + number);
    }


}
