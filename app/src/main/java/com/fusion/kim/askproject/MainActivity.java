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
    private String contactID;

    private SharedPreferences mQuerySp;


    private  double mTotalCost = 0;

    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mQuerySp = getSharedPreferences("queryPreference",MODE_PRIVATE);


        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (mAuth.getCurrentUser() == null){

                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish();

                }

            }
        };

        if (FirebaseAuth.getInstance().getCurrentUser() == null){

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



    }

    @Override
    protected void onStart() {
        super.onStart();

        mTotalCost = 0;

        mAuth.addAuthStateListener(mAuthListener);

        mPeopleListRef.child("PeopleList")
                .child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChildren()){

                    mNoItemsTv.setVisibility(View.VISIBLE);
                    mItemsCoutnLayout.setVisibility(View.GONE);



                } else {

                    mNoItemsTv.setVisibility(View.GONE);
                    mItemsCoutnLayout.setVisibility(View.VISIBLE);

                    int boughtItems = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Person person = snapshot.getValue(Person.class);

                        mTotalCost += person.getTotalAmount();

                        if (person.isBought()){

                            boughtItems += 1;

                        }
                    }

                    mGeneralTotalCostTv.setText("$" + mTotalCost);
                    mGeneralBoughtTv.setText(boughtItems + "/" + (int) dataSnapshot.getChildrenCount() + " gifts bought");

                    int progress = (boughtItems / (int) dataSnapshot.getChildrenCount()) * 100;

                    if (progress > 90){

                        mBuyingProcess.setProgress(progress);
                        mBuyingProcess.setBackgroundColor(getResources().getColor(R.color.colorProgressGreen));
                    } else {
                        mBuyingProcess.setProgress(progress);
                        mBuyingProcess.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    }

                }

                mLoadingPeoplePb.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (mQuerySp.getString("sort", "").equals("name") ){

            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("personName").limitToLast(50);

        } else if (mQuerySp.getString("sort", "").equals("notBought")){

            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("bought").limitToLast(50);

        } else {

            query = mPeopleListRef.child("PeopleList")
                    .child(mAuth.getCurrentUser().getUid()).orderByChild("name").limitToLast(50);

        }


        FirebaseRecyclerOptions<Person> options =
                new FirebaseRecyclerOptions.Builder<Person>()
                        .setQuery(query, Person.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Person, PeopleViewHolder> adapter = new FirebaseRecyclerAdapter<Person, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PeopleViewHolder holder, final int position, @NonNull final Person model) {

                holder.mNameTv.setText(model.getPersonName());
                holder.mInitialTv.setText(String.valueOf(model.getPersonName().charAt(0)));


                if (model.isBought()){

                    holder.mGiftIv.setImageResource(R.drawable.gift);

                } else {

                    holder.mGiftIv.setImageResource(R.drawable.gift_grey);

                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent giftsIntent = new Intent(MainActivity.this, GiftsListActivity.class);
                        giftsIntent.putExtra("personName", model.getPersonName());
                        giftsIntent.putExtra("personID", getRef(position).getKey());
                        startActivity(giftsIntent);

                    }
                });

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

                                        if (options[which].equals(options[1])){

                                            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                            progress.setMessage("Removing...");
                                            progress.setCancelable(false);

                                            progress.show();

                                            FirebaseDatabase.getInstance().getReference().child("GiftsList").child(mAuth.getCurrentUser().getUid())
                                                    .child(getRef(position).getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        mPeopleListRef.child("PeopleList").child(mAuth.getCurrentUser().getUid())
                                                                .child(getRef(position).getKey())
                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    progress.dismiss();

                                                                    Toast.makeText(MainActivity.this, "Person Removed Successfully", Toast.LENGTH_LONG).show();

                                                                } else {

                                                                    progress.dismiss();

                                                                    Toast.makeText(MainActivity.this, "Failed to remove person. Try Again", Toast.LENGTH_LONG).show();

                                                                }

                                                            }
                                                        });

                                                    } else {

                                                        Toast.makeText(MainActivity.this, "Failed to remove person's gifts", Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            });


                                        } else if (options[which].equals(options[0])){

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

            }

            @Override
            public PeopleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new PeopleViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_people_list, parent, false));
            }
        };

        adapter.startListening();
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

                    startActivity(new Intent(MainActivity.this, AddPersonActivity.class));

                } else if (options[which].equals(options[0])){

                    // using native contacts selection
                    // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
                    startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);

                }


            }
        }).show();


    }

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

    private void retrieveContactName() {

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

        if (!TextUtils.isEmpty(contactName)){

            Intent nextIntent = new Intent(MainActivity.this, AddPersonActivity.class);
            nextIntent.putExtra("contactName", contactName);
            startActivity(nextIntent);

        } else {

            Toast.makeText(this, "Failed to capture contact name", Toast.LENGTH_SHORT).show();

        }

    }


    private class PeopleViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mNameTv, mInitialTv;
        private ImageView mGiftIv;

        public PeopleViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mNameTv = itemView.findViewById(R.id.tv_person_name);
            mGiftIv = itemView.findViewById(R.id.iv_gift_icon);
            mInitialTv = itemView.findViewById(R.id.tv_name_initial);

        }
    }

}
