package com.rlc.bookshop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Keep;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;
import java.util.Date;

import jp.co.recruit_mp.android.rmp_appirater.RmpAppirater;


public class MainActivity extends AppCompatActivity {

    public static class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View mView;
        String key;
        String title;
        String imageUrl;
        String download_url;
        String epub_url;

        public BookHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mView.setOnClickListener(this);
        }

        public void setTitle(String title) {
            this.title = title;
            TextView field = (TextView) mView.findViewById(R.id.title);
            field.setText(title);
        }

        public void setImage(String imageUrl) {
            this.imageUrl = imageUrl;
            ImageView image = (ImageView)mView.findViewById(R.id.cover);
            Picasso.with(mView.getContext()).load(imageUrl).into(image);
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public void onClick(View v) {
            Log.i("Bookshop", key);

            Intent detailIntent = new Intent(mView.getContext(), BookDetailActivity.class);
            detailIntent.putExtra("key", key);
            detailIntent.putExtra("title", title);
            detailIntent.putExtra("imageUrl", imageUrl);
            detailIntent.putExtra("download_url", download_url);
            detailIntent.putExtra("epub_url", epub_url);

            mView.getContext().startActivity(detailIntent);
        }
    }

    public static class BookData  {
        public String title_en;
        public String title_ru;
        public String title_zh_cn;
        public String title_zh_hk;
        public String image;
        public String download_url;
        public String epub_url;
        public String date_created;

        public BookData() { }

        public BookData(String title_en, String title_ru, String title_zh_cn, String title_zh_hk, String image, String download_url, String epub_url, String date_created) {
            this.title_en = title_en;
            this.title_ru = title_ru;
            this.title_zh_cn = title_zh_cn;
            this.title_zh_hk = title_zh_hk;
            this.image = image;
            this.download_url = download_url;
            this.epub_url = epub_url;
            this.date_created = date_created;
        }

        public String getDownloadUrl() {
            return download_url;
        }
        public String getEpubUrl() { return epub_url; }
        public String getImage() { return image; }
        public String getTitle() {
            try {
                Field field = this.getClass().getField("title_"+Translate.getLanguage());
                String str = (String)field.get(this);
                return str;

            }  catch (Exception e2) { }
            return title_en;
        }

    }

    private Toolbar toolbar;
    FirebaseRecyclerAdapter mAdapter;
    SharedPreferences prefs;
    String viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getPreferences(0);

        if (prefs.getString("language", "").equals("")) {
            Translate.setLanguage("en");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("language", "en");
            editor.putString("view", "list");
            editor.commit();

            OptionsDialog dialog = new OptionsDialog(MainActivity.this, getPreferences(0));
            dialog.show();
        }

        viewType = prefs.getString("view", "list");
        Translate.setLanguage(prefs.getString("language", "en"));

        RmpAppirater.appLaunched(this,
                new RmpAppirater.ShowRateDialogCondition() {
                    @Override
                    public boolean isShowRateDialog(
                            long appLaunchCount, long appThisVersionCodeLaunchCount,
                            long firstLaunchDate, int appVersionCode,
                            int previousAppVersionCode, Date rateClickDate,
                            Date reminderClickDate, boolean doNotShowAgain) {

                        return (rateClickDate == null && !doNotShowAgain && appLaunchCount >= 3);
                    }
                },
                new RmpAppirater.Options(
                        Translate.s("Orthodox Library"),
                        Translate.s("If you enjoy using Orthodox Library, would you mind taking a moment to rate it? It won\'t take more than a minute. Thanks for your support!"),
                        Translate.s("Rate Orthodox Library"),
                        Translate.s("Remind me later"),
                        Translate.s("No, thanks")));

        setContentView(R.layout.activity_main);
        setTitle(Translate.s("Orthodox Library"));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/index");

        RecyclerView recycler = (RecyclerView) findViewById(R.id.rv);

        mAdapter = new FirebaseRecyclerAdapter<BookData, BookHolder>(BookData.class,
                viewType.equals("list") ? R.layout.list_item : R.layout.grid_item,
                BookHolder.class,
                ref.orderByChild("date_created"))  {

            @Override
            public void populateViewHolder(BookHolder viewHolder, BookData book, int position) {
                viewHolder.setTitle(book.getTitle());
                viewHolder.setImage(book.getImage());

                String key = this.getRef(position).getKey();
                viewHolder.key = key;
                viewHolder.download_url = book.getDownloadUrl();
                viewHolder.epub_url = book.getEpubUrl();
            }
        };

        recycler.setAdapter(mAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menu.getItem(0).setIcon(ContextCompat.getDrawable(this,
                viewType.equals("grid") ? R.drawable.ic_action_list : R.drawable.ic_action_grid));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("view", viewType.equals("list") ? "grid" : "list");
            editor.commit();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            this.finish();

        } else if (id == R.id.action_options) {
            OptionsDialog dialog = new OptionsDialog(MainActivity.this, getPreferences(0));
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateView() {
        Runnable run = new Runnable() {
            public void run() {
                setTitle(Translate.s("Orthodox Library"));
                mAdapter.notifyDataSetChanged();
            }
        };

        runOnUiThread(run);

    }


}
