package com.fusion.kim.askproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.fusion.kim.askproject.Models.Gift;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GiftsListActivity extends AppCompatActivity {

    private RecyclerView mGiftListRv;
    private TextView mNoGiftsTv;
    private RelativeLayout mTotalGiftsCountLayout;

    private DatabaseReference mGiftListRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("personName"));

        mGiftListRv = findViewById(R.id.rv_gifts_list);
        mNoGiftsTv = findViewById(R.id.tv_no_gifts_error);
        mTotalGiftsCountLayout = findViewById(R.id.layout_gifts_items_count);

        mGiftListRv.setLayoutManager(new LinearLayoutManager(this));
        mGiftListRv.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();

        mGiftListRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"));
        mGiftListRef.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mGiftListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()){

                    mNoGiftsTv.setVisibility(View.VISIBLE);
                    mTotalGiftsCountLayout.setVisibility(View.INVISIBLE);

                } else {

                    mNoGiftsTv.setVisibility(View.GONE);
                    mTotalGiftsCountLayout.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Query query = mGiftListRef.limitToLast(50);

        FirebaseRecyclerOptions<Gift> options =
                new FirebaseRecyclerOptions.Builder<Gift>()
                        .setQuery(query, Gift.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Gift, GiftsViewHolder> adapter = new FirebaseRecyclerAdapter<Gift, GiftsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GiftsViewHolder holder, final int position, @NonNull final Gift model) {

                holder.mGiftNameTv.setText(model.getGiftName());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent giftsIntent = new Intent(GiftsListActivity.this, ViewGiftActivity.class);
                        giftsIntent.putExtra("personID", getIntent().getStringExtra("personID"));
                        giftsIntent.putExtra("personName", getIntent().getStringExtra("personName"));
                        giftsIntent.putExtra("uniqueID", "giftItem");
                        giftsIntent.putExtra("giftName", model.getGiftName());
                        giftsIntent.putExtra("giftPrice", model.getGiftPrice());
                        giftsIntent.putExtra("description", model.getDescription());
                        giftsIntent.putExtra("bought", model.isBought());
                        giftsIntent.putExtra("giftKey", getRef(position).getKey());
                        startActivity(giftsIntent);

                    }
                });

            }

            @Override
            public GiftsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new GiftsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gifts_list, parent, false));
            }
        };

        adapter.startListening();
        mGiftListRv.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gift_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_gift) {

            Intent giftsIntent = new Intent(GiftsListActivity.this, AddGiftActivity.class);
            giftsIntent.putExtra("personID", getIntent().getStringExtra("personID"));
            giftsIntent.putExtra("personName", getIntent().getStringExtra("personName"));
            startActivity(giftsIntent);

            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    private class GiftsViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mGiftNameTv;
        private ImageView mGiftIv;

        public GiftsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mGiftNameTv = itemView.findViewById(R.id.tv_gift_name);
            mGiftIv = itemView.findViewById(R.id.iv_gift_gift_icon);

        }
    }
}
