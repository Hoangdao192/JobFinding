package com.uet.fwork.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uet.fwork.Constants;
import com.uet.fwork.database.model.CandidateModel;
import com.uet.fwork.database.model.EmployerModel;
import com.uet.fwork.database.model.UserModel;
import com.uet.fwork.database.model.UserRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository extends Repository {

    private static final String LOG_TAG = "UserRepository";

    private static UserRepository INSTANCE = null;

    //  Path from root node
    public static final String databaseReferencePath = "users/";

    public static UserRepository getInstance() {
        if (!Repository.isInitialize()) {
            Log.d(LOG_TAG, "Repository has not been initialized yet");
            return null;
        }

        if (INSTANCE == null) {
            INSTANCE = new UserRepository();
        }

        return INSTANCE;
    }

    public UserRepository() {
        super(databaseReferencePath);
    }

    public void isUserExists(String userUID, OnQuerySuccessListener<Boolean> onQuerySuccessListener) {
        rootDatabaseReference.child(userUID).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    onQuerySuccessListener.onSuccess(true);
                } else {
                    onQuerySuccessListener.onSuccess(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getUserByUID(
            String userUID,
            Repository.OnQuerySuccessListener<UserModel> listener
    ) {

        rootDatabaseReference.child(userUID).get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String userRole = (String) dataSnapshot.child("role").getValue();
                if (userRole.equals(UserRole.CANDIDATE)) {
                    UserModel userModel = dataSnapshot.getValue(CandidateModel.class);
                    listener.onSuccess(userModel);
                } else if (userRole.equals(UserRole.EMPLOYER)) {
                    UserModel userModel = dataSnapshot.getValue(EmployerModel.class);
                    listener.onSuccess(userModel);
                } else if (userRole.equals(UserRole.NOT_SET)) {
                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
                    listener.onSuccess(userModel);
                }
            } else {
                listener.onSuccess(null);
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
            listener.onSuccess(null);
        });
    }

    public QueryTask<Boolean> insert(UserModel user) {
        return new QueryTask<Boolean>() {
            @Override
            public void execute() {
                if (!user.getId().equals("")) {
                    rootDatabaseReference.child(user.getId()).setValue(user)
                            .addOnSuccessListener(unused -> onSuccess(true))
                            .addOnFailureListener(getOnFailedListener())
                            .addOnCanceledListener(getOnCancelledListener());
                }
            }
        };
    }

    public QueryTask<Boolean> update(UserModel user) {
        return new QueryTask<Boolean>() {
            @Override
            public void execute() {
                if (!user.getId().equals("")) {
                    isUserExists(user.getId())
                            .addOnSuccessListener(isExists -> {
                                if (isExists) {
                                    rootDatabaseReference
                                            .child(user.getId())
                                            .setValue(user)
                                            .addOnSuccessListener(unused -> onSuccess(true))
                                            .addOnFailureListener(getOnFailedListener())
                                            .addOnCanceledListener(getOnCancelledListener());
                                } else {
                                    Log.w(LOG_TAG, "Update: user is not exists");
                                    onSuccess(false);
                                }
                            })
                            .addOnFailedListener(getOnFailedListener())
                            .addOnCancelledListener(getOnCancelledListener())
                            .execute();
                }
            }
        };
    }

    public QueryTask<Boolean> isUserExists(String userId) {
        return new QueryTask<Boolean>() {
            @Override
            public void execute() {
                rootDatabaseReference.child(userId).get()
                        .addOnSuccessListener(dataSnapshot -> {
                            if (dataSnapshot.exists()) {
                                onSuccess(true);
                            } else {
                                onSuccess(false);
                            }
                        })
                        .addOnFailureListener(e -> {
                            e.printStackTrace();
                            onFailed(e);
                        });
            }
        };
    }

    public void insertUser(UserModel userModel) {
        String userUID = userModel.getId();
        if (userModel instanceof CandidateModel) {
            rootDatabaseReference.child(userUID).setValue(((CandidateModel) userModel));
        } else if (userModel instanceof EmployerModel) {
            rootDatabaseReference.child(userUID).setValue(((EmployerModel) userModel));
        } else {
            rootDatabaseReference.child(userUID).setValue(userModel);
        }
    }

    public void updateUser(String userUID, Map<String, Object> updateDataMap) {
        rootDatabaseReference.child(userUID).updateChildren(updateDataMap);
    }

    public void updateUser(String userUID, UserModel userModel) {
        if (userModel.getRole().equals(UserRole.CANDIDATE)) {
            rootDatabaseReference.child(userUID).setValue(((CandidateModel) userModel));
        } else if (userModel.getRole().equals(UserRole.EMPLOYER)) {
            rootDatabaseReference.child(userUID).setValue(((EmployerModel) userModel));
        }
    }

    public void updateUser(UserModel userModel, OnQuerySuccessListener<Boolean> listener) {
        if (!userModel.getId().isEmpty()) {
            isUserExists(userModel.getId(), new OnQuerySuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        if (userModel instanceof CandidateModel) {
                            rootDatabaseReference.child(userModel.getId()).setValue((CandidateModel) userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            listener.onSuccess(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            listener.onSuccess(false);
                                            e.printStackTrace();
                                        }
                                    });
                        } else if (userModel instanceof EmployerModel) {
                            rootDatabaseReference.child(userModel.getId()).setValue((EmployerModel) userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            listener.onSuccess(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            listener.onSuccess(false);
                                            e.printStackTrace();
                                        }
                                    });
                        }
                    } else {
                        listener.onSuccess(false);
                    }
                }
            });
        } else {
            listener.onSuccess(false);
        }
    }

    public void getUserRole(String userUID, OnQuerySuccessListener<String> listener) {
        rootDatabaseReference.child(userUID).child("role").get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        listener.onSuccess(dataSnapshot.getValue(String.class));
                    }
                })
                .addOnFailureListener(System.out::println);
    }

    public void getAllUserFullNameSimilarTo(String target, Integer limit, OnQuerySuccessListener<List<UserModel>> listener) {
        Client client = new Client(Constants.ALGOLIA_APPLICATION_ID, Constants.ALGOLIA_SEARCH_API_KEY);
        Index index = client.getIndex("users");

        Query query = new Query(target);

        index.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(JSONObject content, AlgoliaException error) {
                try {
                    JSONArray hitsArray = content.getJSONArray("hits");
                    List<UserModel> userList = new ArrayList<>();
                    for (int i = 0; i < hitsArray.length(); ++i) {
                        JSONObject userData = hitsArray.getJSONObject(i);
                        UserModel userModel = new UserModel();
                        userModel.setId((String) userData.get("id"));
                        userModel.setEmail(userData.getString("email"));
                        userModel.setContactEmail(userData.getString("contactEmail"));
                        userModel.setFullName(userData.getString("fullName"));
                        userModel.setLastUpdate(userData.getLong("lastUpdate"));
                        userModel.setPhoneNumber(userData.getString("phoneNumber"));
                        userModel.setRole(userData.getString("role"));
                        userModel.setAvatar(userData.getString("avatar"));
                        userList.add(userModel);
                    }
                    listener.onSuccess(userList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

//        SearchClient client = DefaultSearchClient.create(
//                Constants.ALGOLIA_APPLICATION_ID, Constants.ALGOLIA_SEARCH_API_KEY);
//
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        Handler handler = new Handler(Looper.getMainLooper());
//
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                SearchIndex<UserModel> index = client.initIndex("users", UserModel.class);
//                SearchResult<UserModel> result = index.search(new Query(target)).setHitsPerPage(limit);
//                result.getHits().forEach(System.out::println);
//                System.out.println(result.getHits().size());
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        listener.onSuccess(result.getHits());
//                    }
//                });
//            }
//        });


//        index.searchAsync(new Query(target).setHitsPerPage(limit)).thenAccept((results) -> {
//            System.out.println(results.getHits().size());
//           results.getHits().forEach(System.out::println);
//        });
    }
}
