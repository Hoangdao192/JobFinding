package com.uet.fwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class SelectUserRoleFragment extends Fragment {

    private Button btnCandidate, btnEmployer;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;

    public SelectUserRoleFragment() {
        super(R.layout.fragment_select_user_role);
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCandidate = view.findViewById(R.id.btnCandidate);
        btnEmployer = view.findViewById(R.id.btnEmployer);

        btnCandidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data = new HashMap<>();
                data.put("role", UserRole.CANDIDATE);
                userRepository.updateUser(firebaseUser.getUid(), data);
                Navigation.findNavController(getActivity(), R.id.navigation_host)
                        .navigate(
                                R.id.action_selectUserRoleFragment_to_registerCreateProfileFragment
                        );
            }
        });

        btnEmployer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data = new HashMap<>();
                data.put("role", UserRole.EMPLOYER);
                userRepository.updateUser(firebaseUser.getUid(), data);
                Navigation.findNavController(getActivity(), R.id.navigation_host)
                        .navigate(
                                R.id.action_selectUserRoleFragment_to_registerCreateCompanyProfile
                        );
            }
        });
    }
}
