package com.fusion.kim.askproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.fusion.kim.askproject.Models.Person;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //declare member variables
    private RecyclerView mPeopleRv;
    private TextView mNoItemsTv, mGeneralTotalCostTv, mGeneralBoughtTv;
    private RelativeLayout mItemsCoutnLayout;

    private ProgressBar mLoadingPeoplePb, mBuyingProcess;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mPeopleListRef;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;

    //declare a shared preference insatnce
    private SharedPreferences mQuerySp;


    private  double mTotalCost = 0, mBoughtCost = 0;

    private int mTotalItems = 0, mBoughtItems = 0;

    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTotalCost = 0;
        mBoughtCost = 0;

        mTotalItems = 0;
        mBoughtItems = 0;

        //initialize the shared preference which will store how the user prefers to sort the list of people
        mQuerySp = getSharedPreferences("queryPreference",MODE_PRIVATE);


        //initialize all the other member variables

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //check if there is a user logged in. If not then send the user to login activity
                if (mAuth.getCurrentUser() == null){

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish();

                }

            }
        };

        if (FirebaseAuth.getInstance().getCurrentUser() == null){

            //check if there is a user logged in. If not then send the user to login activity
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();

        }

        mPeopleListRef = FirebaseDatabase.getInstance().getReference();

        mPeopleRv = findViewById(R.id.rv_people_list);
        mPeopleRv.setLayoutManager(new LinearLayoutManager(this));
        mPeopleRv.setHasFixedSize(true);

        mLoadingPeoplePb = findViewById(R.id.pb_loading_people);
        mLoadingPeoplePb.setVisibility(View.VISIBLE);
        mNoItemsTv = findViewById(R.id.tv_main_error);
        mBuyingProcess = findViewById(R.id.pb_bought_progress);

        mGeneralBoughtTv = findViewById(R.id.tv_total_general_bought);
        mGeneralTotalCostTv = findViewById(R.id.tv_total_spent);

        mItemsCoutnLayout = findViewById(R.id.layout_general_items_count);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //retrieve the total price of all the gifts of all the users
        FirebaseDatabase.getInstance().getReference().child("GiftsList").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mTotalItems = 0;
                        mTotalCost = 0;
                        mBoughtCost = 0;
                        mBoughtItems = 0;



                        //loop through the datasnapshot to get deeper into the firebase root till you
                        //reach the desired node and retrieve the gift price
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                            mTotalItems += snapshot.getChildrenCount();

                            final String key = snapshot.getKey().toString();

                            FirebaseDatabase.getInstance().getReference().child("GiftsList").child(mAuth.getCurrentUser().getUid())
                                    .child(key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()){

                                        final String key2 = snapshot1.getKey().toString();

                                        FirebaseDatabase.getInstance().getReference().child("GiftsList").child(mAuth.getCurrentUser().getUid())
                                                .child(key).child(key2).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                //check if this node has children, if yes proceed to retrieve the prices
                                                if (dataSnapshot.hasChildren()){

                                                    //for each price, add it to the general toatal price
                                                    mTotalCost += dataSnapshot.child("giftPrice").getValue(Double.class);

                                                    //retrieve the bought state of the price
                                                    boolean bought = dataSnapshot.child("bought").getValue(Boolean.class);

                                                    //check if the gift is bought
                                                    if (bought){

                                                        //if bought, add the price to the total price of bought gifts
                                                        mBoughtCost += dataSnapshot.child("giftPrice").getValue(Double.class);
                                                        mBoughtItems += 1;

                                                    }

                                                    //set the price to display in the textview
                                                    mGeneralTotalCostTv.setText("$" + mBoughtCost +"/$" + mTotalCost);

                                                    //set the text to display the number of bought items
                                                    mGeneralBoughtTv.setText(mBoughtItems + "/" + mTotalItems + " gifts bought");

                                                    //calculate the progress of bought items
                                                    double progress = calculateProgress(mBoughtItems, mTotalItems);

                                                    if (progress == 0){

                                                        mBuyingProcess.setProgress((int) progress);
                                                        mBuyingProcess.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_background));

                                                    } else if (progress > 99){

                                                        mBuyingProcess.setProgress((int) progress);
                                                        mBuyingProcess.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal));

                                                    } else {
                                                        mBuyingProcess.setProgress((int) progress);
                                                        mBuyingProcess.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progress_bar_horizontal_red));

                                                    }

                                                    Log.e("Retrieved String", dataSnapshot.child("giftPrice").getValue(Double.class).toString());

                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

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
        //Set all counts to zero at first

        //check if there any people
        mPeopleListRef.child("PeopleList")
                .child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.hasChildren()){

                            //if there are no people then display a message saying so
                            mNoItemsTv.setVisibility(View.VISIBLE);
                            //hide the bar at the bottom that shows total price and progress
                            mItemsCoutnLayout.setVisibility(View.GONE);
                            mLoadingPeoplePb.setVisibility(View.GONE);

                        } else {

                            //if there are people then hide the message
                            mNoItemsTv.setVisibility(View.GONE);
                            //display the bar at the bottom that shows total price and progress
                            mItemsCoutnLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //add listener to listen for authentication changes within the app i.e user logout
        mAuth.addAuthStateListener(mAuthListener);


        //retrieve the preference of sorting the list
        if (mQuerySp.getString("sort", "").equals("name") ){

            //sort by name
            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("personName").limitToLast(50);

        } else if (mQuerySp.getString("sort", "").equals("notBought")){

            //sort by not bought items
            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("bought").limitToLast(50);

        } else {

            //default sorting
            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("name").limitToLast(50);

        }


        //recycler options for the adapter
        FirebaseRecyclerOptions<Person> options =
                new FirebaseRecyclerOptions.Builder<Person>()
                        .setQuery(query, Person.class)
                        .setLifecycleOwner(this)
                        .build();

        //adapter for the recyclerview
        FirebaseRecyclerAdapter<Person, PeopleViewHolder> adapter = new FirebaseRecyclerAdapter<Person, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final PeopleViewHolder holder, final int position, @NonNull final Person model) {

                //set the data to display
                holder.mNameTv.setText(model.getPersonName());
                holder.mInitialTv.setText(String.valueOf(model.getPersonName().charAt(0)));
                holder.mExpiryTv.setText("Deadline: " + model.getDeadline());


                FirebaseDatabase.getInstance().getReference().child("GiftsList").child(mAuth.getCurrentUser().getUid())
                        .child(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChildren()){

                            int mBoughtItems = 0, mTotalItems;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){


                                mTotalItems = (int) dataSnapshot.getChildrenCount();

                                if (snapshot.child("bought").getValue(Boolean.class) == true){

                                    mBoughtItems = mBoughtItems + 1;

                                }

                                if (mBoughtItems == mTotalItems ){

                                    holder.mGiftIv.setImageResource(R.drawable.gift);


                                } else if (mBoughtItems < mTotalItems){

                                    holder.mGiftIv.setImageResource(R.drawable.gift_grey);

                                }

                            }

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                //click listener to open the list of gifts associated with the person
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent giftsIntent = new Intent(MainActivity.this, GiftsListActivity.class);
                        giftsIntent.putExtra("personName", model.getPersonName());
                        giftsIntent.putExtra("personID", getRef(position).getKey());
                        startActivity(giftsIntent);

                    }
                });

                //long click listener to pop up a dialog of options
                holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        final String [] options = {"Edit", "Delete"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        // Get the layout inflater
                        builder.setTitle(model.getPersonName())
                                .setItems(options, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // The 'which' argument contains the index position
                                        // of the selected item

                                        //check which item was clicked

                                        if (options[which].equals(options[1])){


                                            //item at pos 1 == delete

                                            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                            progress.setMessage("Removing...");
                                            progress.setCancelable(false);

                                            progress.show();

                                            try {

                                                //remove the gifts of the person from firebase
                                                FirebaseDatabase.getInstance().getReference().child("GiftsList").child(mAuth.getCurrentUser().getUid())
                                                        .child(getRef(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            //if removal was successful then remove the person
                                                            mTotalItems = 0;
                                                            mTotalCost = 0;
                                                            mBoughtCost = 0;
                                                            mBoughtItems = 0;

                                                            mPeopleListRef.child("PeopleList").child(mAuth.getCurrentUser().getUid())
                                                                    .child(getRef(position).getKey())
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){

                                                                        //removal was a success
                                                                        progress.dismiss();

                                                                        mTotalItems = 0;
                                                                        mTotalCost = 0;
                                                                        mBoughtCost = 0;
                                                                        mBoughtItems = 0;


                                                                        Toast.makeText(MainActivity.this, "Person Removed Successfully", Toast.LENGTH_LONG).show();

                                                                    } else {

                                                                        //removal failed
                                                                        progress.dismiss();

                                                                        mTotalItems = 0;
                                                                        mTotalCost = 0;
                                                                        mBoughtCost = 0;
                                                                        mBoughtItems = 0;


                                                                        Toast.makeText(MainActivity.this, "Failed to remove person. Try Again", Toast.LENGTH_LONG).show();

                                                                    }

                                                                }
                                                            });

                                                        } else {
                                                            progress.dismiss();

                                                            Toast.makeText(MainActivity.this, "Failed to remove person's gifts", Toast.LENGTH_LONG).show();

                                                        }

                                                    }
                                                });

                                            } catch (Exception e){
                                                progress.dismiss();
                                                Toast.makeText(MainActivity.this, "Failed to delete person. Try again.", Toast.LENGTH_LONG).show();
                                            }


                                        } else if (options[which].equals(options[0])){

                                            //item at pos 0 == edit
                                            //send the user to edit the person

                                            Intent editIntent = new Intent(MainActivity.this, EditPersonActivity.class);
                                            editIntent.putExtra("personName", model.getPersonName());
                                            editIntent.putExtra("personID", getRef(position).getKey());
                                            editIntent.putExtra("deadline", model.getDeadline());
                                            startActivity(editIntent);

                                        }


                                    }
                                }).show();

                        return true;
                    }
                });

                mLoadingPeoplePb.setVisibility(View.GONE);

            }

            @Override
            public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new PeopleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_people_list, parent, false));
            }
        };

        //activate the adapter
        adapter.startListening();
        //set the adapter to the recyclerview
        mPeopleRv.setAdapter(adapter);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_add_person) {

            showAddPersonDialog();

            return true;
        }

        if (id == R.id.action_sort_list) {

            showShortDialog();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            mAuth.signOut();

        }

        if (id == R.id.nav_about) {

            startActivity(new Intent(MainActivity.this, AboutActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //method to pop up a dialog for choosing how to add a person
    private void showAddPersonDialog(){

        final String [] options = {"From Contacts", "Add Manually"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater
        builder.setTitle("Add a person to your list")
                .setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item

                if (options[which].equals(options[1])){

                    //pos 1 == add manually
                    startActivity(new Intent(MainActivity.this, AddPersonActivity.class));

                } else if (options[which].equals(options[0])){
                    //pos 0 == pick from contacts

                    // using native contacts selection
                    // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);

                }


            }
        }).show();


    }

    //method to pop up dialog to pick how to sort the list
    private void showShortDialog(){

        final String [] options = {"Name", "Not Bought"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Get the layout inflater
        builder.setTitle("Order")
                .setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        if (options[which].equals(options[0])){

                            mQuerySp.edit().putString("sort", "name").apply();
                            finish();
                            startActivity(getIntent());

                        } else if (options[which].equals(options[1])){

                            mQuerySp.edit().putString("sort", "notBought").apply();
                            finish();
                            startActivity(getIntent());

                        }


                    }
                }).show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();

        }
    }

    //method to send the user to contacts list to pick a person
    private void retrieveContactName() {

        //declare and initialize to null variable to hold the name of the picked person
        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d(TAG, "Contact Name: " + contactName);

        //check if the name was picked successfully
        if (!TextUtils.isEmpty(contactName)){

            //send the user to save pick deadline and save the person
            Intent nextIntent = new Intent(MainActivity.this, AddPersonActivity.class);
            nextIntent.putExtra("contactName", contactName);
            startActivity(nextIntent);

        } else {

            Toast.makeText(this, "Failed to capture contact name", Toast.LENGTH_SHORT).show();

        }

    }


    //view holder to link the recylcerview with the views to be displayed in a row
    private class PeopleViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mNameTv, mInitialTv, mExpiryTv;
        private ImageView mGiftIv;

        public PeopleViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mNameTv = itemView.findViewById(R.id.tv_person_name);
            mGiftIv = itemView.findViewById(R.id.iv_gift_icon);
            mInitialTv = itemView.findViewById(R.id.tv_name_initial);
            mExpiryTv = itemView.findViewById(R.id.tv_expiry);

        }
    }

    public static double calculateProgress(int boughtItems, int totalItems){

        double progress = ((double) boughtItems / (double) totalItems) * 100;

        return progress;

    }

}
