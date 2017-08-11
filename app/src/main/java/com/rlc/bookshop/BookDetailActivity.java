package com.rlc.bookshop;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import com.folioreader.activity.FolioActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
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
        public String title_en;
        public String title_ru;
        public String title_zh_cn;
        public String title_zh_hk;
        public String author_en;
        public String author_ru;
        public String author_zh_cn;
        public String author_zh_hk;
        public String description_en;
        public String description_ru;
        public String description_zh_cn;
        public String description_zh_hk;
        public String image;
        public String language;
        public String pages;
        public String publisher;
        public String translator;
        public String date_created;

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
    ProgressDialog mProgressDialog;
    Timer myTimer;

    private DownloadManager downloadManager;
    private long downloadReference;

    IntentFilter filter;

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

                text1.setText(Translate.s(detailsSchema.get(position).getValue()));
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

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(downloadReceiver);
    }

    private void previewFile(String filename) {
        String ext  =  filename.substring(filename.lastIndexOf('.') + 1);
        Uri uri = Uri.parse(filename);
        Intent intent;

        if (ext.equals("pdf") || ext.equals("PDF")) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");

        } else {
            intent = new Intent(BookDetailActivity.this, FolioActivity.class);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_TYPE, FolioActivity.EpubSourceType.SD_CARD);
            intent.putExtra(FolioActivity.INTENT_EPUB_SOURCE_PATH, uri.getPath());
            startActivity(intent);

        }

        startActivity(intent);
    }

    private void startDownload(String url) {
        if (downloadReference != 0) {
            Toast toast = Toast.makeText(BookDetailActivity.this,
                    "Download in progress", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
            return;
        }

        Uri Download_Uri = Uri.parse(url);
        File f = new File(""+Download_Uri);

        String filename = f.getName();
        String localFile = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+ "/"+filename;
        File downloadedFile =  new File(localFile);

        if (downloadedFile != null && downloadedFile.exists()) {
            previewFile(downloadedFile.toURI().toString());
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        request.setTitle("Downloading");
        request.setDescription(bookTitle);

        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, filename);

        downloadReference = downloadManager.enqueue(request);

        mProgressDialog = new ProgressDialog(BookDetailActivity.this);
        mProgressDialog.setMessage(Translate.s("Downloading") + " " + filename);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // downloadTask.cancel(true);
            }
        });

        myTimer = new Timer();
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
                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setMax(100);
                        mProgressDialog.setProgress((int)dl_progress);
                    }
                });

            }

        }, 0, 10);

        mProgressDialog.show();

    }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.pdfButton:
                startDownload(download_url);
                break;

            case R.id.epubButton:
                startDownload(epub_url);
                break;
        }
    }


    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == referenceId) {
                myTimer.cancel();
                mProgressDialog.dismiss();

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(referenceId);
                Cursor c = downloadManager.query(query);

                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String filename = c
                                .getString(c
                                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                        previewFile(filename);

                    } else {
                        Toast toast = Toast.makeText(BookDetailActivity.this,
                                "Unable to download file", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 25, 400);
                        toast.show();
                    }
                }


            }

            downloadReference = 0;
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
