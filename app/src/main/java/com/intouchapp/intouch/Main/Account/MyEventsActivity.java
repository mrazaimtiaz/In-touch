package com.intouchapp.intouch.Main.Account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.intouchapp.intouch.Main.MainActivity;
import com.intouchapp.intouch.Models.Event;
import com.intouchapp.intouch.Models.Friend;
import com.intouchapp.intouch.Models.Hood;
import com.intouchapp.intouch.Models.House;
import com.intouchapp.intouch.Models.Relative;
import com.intouchapp.intouch.Models.User;
import com.intouchapp.intouch.R;
import com.intouchapp.intouch.Utills.DropDownAdapter;
import com.intouchapp.intouch.Utills.EventRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MyEventsActivity extends AppCompatActivity {


    //widget
    private RecyclerView mRecyclerView;
    private ConstraintLayout mNothing,mMain,mProgress;
    private ImageView mBack;

    private FirebaseFirestore mDb;

    private Context mContext;

    private List<String> freindList;
    private List<String> memberList;
    private List<String> relativeList;
    private User user;

    private String u_id;

    private static final String TAG = "MyEventsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);


        mRecyclerView = findViewById(R.id.recycler_view);
        mNothing = (ConstraintLayout) findViewById(R.id.constraintLayoutNothing);
        mProgress = (ConstraintLayout) findViewById(R.id.constraintLayoutprogress);
        mMain = (ConstraintLayout) findViewById(R.id.constraintLayoutmain);

        mBack = (ImageView) findViewById(R.id.iv_back);
        mDb = FirebaseFirestore.getInstance();
        mContext = MyEventsActivity.this;

        mMain.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
        gettingUserList();

        freindList = new ArrayList<>();
        memberList = new ArrayList<>();
        relativeList = new ArrayList<>();

        u_id = getIntent().getStringExtra(getString(R.string.u_id));

        Log.d(TAG, "onCreate: u_id post " + u_id);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    //******************************************************** getting userdata ****************************************************
    private void gettingUserList() {
        try {
            mDb.collection(getString(R.string.collection_users)).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        user = Objects.requireNonNull(task.getResult()).toObject(User.class);
                        try {
                            mDb.collection(getString(R.string.collection_hoods)).document(user.getN_id()).collection(getString(R.string.collection_houses)).document(user.getH_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        House house = Objects.requireNonNull(task.getResult()).toObject(House.class);
                                        assert house != null;
                                        if (house.getMembers() != null) {
                                            if (house.getMembers().size() != 0) {
                                                Log.d(TAG, "onComplete: get member list");
                                                memberList.addAll(house.getMembers());
                                                Log.d(TAG, "onComplete:memberList " + memberList);
                                            }
                                        }
                                    }
                                    mDb.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getUid()).collection(getString(R.string.collection_relatives)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                List<DocumentSnapshot> list = Objects.requireNonNull(task.getResult()).getDocuments();
                                                for (int i = 0; i < list.size(); i++) {
                                                    Relative relative = list.get(i).toObject(Relative.class);
                                                    assert relative != null;
                                                    if (relative.getR_id() != null) {
                                                        if (relative.getR_id().size() != 0)
                                                            Log.d(TAG, "onComplete: get relative list");
                                                        relativeList.addAll(relative.getR_id());
                                                        Log.d(TAG, "onComplete:relativeList " + relativeList);
                                                    }
                                                }
                                            }
                                            mDb.collection(getString(R.string.collection_users)).document(FirebaseAuth.getInstance().getUid()).collection(getString(R.string.collection_friends)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        List<DocumentSnapshot> list = Objects.requireNonNull(task.getResult()).getDocuments();
                                                        for (int i = 0; i < list.size(); i++) {
                                                            Friend friend = list.get(i).toObject(Friend.class);
                                                            assert friend != null;
                                                            if (friend.getF_id() != null) {
                                                                if (friend.getF_id().size() != 0) {
                                                                    Log.d(TAG, "onComplete: get friend list");
                                                                    freindList.addAll(friend.getF_id());
                                                                    Log.d(TAG, "onComplete:freindList " + freindList);
                                                                }
                                                            }
                                                            if (i == list.size() - 1) {
                                                                settingList();

                                                            }
                                                        }
                                                    } if(Objects.requireNonNull(task.getResult()).size() == 0) {
                                                        settingList();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            });


                        } catch (NullPointerException e) {
                            Log.d(TAG, "onComplete: NullPointerException " + e.getMessage());
                            Intent intent = new Intent(mContext, MainActivity.class);
                            startActivity(intent);
                        }
                    }

                }
            });
        }catch (NullPointerException e){
            e.printStackTrace();
            settingList();
        }

    }
    //********************************************************* setting list *********************************************************
    private void settingList() {
        final List<Event> members = new ArrayList<>();

            try {

                mDb.collection(getString(R.string.collection_events)).orderBy(getString(R.string.timestamp)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> list = Objects.requireNonNull(task.getResult()).getDocuments();
                            for (int i = 0; i < list.size(); i++) {
                                final Event event = list.get(i).toObject(Event.class);

                                if(u_id.equals(FirebaseAuth.getInstance().getUid())){
                                    assert event != null;
                                    if(u_id.equals(event.getU_id())){
                                        members.add(event);
                                    }
                                }else {
                                    assert event != null;
                                    if (event.getU_id().equals(u_id)) {

                                        if (event.getType().equals(getString(R.string.type_relative))) {
                                            if (event.getShareWith().equals(getString(R.string.empty))) {
                                                Log.d(TAG, "onComplete: into the list " + relativeList);
                                                if (relativeList != null) {
                                                    Log.d(TAG, "setting list relative list not null " + relativeList);
                                                    Log.d(TAG, "onComplete: " + relativeList);
                                                    if (relativeList.size() != 0) {
                                                        Log.d(TAG, "onComplete: size is 0" + relativeList);
                                                        if (relativeList.contains(event.getU_id())) {
                                                            Log.d(TAG, "onComplete: setting list add relative post");
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (user != null) {
                                                    if (event.getShareWith().equals(user.getH_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                        }
                                        if (event.getType().equals(getString(R.string.type_friend))) {
                                            if (event.getShareWith().equals(getString(R.string.empty))) {
                                                if (freindList != null) {
                                                    Log.d(TAG, "setting list freind list not null " + freindList);
                                                    if (freindList.size() != 0) {
                                                        if (freindList.contains(event.getU_id())) {
                                                            Log.d(TAG, "onComplete: setting list add freind list post");
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (user != null) {
                                                    if (event.getShareWith().equals(user.getH_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                        }
                                        if (event.getType().equals(getString(R.string.type_member))) {
                                            if (event.getShareWith().equals(getString(R.string.empty))) {
                                                if (memberList != null) {
                                                    Log.d(TAG, "onComplete: setting list " + memberList);
                                                    if (memberList.size() != 0) {
                                                        if (memberList.contains(event.getU_id())) {
                                                            Log.d(TAG, "onComplete: setting list add member post");
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (user != null) {
                                                    if (event.getShareWith().equals(user.getH_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                        }
                                        if (event.getType().equals(getString(R.string.type_knower))) {
                                            if (memberList != null) {
                                                if (memberList.size() != 0) {
                                                    if (memberList.contains(event.getU_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                            if (freindList != null) {
                                                if (freindList.size() != 0) {
                                                    if (freindList.contains(event.getU_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                            if (relativeList != null) {
                                                if (relativeList.size() != 0) {
                                                    if (relativeList.contains(event.getU_id())) {
                                                        members.add(event);
                                                    }
                                                }
                                            }
                                        }
                                        if (event.getType().equals(getString(R.string.type_hood))) {
                                            if (user.getN_id() != null) {
                                                mDb.collection(getString(R.string.collection_users)).document(event.getU_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            User postUser = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                                            assert postUser != null;
                                                            if (postUser.getN_id() != null) {
                                                                if (user.getN_id().equals(postUser.getN_id())) {
                                                                    members.add(event);
                                                                } else {
                                                                    if (memberList != null) {
                                                                        if (memberList.size() != 0) {
                                                                            if (memberList.contains(event.getU_id())) {
                                                                                members.add(event);
                                                                            }
                                                                        }
                                                                    }
                                                                    if (freindList != null) {
                                                                        if (freindList.size() != 0) {
                                                                            if (freindList.contains(event.getU_id())) {
                                                                                members.add(event);
                                                                            }
                                                                        }
                                                                    }
                                                                    if (relativeList != null) {
                                                                        if (relativeList.size() != 0) {
                                                                            if (relativeList.contains(event.getU_id())) {
                                                                                members.add(event);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                if (memberList != null) {
                                                                    if (memberList.size() != 0) {
                                                                        if (memberList.contains(event.getU_id())) {
                                                                            members.add(event);
                                                                        }
                                                                    }
                                                                }
                                                                if (freindList != null) {
                                                                    if (freindList.size() != 0) {
                                                                        if (freindList.contains(event.getU_id())) {
                                                                            members.add(event);
                                                                        }
                                                                    }
                                                                }
                                                                if (relativeList != null) {
                                                                    if (relativeList.size() != 0) {
                                                                        if (relativeList.contains(event.getU_id())) {
                                                                            members.add(event);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                });

                                            } else {
                                                if (memberList != null) {
                                                    if (memberList.size() != 0) {
                                                        if (memberList.contains(event.getU_id())) {
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                                if (freindList != null) {
                                                    if (freindList.size() != 0) {
                                                        if (freindList.contains(event.getU_id())) {
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                                if (relativeList != null) {
                                                    if (relativeList.size() != 0) {
                                                        if (relativeList.contains(event.getU_id())) {
                                                            members.add(event);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (event.getType().equals(getString(R.string.type_other))) {
                                            if (user.getN_id() != null) {
                                                members.add(event);

                                            } else {
                                                mDb.collection(getString(R.string.collection_users)).document(event.getU_id()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            User postUser = Objects.requireNonNull(task.getResult()).toObject(User.class);
                                                            assert postUser != null;
                                                            if (postUser.getN_id() != null) {
                                                                if (user.getN_id().equals(postUser.getN_id())) {
                                                                    members.add(event);
                                                                }
                                                            }
                                                            if (memberList != null) {
                                                                if (memberList.size() != 0) {
                                                                    if (memberList.contains(event.getU_id())) {
                                                                        members.add(event);
                                                                    }
                                                                }
                                                            }
                                                            if (freindList != null) {
                                                                if (freindList.size() != 0) {
                                                                    if (freindList.contains(event.getU_id())) {
                                                                        members.add(event);
                                                                    }
                                                                }
                                                            }
                                                            if (relativeList != null) {
                                                                if (relativeList.size() != 0) {
                                                                    if (relativeList.contains(event.getU_id())) {
                                                                        members.add(event);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                });

                                            }
                                        }
                                    }

                                }

                                if (i == list.size() - 1) {
                                    Log.d(TAG, "onComplete: 1settingrecycler " + memberList + relativeList + freindList);
                                    settingRecyclerView(members);
                                }
                            }
                        } if(Objects.requireNonNull(task.getResult()).size() == 0) {
                            Log.d(TAG, "onComplete: 2settingrecycler " + memberList + relativeList + freindList);
                            settingRecyclerView(members);
                        }
                    }
                });
            }catch (NullPointerException e){
                settingRecyclerView(members);
            }


    }

    //------------------------------------------------setting recyclerview---------------------------------------------------------------------
    public void settingRecyclerView(List<Event> events){
        if(events != null){
            if(events.size() != 0){
                mNothing.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                Log.d(TAG, "settingRecyclerView: " + relativeList + freindList + memberList);
                mRecyclerView.setAdapter(new EventRecyclerAdapter(events,relativeList,freindList,memberList,mContext));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
                mMain.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
            }else{
                mMain.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                mNothing.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        }else{
            mMain.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
            mNothing.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

    }

}
