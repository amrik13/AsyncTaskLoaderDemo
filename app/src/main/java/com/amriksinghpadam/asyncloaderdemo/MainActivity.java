package com.amriksinghpadam.asyncloaderdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
    private ListView listView;
    private String[] listItemArray;
    private TextView textView;
    private Button btn, addBtn;
    private ArrayAdapter<String> adapter;
    private EditText editText;
    private MyDatabase db;
    private Menu menu;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listViewId);
        textView = findViewById(R.id.textViewId);
        btn = findViewById(R.id.buttonId);
        editText = findViewById(R.id.editTextId);
        addBtn = findViewById(R.id.addBtnId);
        menu = findViewById(R.id.resetId);
        progressBar = findViewById(R.id.progressBarId);

        db = new MyDatabase(MainActivity.this);
        //listItemArray = getResources().getStringArray(R.array.listItems);
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor data = db.readItem();
                if (data.getCount() != 0) {
                    LoaderManager loaderManager = getSupportLoaderManager();
                    if (loaderManager.getLoader(1) == null) {
                        loaderManager.initLoader(1, null, MainActivity.this);
                    } else {
                        loaderManager.restartLoader(1, null, MainActivity.this);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No Data Found!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToAdd = editText.getText().toString().trim();
                if (!TextUtils.isEmpty(textToAdd)) {
                    db.insert(textToAdd);
                    editText.setText("");
                    if (getSupportLoaderManager().getLoader(1) != null) {
                        getSupportLoaderManager().restartLoader(1, null, MainActivity.this);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Empty Field!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader(this) {
            @Override
            protected void onStartLoading() {
                progressBar.setVisibility(View.VISIBLE);
                textView.setText("Wait, We Are Retrieving Your Data!");
                forceLoad();
            }
            @Nullable
            @Override
            public String loadInBackground() {
                ArrayList<String> list = new ArrayList<>();
                Cursor data = db.readItem();
                while (data.moveToNext()) {
                    list.add(data.getString(1));
                }
                listItemArray = list.toArray(new String[list.size()]);
                //for loading delay
                synchronized (this) {
                    try {
                        wait(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }
    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        adapter.clear();
        int l = 0;
        while (l < listItemArray.length) {
            adapter.add(listItemArray[l]);
            l++;
        }
        progressBar.setVisibility(View.INVISIBLE);
        textView.setText("Retrieving Done!");
    }
    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Toast.makeText(MainActivity.this, "Loader Reset", Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        db.deleteItem();
        if (adapter != null) {
            adapter.clear();
            Toast.makeText(MainActivity.this, "Deleted!!", Toast.LENGTH_SHORT).show();
            textView.setText("Add Item Below!");
        }
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        getSupportLoaderManager().destroyLoader(1);
    }
}
