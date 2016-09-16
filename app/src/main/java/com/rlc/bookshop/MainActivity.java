package com.rlc.bookshop;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {

    public static class BookHolder extends RecyclerView.ViewHolder {
        View mView;

        public BookHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView field = (TextView) mView.findViewById(R.id.title);
            field.setText(title);
        }

        public void setImage(String imageUrl) {
            ImageView image = (ImageView)mView.findViewById(R.id.cover);
            Picasso.with(mView.getContext()).load(imageUrl).into(image);
        }

    }

    public static class BookData  {
        String title_en;
        String title_ru;
        String title_zh_cn;
        String title_zh_hk;
        String image;
        String download_url;
        String epub_url;
        String date_created;

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
        public String getTitle() { return title_en; }

    }

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Translate.setLanguage("en");

        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("/index");

        RecyclerView recycler = (RecyclerView) findViewById(R.id.rv);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recycler.setLayoutManager(layoutManager);

        FirebaseRecyclerAdapter mAdapter = new FirebaseRecyclerAdapter<BookData, BookHolder>(BookData.class, R.layout.list_item, BookHolder.class, ref.orderByChild("date_created"))  {
            @Override
            public void populateViewHolder(BookHolder viewHolder, BookData book, int position) {
                viewHolder.setTitle(book.getTitle());
                viewHolder.setImage(book.getImage());
            }
        };

        recycler.setAdapter(mAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /*
        ref.orderByChild("date_created").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot book: snapshot.getChildren()) {
                    BookData item = book.getValue(BookData.class);
                    Log.i("Bookshop", item.getDownloadUrl());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("Bookshop", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
       */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_toggle) {

        } else if (id == R.id.action_options) {
            OptionsDialog dialog = new OptionsDialog(MainActivity.this, getPreferences(0));
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
