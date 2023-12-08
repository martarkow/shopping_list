package com.example.listazakupow;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.listazakupw.R;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText editTextItem;
    private Button buttonAdd;
    private ListView listViewItems;
    private ArrayAdapter<ShoppingItem> adapter;
    private DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);

        editTextItem = findViewById(R.id.editTextItem);
        buttonAdd = findViewById(R.id.buttonAdd);
        listViewItems = findViewById(R.id.listViewItems);

        setSupportActionBar(findViewById(R.id.toolbar));

        ArrayList<ShoppingItem> items = readItemsFromDatabase();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, items);
        listViewItems.setAdapter(adapter);
        listViewItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItem = editTextItem.getText().toString();
                if (!newItem.trim().isEmpty()) {
                    insertItemToDatabase(newItem);
                    updateUI();
                    editTextItem.getText().clear();
                }
            }
        });

        listViewItems.setOnItemClickListener((parent, view, position, id) -> {
            ShoppingItem selectedItem = adapter.getItem(position);
            toggleItemPurchased(selectedItem);
            updateUI();
        });

        listViewItems.setOnItemLongClickListener((parent, view, position, id) -> {
            ShoppingItem selectedItem = adapter.getItem(position);
            deleteItemFromDatabase(selectedItem);
            updateUI();
            return true;
        });
    }

    private ArrayList<ShoppingItem> readItemsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<ShoppingItem> items = new ArrayList<>();

        Cursor cursor = db.query(
                DbHelper.TABLE_ITEMS,
                new String[]{DbHelper.COLUMN_ID, DbHelper.COLUMN_NAME, DbHelper.COLUMN_PURCHASED},
                null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ID));
            String itemName = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_NAME));
            boolean isPurchased = cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_PURCHASED)) == 1;

            if (isPurchased) {
                isPurchased = false;
            }

            ShoppingItem shoppingItem = new ShoppingItem(itemId, itemName, isPurchased);
            items.add(shoppingItem);
        }

        cursor.close();
        return items;
    }

    private void insertItemToDatabase(String itemName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_NAME, itemName);
        values.put(DbHelper.COLUMN_PURCHASED, 0);

        db.insert(DbHelper.TABLE_ITEMS, null, values);
    }

    private void toggleItemPurchased(ShoppingItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_PURCHASED, item.isPurchased() ? 0 : 1);

        db.update(
                DbHelper.TABLE_ITEMS,
                values,
                DbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())}
        );

        item.setPurchased(!item.isPurchased());
    }

    private void deleteItemFromDatabase(ShoppingItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(
                DbHelper.TABLE_ITEMS,
                DbHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())}
        );
    }

    private void updateUI() {
        adapter.clear();
        adapter.addAll(readItemsFromDatabase());
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_all) {
            clearAllItems();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearAllItems() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_ITEMS, null, null);
        updateUI();
        Toast.makeText(this, "Wszystkie przedmioty z listy zakupów zostały usunięte", Toast.LENGTH_SHORT).show();
    }
}