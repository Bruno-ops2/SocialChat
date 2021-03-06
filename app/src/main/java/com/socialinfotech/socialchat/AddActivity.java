package com.socialinfotech.socialchat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewAnimator;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socialinfotech.socialchat.adapter.UserViewHolder;
import com.socialinfotech.socialchat.domain.AddUserData;
import com.socialinfotech.socialchat.domain.User;
import com.socialinfotech.socialchat.domain.chat.conversation;
import com.socialinfotech.socialchat.domain.util.LibraryClass;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddActivity extends AppCompatActivity {


    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.animation_icon)
    SimpleDraweeView animationIcon;
    @Bind(R.id.rv_users)
    RecyclerView rvUsers;
    @Bind(R.id.viewAnimator)
    ViewAnimator viewAnimator;
    private Firebase firebase;
    private UserRecyclerAdapter adapter;
    private User own_user;
    private List<AddUserData> addUserDatas = new ArrayList<AddUserData>();
    private UserAdapter userAdapter;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(AddActivity.this);
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(AddActivity.this, R.anim.tween);
        animationIcon.startAnimation(myFadeInAnimation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {

            firebase = LibraryClass.getFirebase().child("users");
            firebase = firebase.child(firebase.getAuth().getUid());
        } catch (Exception e) {
            Intent intent = new Intent(AddActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot == null)) {
                    Log.e("s", dataSnapshot.toString());
                    own_user = dataSnapshot.getValue(User.class);
                    init();

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("s", "error==");
            }
        });


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7100319741895489~8608453659");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        requestNewInterstitial();

    }

    private void requestNewInterstitial() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7100319741895489/9526783655");
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                openAd();
            }
        });
    }



    private void openAd() {
        //ads load
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void init() {
        rvUsers.setHasFixedSize(true);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        Log.e("adaoter", "in");
        userAdapter = new UserAdapter();
        rvUsers.setAdapter(userAdapter);
        if (getArray().size() > 0) {
            userAdapter.addData(getArray());
            viewAnimator.setDisplayedChild(2);
        }
        final Firebase firebase = LibraryClass.getFirebase().child("users");
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("adaoter", "ch");

                if (!(dataSnapshot.getValue() == null)) {
                    addUserDatas = new ArrayList<AddUserData>();
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        AddUserData addUserData = d.getValue(AddUserData.class);
                        addUserData.setUid(d.getKey());
                        if (!addUserData.getUid().equals(firebase.getAuth().getUid())) {
                            addUserDatas.add(addUserData);
                        }

                    }
                    viewAnimator.setDisplayedChild(2);
                    if (getArray().size() == 0) {
                        userAdapter.addData(addUserDatas);
                    }
                    saveArray(addUserDatas);


                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("adaoter", "error");
                viewAnimator.setDisplayedChild(1);
            }
        });


    }




    private void saveArray(List<AddUserData> addUserDatas) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sp.edit();
        Gson gson = new Gson();
        String jsonText = gson.toJson(addUserDatas);
        prefsEditor.putString("key", jsonText);
        prefsEditor.commit();
    }

    private List<AddUserData> getArray() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        String jsonText = sp.getString("key", null);
        List<AddUserData> httpParamList =
                new Gson().fromJson(jsonText, new TypeToken<List<AddUserData>>() {
                }.getType());
        if (httpParamList == null) {
            return new ArrayList<AddUserData>();
        } else {
            return httpParamList;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

        private List<AddUserData> moviesList = new ArrayList<AddUserData>();


        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.raw_adduser, parent, false);

            return new UserViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(UserViewHolder userViewHolder, int position) {
            final AddUserData user = moviesList.get(position);
            userViewHolder.text1.setText(user.getName());
            userViewHolder.text2.setText(user.getEmail());
            userViewHolder.list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.e("click", getRef(i).getKey());

                    final conversation sender_Model = new conversation();
                    final Firebase transaction_firbase = LibraryClass.getFirebase().child("users").child(firebase.getAuth().getUid()).child(getString(R.string.conversation)).child(user.getUid());
                    transaction_firbase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e("data", dataSnapshot + "");
                            if (dataSnapshot.getValue() == null) {
                                sender_Model.setTable_name(getString(R.string.table_name, firebase.getAuth().getUid(), user.getUid()));
                                sender_Model.setUser_name(user.getName());
                                sender_Model.setLast_msg("");
                                sender_Model.setRead_number(0);
                                sender_Model.setStatus("online");
                                transaction_firbase.setValue(sender_Model);
                                Firebase transaction_firbase_receicer = LibraryClass.getFirebase().child("users").child(user.getUid()).child(getString(R.string.conversation)).child(firebase.getAuth().getUid());
                                sender_Model.setUser_name(own_user.getName());
                                transaction_firbase_receicer.setValue(sender_Model);

                            } else {
                                conversation sender_getModel = dataSnapshot.getValue(conversation.class);
                                sender_Model.setTable_name(sender_getModel.getTable_name());


                            }
                            Log.e("username", sender_Model.getUser_name() + "check");
                            Intent intent = new Intent(AddActivity.this, ChatActivity.class);
                            intent.putExtra("user_id", user.getUid());
                            intent.putExtra("title", user.getName());
                            intent.putExtra("table_name", sender_Model.getTable_name());
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });


                }
            });
        }

        @Override
        public int getItemCount() {
            return moviesList.size();
        }

        public void addData(List<AddUserData> addUserDatas) {
            moviesList = addUserDatas;
            notifyDataSetChanged();
        }
    }

    private class UserRecyclerAdapter extends FirebaseRecyclerAdapter<User, UserViewHolder> {

        public UserRecyclerAdapter(
                Class<User> modelClass,
                int modelLayout,
                Class<UserViewHolder> viewHolderClass,
                Query ref) {

            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(
                final UserViewHolder userViewHolder,
                final User user, final int i) {

            userViewHolder.text1.setText(user.getName());
            userViewHolder.text2.setText(user.getEmail());
            if (getRef(i).getKey().equals(firebase.getAuth().getUid())) {
                userViewHolder.text2.setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                userViewHolder.text2.setTextColor(getResources().getColor(R.color.secondeyText));
            }
            userViewHolder.list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("click", getRef(i).getKey());
                    if (!getRef(i).getKey().equals(firebase.getAuth().getUid())) {
                        final conversation sender_Model = new conversation();
                        final Firebase transaction_firbase = LibraryClass.getFirebase().child("users").child(firebase.getAuth().getUid()).child(getString(R.string.conversation)).child(getRef(i).getKey());
                        transaction_firbase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.e("data", dataSnapshot + "");
                                if (dataSnapshot.getValue() == null) {
                                    sender_Model.setTable_name(getString(R.string.table_name, firebase.getAuth().getUid(), getRef(i).getKey()));
                                    sender_Model.setUser_name(user.getName());
                                    sender_Model.setLast_msg("");
                                    sender_Model.setRead_number(0);
                                    sender_Model.setStatus("online");
                                    transaction_firbase.setValue(sender_Model);
                                    Firebase transaction_firbase_receicer = LibraryClass.getFirebase().child("users").child(getRef(i).getKey()).child(getString(R.string.conversation)).child(firebase.getAuth().getUid());
                                    sender_Model.setUser_name(own_user.getName());
                                    transaction_firbase_receicer.setValue(sender_Model);

                                } else {
                                    conversation sender_getModel = dataSnapshot.getValue(conversation.class);
                                    sender_Model.setTable_name(sender_getModel.getTable_name());


                                }
                                Log.e("username", sender_Model.getUser_name() + "check");
                                Intent intent = new Intent(AddActivity.this, ChatActivity.class);
                                intent.putExtra("user_id", getRef(i).getKey());
                                intent.putExtra("title", user.getName());
                                intent.putExtra("table_name", sender_Model.getTable_name());
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });


                    }

                }
            });
        }
    }


}
