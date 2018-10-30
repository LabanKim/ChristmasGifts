package com.fusion.kim.askproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.fusion.kim.askproject.Models.Gift;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GiftsListActivity extends AppCompatActivity {

    private RecyclerView mGiftListRv;
    private TextView mNoGiftsTv, mTotalCostTv, mTotalBoughtTv;
    private RelativeLayout mTotalGiftsCountLayout;

    private DatabaseReference mGiftListRef;
    private FirebaseAuth mAuth;

    private ProgressBar mGiftsProgress;

    private double mTotalCost = 0;

    private String personID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("personName"));

        personID = getIntent().getStringExtra("personID");

        mGiftListRv = findViewById(R.id.rv_gifts_list);
        mNoGiftsTv = findViewById(R.id.tv_no_gifts_error);
        mTotalGiftsCountLayout = findViewById(R.id.layout_gifts_items_count);
        mTotalCostTv = findViewById(R.id.tv_total_gifts_spent);
        mTotalBoughtTv = findViewById(R.id.tv_total_gifts_bought);

        mGiftsProgress = findViewById(R.id.pb_gifts_bought_progress);

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

                    int totalCount = (int) dataSnapshot.getChildrenCount();

                    int boughtItems = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gift gift = snapshot.getValue(Gift.class);

                        if (gift.isBought()){

                            mTotalCost += gift.getGiftPrice();
                            boughtItems += 1;

                        }
                    }

                    mNoGiftsTv.setVisibility(View.GONE);
                    mTotalGiftsCountLayout.setVisibility(View.VISIBLE);
                    mTotalBoughtTv.setText("Bought: " + boughtItems + "/" + totalCount);
                    mTotalCostTv.setText("Total Cost: "+ mTotalCost + "$");

                    mTotalCostTv.setText("$" + mTotalCost);
                    mTotalBoughtTv.setText(boughtItems + "/" + totalCount + " gifts bought");

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

                if (model.isBought()){

                    holder.mGiftIv.setImageResource(R.drawable.gift);

                } else {

                    holder.mGiftIv.setImageResource(R.drawable.gift_grey);

                }

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
                        giftsIntent.putExtra("imageOne", model.getImageOne());
                        giftsIntent.putExtra("imageTwo", model.getImageTwo());
                        giftsIntent.putExtra("imageThree", model.getImageThree());
                        startActivityForResult(giftsIntent, 1);

                    }
                });


                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(GiftsListActivity.this);
                        // Get the layout inflater
                        builder.setMessage("Are you sure you want to delete this gift?");
                        builder.setTitle(model.getGiftName())
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        final ProgressDialog progress = new ProgressDialog(GiftsListActivity.this);
                                        progress.setMessage("Removing...");
                                        progress.setCancelable(false);

                                        progress.show();

                                        mGiftListRef.child(getRef(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    progress.dismiss();

                                                    Toast.makeText(GiftsListActivity.this, "Gift Deleted", Toast.LENGTH_LONG).show();

                                                } else {

                                                    progress.dismiss();

                                                    Toast.makeText(GiftsListActivity.this, "Failed to Delete Gift. Try Again", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });

                                    }
                                })
                                .show();

                        return true;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(GiftsListActivity.this, MainActivity.class));
        finish();

    }

}
