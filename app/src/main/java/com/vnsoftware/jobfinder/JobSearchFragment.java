package com.vnsoftware.jobfinder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vnsoftware.jobfinder.adapter.SpinnerAdapter;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.post.PostsAdapter;

import java.util.ArrayList;
import java.util.List;

public class JobSearchFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;

    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private Spinner spnMajor;
    private Button search;
    private EditText edtAddress, edtSalary, edtExp;

    private List<PostModel> postList;
    private List<String> majorList = new ArrayList<>();

    public JobSearchFragment() {
        super(R.layout.fragment_job_search);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_job_search, container, false);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        recyclerView = view.findViewById(R.id.recyclerviewSearch);

        spnMajor = view.findViewById(R.id.spnJobMajor);
        edtAddress = view.findViewById(R.id.edtJobAddress);
        edtSalary = view.findViewById(R.id.edtJobSalary);
        edtExp = view.findViewById(R.id.edtJobSalary);
        search = view.findViewById(R.id.btnSearchJob);

        postList = new ArrayList<>();
        loadMajorList();

        //double jobExperience = Double.parseDouble(edtExp.getText().toString().trim());
        //Long jobSalary = Long.valueOf(edtExp.getText().toString().trim());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                //show newest post first (load data from last)
                layoutManager.setStackFromEnd(true);
                layoutManager.setReverseLayout(true);
                //set this layout to recyclerview
                recyclerView.setLayoutManager(layoutManager);
                if (spnMajor.getSelectedItem().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "Bạn chưa chọn chuyên ngành công việc!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtSalary.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "Bạn chưa nhập lương mong muốn", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edtExp.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), "Bạn chưa nhập kinh nghiệm!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String jobMajor = spnMajor.getSelectedItem().toString().trim();
                Long jobSalary = Long.valueOf(edtSalary.getText().toString().trim());
                String adress =  edtAddress.getText().toString().trim();
                double jobExperience = Double.parseDouble(edtExp.getText().toString().trim());

                //init post list
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("posts/list");
                Query query = databaseReference.orderByChild("postMajor").equalTo(jobMajor);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        postList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PostModel myPosts = ds.getValue(PostModel.class);


                                if ( myPosts.getPostAddress().contains(adress)&&(myPosts.getPostSalary() >= jobSalary || myPosts.getPostExperience() <= jobExperience)) {


                                        //add to list
                                        postList.add(myPosts);

                                        //adapter
                                        postsAdapter = new PostsAdapter(getActivity(), postList);
                                        recyclerView.setAdapter(postsAdapter);

                                }
                            }
                        if (postList.isEmpty()){
                            Toast.makeText(getActivity(), "Không tìm thấy công việc!", Toast.LENGTH_SHORT).show();
                        }
                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        return view;
    }

    private void loadMajorList() {
        firebaseDatabase.getReference("userMajors")
                .get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        GenericTypeIndicator<List<String>> genericTypeIndicator =
                                new GenericTypeIndicator<List<String>>() {
                                };
                        majorList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                        SpinnerAdapter<String> spinnerMajorAdapter = new SpinnerAdapter<>(
                                getActivity(), majorList, R.layout.item_spinner,
                                (itemView, position) -> {
                                    TextView txtView = itemView.findViewById(R.id.txtView);
                                    txtView.setText(majorList.get(position));
                                }
                        );
                        spnMajor.setAdapter(spinnerMajorAdapter);
                    }
                })
                .addOnFailureListener(System.out::println);
    }

}