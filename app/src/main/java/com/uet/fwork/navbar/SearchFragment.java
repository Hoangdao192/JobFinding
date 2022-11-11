package com.uet.fwork.navbar;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

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

    /*
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_logout) {
                    auth.signOut();
                    return true;
                } else if (id == R.id.action_search) {
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }
*/

}