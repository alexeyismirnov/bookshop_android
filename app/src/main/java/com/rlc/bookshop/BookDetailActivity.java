package com.rlc.bookshop;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public class BookDetailActivity extends AppCompatActivity {

    private static ArrayList<Entry<String, String>> detailsSchema;
    static {
        detailsSchema = new ArrayList<Entry<String, String>>() {{
                add(new SimpleEntry<>("description", "Description"));
                add(new SimpleEntry<>("author", "Author"));
                add(new SimpleEntry<>("translator", "Translator"));
                add(new SimpleEntry<>("language", "Language"));
                add(new SimpleEntry<>("pages", "Number of pages"));
                add(new SimpleEntry<>("publisher", "Publisher"));
                add(new SimpleEntry<>("date_created", "Date added"));
        }};
    }

    public static class BookDetails {
        String title_en;
        String title_ru;
        String title_zh_cn;
        String title_zh_hk;
        String author_en;
        String author_ru;
        String author_zh_cn;
        String author_zh_hk;
        String description_en;
        String description_ru;
        String description_zh_cn;
        String description_zh_hk;
        String image;
        String language;
        String pages;
        String publisher;
        String translator;
        String date_created;

        public BookDetails() { }

        public BookDetails(String title_en, String title_ru, String title_zh_cn, String title_zh_hk,
                           String author_en, String author_ru, String author_zh_cn, String author_zh_hk,
                           String description_en, String description_ru, String description_zh_cn, String description_zh_hk,
                           String image, String language, String pages, String publisher, String translator, String date_created) {

            this.title_en = title_en;
            this.title_ru = title_ru;
            this.title_zh_cn = title_zh_cn;
            this.title_zh_hk = title_zh_hk;
            this.author_en = author_en;
            this.author_ru = author_ru;
            this.author_zh_cn = author_zh_cn;
            this.author_zh_hk = author_zh_hk;
            this.description_en = description_en;
            this.description_ru = description_ru;
            this.description_zh_cn = description_zh_cn;
            this.description_zh_hk = description_zh_hk;
            this.image = image;
            this.language = language;
            this.pages = pages;
            this.publisher = publisher;
            this.translator = translator;
            this.date_created = date_created;
        }

    }

    private ArrayAdapter adapter;
    private BookDetails item;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_detail);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(Translate.s("Orthodox Library"));

        item = new BookDetails();

        final ListView listview = (ListView) findViewById(R.id.listView);

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, detailsSchema) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setText("");
                if (Build.VERSION.SDK_INT < 23) {
                    text2.setTextAppearance(view.getContext(), android.R.style.TextAppearance_DeviceDefault_Medium);

                } else {
                    text2.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                }
                String fieldName = detailsSchema.get(position).getKey();

                try {
                    Field field = item.getClass().getField(fieldName);
                    String str = (String)field.get(item);
                    text2.setText(str);


                } catch (Exception e) {
                    try {
                        Field field = item.getClass().getField(fieldName+"_"+Translate.getLanguage());
                        String str = (String)field.get(item);
                        text2.setText(str);

                    }  catch (Exception e2) { }
                }

                text1.setText(detailsSchema.get(position).getValue());
                return view;
            }
        };

        listview.setAdapter(adapter);

        String key = this.getIntent().getExtras().getString("key");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("/details").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                item = dataSnapshot.getValue(BookDetails.class);

                Runnable run = new Runnable() {
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                };

                runOnUiThread(run);
            }

            public void onCancelled(DatabaseError firebaseError) { }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

}
