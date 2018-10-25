package com.fusion.kim.askproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.fusion.kim.askproject.Models.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView mPeopleRv;
    private TextView mErrorTv;

    private ProgressBar mLoadingPeoplePb;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mPeopleListRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        if (mAuth.getCurrentUser() == null){

            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();

        }

        mPeopleListRef = FirebaseDatabase.getInstance().getReference().child("PeopleList")
                .child(mAuth.getCurrentUser().getUid());
        mPeopleListRef.keepSynced(true);

        mPeopleRv = findViewById(R.id.rv_people_list);
        mPeopleRv.setLayoutManager(new LinearLayoutManager(this));
        mPeopleRv.setHasFixedSize(true);

        mLoadingPeoplePb = findViewById(R.id.pb_loading_people);
        mErrorTv = findViewById(R.id.tv_main_error);

        NavigationView navView = findViewById(R.id.nav_view);
        View headerView = navView.inflateHeaderView(R.layout.nav_header_main);
        ImageView profileImageIv = headerView.findViewById(R.id.iv_profile_pic);
        TextView userNameTv = headerView.findViewById(R.id.tv_nav_user_name);
        TextView userEmailTv = headerView.findViewById(R.id.tv_user_email);
        TextView appNameTv = headerView.findViewById(R.id.tv_project_name);
        TextView phoneNumber = headerView.findViewById(R.id.tv_nav_phone_number);
        userNameTv.setText("Name");
        appNameTv.setText("Christmas Gift List");


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

        mAuth.addAuthStateListener(mAuthListener);

        Query query = mPeopleListRef.limitToLast(50);

        FirebaseRecyclerOptions<Person> options =
                new FirebaseRecyclerOptions.Builder<Person>()
                        .setQuery(query, Person.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter<Person, PeopleViewHolder> adapter = new FirebaseRecyclerAdapter<Person, PeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PeopleViewHolder holder, int position, @NonNull Person model) {

                holder.mNameTv.setText(model.getPersonName());

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
        if (id == R.id.action_logout) {

            mAuth.signOut();

            return true;
        }

        if (id == R.id.action_add_person) {

            showAddPersonDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_poultry) {
            // Handle the camera action
        } else if (id == R.id.nav_fish) {

        } else if (id == R.id.nav_dairy) {

        } else if (id == R.id.nav_horticulture) {

        } else if (id == R.id.nav_upload_product) {


        } else if (id == R.id.nav_view_my_product){


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

                }


            }
        }).show();


    }

    private class PeopleViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView mNameTv;
        private ImageView mGiftIv;

        public PeopleViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mNameTv = itemView.findViewById(R.id.tv_person_name);
            mGiftIv = itemView.findViewById(R.id.iv_gift_icon);

        }
    }

}
