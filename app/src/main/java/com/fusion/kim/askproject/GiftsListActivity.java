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

    //declare member variables
    private RecyclerView mGiftListRv;
    private TextView mNoGiftsTv, mTotalCostTv, mTotalBoughtTv, mPersonGiftNameTv;
    private RelativeLayout mTotalGiftsCountLayout;

    private DatabaseReference mGiftListRef;
    private FirebaseAuth mAuth;

    private ProgressBar mGiftsProgress;

    private double mTotalCostBought = 0, mTotalPrice = 0;
    private int mBoughtItems = 0, mTotalCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gifts_list);

        //set up the back button on action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //set the title of the action bar to be the name of the person
        getSupportActionBar().setTitle(getIntent().getStringExtra("personName"));

        mTotalCostBought = 0;
        mTotalPrice = 0;
        mBoughtItems = 0;
        mTotalCount = 0;

        //initialize the member variables
        mGiftListRv = findViewById(R.id.rv_gifts_list);
        mNoGiftsTv = findViewById(R.id.tv_no_gifts_error);
        mTotalGiftsCountLayout = findViewById(R.id.layout_gifts_items_count);
        mTotalCostTv = findViewById(R.id.tv_total_gifts_spent);
        mTotalBoughtTv = findViewById(R.id.tv_total_gifts_bought);
        mPersonGiftNameTv = findViewById(R.id.tv_gift_person_name);

        mPersonGiftNameTv.setText(getIntent().getStringExtra("personName"));

        mGiftsProgress = findViewById(R.id.pb_gifts_bought_progress);

        mGiftListRv.setLayoutManager(new LinearLayoutManager(this));
        mGiftListRv.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();

        mGiftListRef = FirebaseDatabase.getInstance().getReference().child("GiftsList")
                .child(mAuth.getCurrentUser().getUid()).child(getIntent().getStringExtra("personID"));
        mGiftListRef.keepSynced(true);

        //check whether there are any gifts associated with the person
        mGiftListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()){

                    //if there are no gifts, display a message saying so and hide the bar at the
                    //bottom, showing total cost and buying progress bar
                    mNoGiftsTv.setVisibility(View.VISIBLE);
                    mTotalGiftsCountLayout.setVisibility(View.INVISIBLE);

                } else {

                    //if there are gifts, count the total gifts
                    mTotalPrice = 0;
                    mTotalCostBought = 0;

                    mTotalCount = (int) dataSnapshot.getChildrenCount();

                    mBoughtItems = 0;

                    //for each gift, retrieve it's price
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Gift gift = snapshot.getValue(Gift.class);


                        mTotalPrice += gift.getGiftPrice();

                        //if the gift is bought
                        if (gift.isBought()){

                            //add the gift'd price to the bought gifts total cost
                            mTotalCostBought += gift.getGiftPrice();
                            //increment the number of bought gifts
                            mBoughtItems += 1;

                        }
                    }

                    //hide the no gifts message
                    mNoGiftsTv.setVisibility(View.GONE);
                    //show the bar at the bottom showing the buying progress and total cost
                    mTotalGiftsCountLayout.setVisibility(View.VISIBLE);
                    mTotalBoughtTv.setText("Bought: " + mBoughtItems + "/" + mTotalCount);

                    mTotalCostTv.setText("$" + mTotalCostBought + "/$" + mTotalPrice );
                    mTotalBoughtTv.setText(mBoughtItems + "/" + mTotalCount + " gifts bought");

                    //calculate the buying progress
                    double progress = ((double) mBoughtItems / (double) (int) dataSnapshot.getChildrenCount()) * 100;

                    if (progress == 0){

                        mGiftsProgress.setProgress((int) progress);
                        mGiftsProgress.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_background));

                    } else if (progress > 99){

                        mGiftsProgress.setProgress((int) progress);
                        mGiftsProgress.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal));

                    } else {
                        mGiftsProgress.setProgress((int) progress);
                        mGiftsProgress.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_red));

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //populate the gifts list
        populateGiftsList();

    }

    //method to load data from firebase and populate the gifts list
    private void populateGiftsList(){

        //set up the query to request data from the firebase reference specified
        Query query = mGiftListRef.limitToLast(50);

        FirebaseRecyclerOptions<Gift> options =
                new FirebaseRecyclerOptions.Builder<Gift>()
                        .setQuery(query, Gift.class)
                        .setLifecycleOwner(this)
                        .build();

        //set up the adapter to be used in the recyclerview to display the list of gifts
        FirebaseRecyclerAdapter<Gift, GiftsViewHolder> adapter = new FirebaseRecyclerAdapter<Gift, GiftsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull GiftsViewHolder holder, final int position, @NonNull final Gift model) {

                //set up data to display
                holder.mGiftNameTv.setText(model.getGiftName());
                holder.mGiftPriceTv.setText("$" + model.getGiftPrice());

                if (model.isBought()){

                    holder.mGiftIv.setImageResource(R.drawable.gift);

                } else {

                    holder.mGiftIv.setImageResource(R.drawable.gift_grey);

                }

                //add click listener to the gift row and send the user to view/edit the gift
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try{

                            Intent giftsIntent = new Intent(GiftsListActivity.this, ViewGiftActivity.class);
                            //add data to send to the next activity

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

                        } catch (IndexOutOfBoundsException e){

                        }



                    }
                });


                // add long click listener to a gift row
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        //create an alert dialog builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(GiftsListActivity.this);
                        // Add a message to the alert dialog
                        builder.setMessage("Are you sure you want to delete this gift?");
                        //set the title of the alert dailog to be the name of the gift
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

                                        //set up a progress bar
                                        final ProgressDialog progress = new ProgressDialog(GiftsListActivity.this);
                                        progress.setMessage("Removing...");
                                        progress.setCancelable(false);

                                        progress.show();

                                        try {

                                            //remove the gift from  firebase
                                            mGiftListRef.child(getRef(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        //if the removal was successful
                                                        progress.dismiss();

                                                        Toast.makeText(GiftsListActivity.this, "Gift Deleted", Toast.LENGTH_LONG).show();

                                                    } else {

                                                        //the removal was unsuccessful
                                                        progress.dismiss();

                                                        Toast.makeText(GiftsListActivity.this, "Failed to Delete Gift. Try Again", Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            });

                                        } catch (Exception e){
                                            progress.dismiss();
                                            Toast.makeText(GiftsListActivity.this, "Failed to delete gift. Try Again.", Toast.LENGTH_LONG).show();
                                        }


                                    }
                                })
                                .show();

                        return true;
                    }
                });

            }

            //inflate the viewholder of the adapter
            @Override
            public GiftsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new GiftsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_gifts_list, parent, false));
            }
        };

        //activate the adapter to start listening for event changes in firebase
        adapter.startListening();
        //set the adapter for the recycler view
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

            //start activity to add a new gift
            Intent giftsIntent = new Intent(GiftsListActivity.this, AddGiftActivity.class);
            giftsIntent.putExtra("personID", getIntent().getStringExtra("personID"));
            giftsIntent.putExtra("personName", getIntent().getStringExtra("personName"));
            startActivity(giftsIntent);

            return true;
        }



        return super.onOptionsItemSelected(item);
    }

    // A view holder to set up a row of the gifts list of the recycler view
    private class GiftsViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mGiftNameTv, mGiftPriceTv;
        private ImageView mGiftIv;

        public GiftsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mGiftNameTv = itemView.findViewById(R.id.tv_gift_name);
            mGiftPriceTv = itemView.findViewById(R.id.tv_gift_price);
            mGiftIv = itemView.findViewById(R.id.iv_gift_gift_icon);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent mainIntent = new Intent(GiftsListActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();

    }
}
