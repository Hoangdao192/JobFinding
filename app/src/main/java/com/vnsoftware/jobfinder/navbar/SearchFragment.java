package com.vnsoftware.jobfinder.navbar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
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
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.UsersAdapter;
import com.vnsoftware.jobfinder.database.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    FirebaseAuth auth;

    private SearchView searchView;
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

        searchView = view.findViewById(R.id.search_user);
        searchView.clearFocus();
        recyclerView = view.findViewById(R.id.search_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list for searching
        userModelList = new ArrayList<>();

        getAllUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //if search query is not null then search
                if (!TextUtils.isEmpty(query.trim())) {
                    searchUsers(query);

                } else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUsers(newText);

                } else {
                    getAllUsers();
                }
                return false;
            }
        });

        return view;
    }

    private void searchUsers(String query) {
        //get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);

                    //get all searched users except current signed in user
                    if (!userModel.getEmail().equals(currentUser.getEmail())) {
                        if (userModel.getFullName() != "" && userModel.getFullName() != null && userModel.getFullName().toLowerCase().contains(query.toLowerCase())) {
                            userModelList.add(userModel);
                        }
                    }

                    usersAdapter = new UsersAdapter(getActivity(), userModelList);
                    //refresh adapter
                    usersAdapter.notifyDataSetChanged();
                    //set adapter to the recycler view
                    recyclerView.setAdapter(usersAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        //get all data from path

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModelList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel userModel = ds.getValue(UserModel.class);

                    //get all users except current signed in user
                    if (!userModel.getEmail().equals(currentUser.getEmail())) {
                        userModelList.add(userModel);
                    }

                    usersAdapter = new UsersAdapter(getActivity(), userModelList);
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