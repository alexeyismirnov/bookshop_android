package com.rlc.bookshop;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.Timer;
import java.util.TimerTask;


public class BookDetailActivity extends AppCompatActivity implements View.OnClickListener {

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
    private String download_url;
    private String epub_url;
    String bookTitle;
    ProgressBar mProgressBar;

    private DownloadManager downloadManager;
    private long downloadReference;

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

        TextView titleView = (TextView)findViewById(R.id.bookTitle);
        bookTitle = this.getIntent().getExtras().getString("title");
        titleView.setText(bookTitle);

        ImageView image = (ImageView)findViewById(R.id.bookImage);
        String imageUrl = this.getIntent().getExtras().getString("imageUrl");
        Picasso.with(this).load(imageUrl).into(image);

        download_url = this.getIntent().getExtras().getString("download_url");
        epub_url = this.getIntent().getExtras().getString("epub_url");

        ImageButton epubButton = (ImageButton)findViewById(R.id.epubButton);
        ImageButton pdfButton = (ImageButton)findViewById(R.id.pdfButton);

        if (epub_url == null || epub_url.trim().isEmpty()) {
            epubButton.setVisibility(View.INVISIBLE);
        }

        pdfButton.setOnClickListener(this);
        epubButton.setOnClickListener(this);

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


        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.pdfButton:
                downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                Uri Download_Uri = Uri.parse(download_url);
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                request.setTitle("Downloading");
                request.setDescription(bookTitle);

                request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"book.pdf");

                downloadReference = downloadManager.enqueue(request);

                mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
                mProgressBar.setVisibility(View.VISIBLE);

                Timer myTimer = new Timer();
                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(downloadReference);
                        Cursor cursor = downloadManager.query(q);
                        cursor.moveToFirst();
                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        cursor.close();
                        final int dl_progress = (bytes_downloaded * 100 / bytes_total);
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                mProgressBar.setProgress(dl_progress);
                            }
                        });

                    }

                }, 0, 10);

                break;
            case R.id.epubButton:
                break;
        }
    }


    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == referenceId) {
                Toast toast = Toast.makeText(BookDetailActivity.this,
                        "Downloading of data just finished", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 25, 400);
                toast.show();

                mProgressBar.setVisibility(View.INVISIBLE);
                
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(referenceId);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c
                            .getInt(columnIndex)) {

                        String uriString = c
                                .getString(c
                                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Uri uri = Uri.parse(uriString);

                        intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "application/pdf");

                        // callback.onSuccess();

                        context.startActivity(intent);

                    }
                    else{
                        // callback.onError();
                    }
                }


            }
        }
    };

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
