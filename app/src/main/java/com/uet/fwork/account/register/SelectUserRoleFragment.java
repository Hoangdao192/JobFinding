package com.uet.fwork.account.register;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.uet.fwork.R;
import com.uet.fwork.database.model.UserRole;
import com.uet.fwork.database.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class SelectUserRoleFragment extends Fragment {
    private static final String LOG_TAG = "User role";

    private Button btnCandidate, btnEmployer;
    private UserRepository userRepository;
    private FirebaseUser firebaseUser;

    private NavController navController;

    public SelectUserRoleFragment() {
        super(R.layout.fragment_select_user_role);
        userRepository = new UserRepository(FirebaseDatabase.getInstance());
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(LOG_TAG, "Select user role");
        btnCandidate = view.findViewById(R.id.btnCandidate);
        btnEmployer = view.findViewById(R.id.btnEmployer);
        navController = Navigation.findNavController(getActivity(), R.id.navigation_host);

        btnCandidate.setOnClickListener(button -> {
            Log.d(LOG_TAG, "Select role " + UserRole.CANDIDATE);
            Map<String, Object> data = new HashMap<>();
            data.put("role", UserRole.CANDIDATE);
            userRepository.updateUser(firebaseUser.getUid(), data);
            navController.navigate(
                            R.id.action_selectUserRoleFragment_to_registerCreateProfileFragment
                    );
        });

        btnEmployer.setOnClickListener(button -> {
            Log.d(LOG_TAG, "Select role " + UserRole.EMPLOYER);
            Map<String, Object> data = new HashMap<>();
            data.put("role", UserRole.EMPLOYER);
            userRepository.updateUser(firebaseUser.getUid(), data);
            navController.navigate(
                            R.id.action_selectUserRoleFragment_to_createCompanyProfileFragment
                    );
        });
    }
}
