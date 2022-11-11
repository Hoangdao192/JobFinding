package com.uet.fwork.navbar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uet.fwork.R;
import com.uet.fwork.UsersAdapter;
import com.uet.fwork.database.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    RecyclerView recyclerView;
    UsersAdapter usersAdapter;
    List<UserModel> userModelList;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView =view.findViewById(R.id.search_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list for searching
        userModelList = new ArrayList<>();

        //getAll users
        getAllUsers();

        return view;
    }

    private void getAllUsers(){
        //get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModelList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    UserModel userModel = ds.getValue(UserModel.class);

                    //get all users except current signed in user
                    if(!userModel.getEmail().equals(currentUser.getEmail())){
                        userModelList.add(userModel);
                    }

                    usersAdapter = new UsersAdapter(getActivity(),userModelList);
                    //set adapter to the recycler view
                    recyclerView.setAdapter(usersAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}