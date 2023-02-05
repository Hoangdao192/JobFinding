package com.vnsoftware.jobfinder.post;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.R;
//import com.uet.fwork.ViewProfileActivity;
import com.vnsoftware.jobfinder.database.model.UserModel;
import com.vnsoftware.jobfinder.ViewProfileActivityCandidate;
import com.vnsoftware.jobfinder.ViewProfileActivityEmployer;
import com.vnsoftware.jobfinder.database.model.UserRole;
import com.vnsoftware.jobfinder.database.model.post.PostApplyModel;
import com.vnsoftware.jobfinder.database.model.post.PostApplyStatus;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.database.model.post.ReactionModel;
import com.vnsoftware.jobfinder.database.repository.CommentRepository;
import com.vnsoftware.jobfinder.database.repository.PostApplyRepository;
import com.vnsoftware.jobfinder.database.repository.PostReactionRepository;
import com.vnsoftware.jobfinder.database.repository.Repository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.dialog.ConfirmDialog;
import com.vnsoftware.jobfinder.dialog.ErrorDialog;
import com.vnsoftware.jobfinder.firebasehelper.CloudMessagingHelper;
import com.vnsoftware.jobfinder.firebasehelper.FirebaseAuthHelper;
import com.vnsoftware.jobfinder.util.TimestampToString;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyHolder> {

    Context context;
    List<PostModel> postModelList;
    String myUid;
//    UserRepository userRepository;
    private PostReactionRepository reactionRepository;
    private UserRepository userRepository;
    private PostApplyRepository postApplyRepository;
    private CommentRepository commentRepository;
    private FirebaseUser firebaseUser;

    private FirebaseAuthHelper firebaseAuthHelper;
    DecimalFormat format = new DecimalFormat("0.#");
    public PostsAdapter(Context context, List<PostModel> postModelList) {
        this.context = context;
        this.postModelList = postModelList;
        postApplyRepository = PostApplyRepository.getInstance();
        userRepository = UserRepository.getInstance();
        reactionRepository = PostReactionRepository.getInstance();
        commentRepository = CommentRepository.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseAuthHelper = FirebaseAuthHelper.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_posts.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        PostModel post = postModelList.get(position);

        if (!firebaseAuthHelper.getUser().getRole().equals(UserRole.CANDIDATE)) {
            holder.btnPostApply.setVisibility(View.GONE);
        }

        userRepository.getUserByUID(post.getUserId(), new Repository.OnQuerySuccessListener<UserModel>() {
            @Override
            public void onSuccess(UserModel userModel) {
                if (userModel.getRole().equals(UserRole.CANDIDATE)) {
                    holder.title.setText("Cần tìm việc:");
                    holder.salaryTitle.setText("Mức lương mong muốn:");
                    holder.locationTitle.setText("Khu vực mong muốn:");
                    holder.btnPostApply.setVisibility(View.GONE);
                } else {
                    holder.title.setText("Cần tuyển công việc:");
                    holder.salaryTitle.setText("Mức lương dự kiến:");
                    holder.btnPostApply.setVisibility(View.VISIBLE);
                }
            }
        });

        //get data
        String uid = post.getUserId();
        String uEmail = post.getUserEmail();
        String uName = post.getUserName();
        String uDp = post.getUserDp();
        String pId = post.getPostId();
        String pJobName = post.getPostName();
        String pJobMajor = post.getPostMajor();
        String pJobAddress = post.getPostAddress();
        double pJobExperience = post.getPostExperience();
        Long pJobSalary = post.getPostSalary();
        String pJobDescription = post.getPostDescription();
        String pJobImage = post.getPostImage();
        String pTimeStamp = post.getPostTime().toString();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        //Calendar calendar = Calendar.getInstance(Locale.getDefault());
        //calendar.setTimeInMillis(Long.parseLong(pTimeStamp));

        //set data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(TimestampToString.convert(post.getPostTime()));
        holder.pJobNameTv.setText(pJobName);
        holder.pJobMajorTv.setText(pJobMajor);
        holder.pJobDescriptionTv.setText(pJobDescription);
        holder.pJobSalaryTv.setText(Long.toString(pJobSalary));
        holder.pJobAddressTv.setText(pJobAddress);
        holder.pTimeTv.setText(TimestampToString.convert(post.getPostTime()));
        holder.pJobExperienceTv.setText(format.format(pJobExperience));

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.blank_user_avatar).into(holder.uAvatarIv);
        } catch (Exception e) {

        }

        //set post image
        //if post doesn't have any images, hide imageView
        if (pJobImage.equals("")) {
            //hide imageView
            holder.postImageIv.setVisibility(View.GONE);
        } else {
            try {
                Picasso.get().load(pJobImage).into(holder.postImageIv);
            } catch (Exception e) {

            }
        }

        holder.uNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRepository = UserRepository.getInstance();
                userRepository.getUserRole(uid, g -> {
                    if(g.equals("Candidate")) {
                        Intent intent = new Intent(context, ViewProfileActivityCandidate.class);
                        intent.putExtra("id",uid);
                        context.startActivity(intent);
                    } else if (g.equals("Employer")) {
                        Intent intent = new Intent(context, ViewProfileActivityEmployer.class);
                        intent.putExtra("id",uid);
                        context.startActivity(intent);
                    }
                });


            }
        });

        //Buttons click
        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreButton, uid, myUid, pId, pJobImage);
            }
        });

        reactionRepository.isUserLikePost(post.getPostId(), firebaseUser.getUid(), new Repository.OnQuerySuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_no_fill));
                } else {
                    holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_fill));
                }
            }
        });

        holder.btnLike.setOnClickListener(button -> reactionRepository
                .isUserReactionPost(post.getPostId(), firebaseUser.getUid())
                .addOnSuccessListener(isExists -> {
                    if (isExists) {
                        holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_no_fill));
                        reactionRepository.removeReactionByPostAndUser(post.getPostId(), firebaseUser.getUid());
                    } else {
                        holder.btnLike.setImageDrawable(context.getDrawable(R.drawable.ic_heart_fill));
                        ReactionModel reaction = new ReactionModel(
                                firebaseUser.getUid(), post.getPostId(),
                                Calendar.getInstance().getTimeInMillis()/1000
                        );
                        reactionRepository.insert(reaction)
                                .addOnSuccessListener(isSuccess -> {
                                    if (isSuccess) {
                                        CloudMessagingHelper
                                                .getInstance()
                                                .sendPostReactionNotify(reaction);
                                    }
                                }).execute();
                    }
                }).execute());
        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentViewDialog commentViewDialog = new CommentViewDialog(context, post.getPostId());
                commentViewDialog.show();
            }
        });
        holder.btnPostApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postApplyRepository.isUserApplyPost(post.getPostId(), firebaseUser.getUid(),
                        result -> {
                            if (result) {
                                ErrorDialog errorDialog = new ErrorDialog(
                                        context, "Ứng tuyển không thành công",
                                        "Bạn đã ứng tuyển công việc này rồi"
                                );
                                errorDialog.show();
                            } else {
                                postApplyRepository.insert(new PostApplyModel(
                                        post.getPostId(),
                                        firebaseUser.getUid(),
                                        Calendar.getInstance().getTimeInMillis() / 1000,
                                        PostApplyStatus.WAITING
                                ),  postApplyModel -> {
                                    if (postApplyModel != null) {
                                        ErrorDialog errorDialog = new ErrorDialog(
                                                context, "Ứng tuyển thành công",
                                                "Đơn ứng tuyển của bạn đã được gửi đi"
                                        );
                                        errorDialog.show();
                                    } else {
                                        ErrorDialog errorDialog = new ErrorDialog(
                                                context, "Ứng tuyển không thành công",
                                                "Đơn ứng tuyển của bạn chưa được gửi đi"
                                        );
                                        errorDialog.show();
                                    }
                                });
                            }
                        });
            }
        });

        reactionRepository.getNumberOfReaction(post.getPostId(), new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                holder.txvLikeNumber.setText(result + " Lượt thích");
            }
        });

        commentRepository.getNumberOfComment(post.getPostId(), new Repository.OnQuerySuccessListener<Long>() {
            @Override
            public void onSuccess(Long result) {
                holder.txvCommentNumber.setText(result + " Bình luận");
            }
        });
    }

    private void showMoreOptions(ImageButton moreButton, String uid, String myUid, String pId, String pJobImage) {
        //creating popup menu
        PopupMenu popupMenu = new PopupMenu(context, moreButton, Gravity.END);

        //only show delete on current user's posts

        if (uid.equals(myUid)) {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Xóa Bài Viết");
        } else {
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Báo cáo");
        }

        //adding items

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0 && uid.equals(myUid)) {
                    beginDelete(pId, pJobImage);

                } else {
                    ConfirmDialog confirmDialog = new ConfirmDialog(
                            context, "Báo cáo bài viết",
                            "Bạn có chắc chắn muốn báo cáo bài đăng này?",
                            new ConfirmDialog.OnEventListener() {
                                @Override
                                public void onConfirm() {
                                    ErrorDialog errorDialog = new ErrorDialog(
                                            context, "Báo cáo thành công",
                                            "Cảm ơn bạn đã báo cáo. \nChúng tôi sẽ xem xét bài đăng này."
                                    );
                                    errorDialog.show();
                                }

                                @Override
                                public void onCancel() {

                                }
                            }
                    );
                    confirmDialog.show();
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pJobImage) {
        if (pJobImage.equals("noImage")) {

        } else {

        }
    }

    @Override
    public int getItemCount() {
        return postModelList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //views from row_posts.xml
        ImageView uAvatarIv, postImageIv, btnLike;
        TextView uNameTv, pTimeTv, pJobNameTv, pJobMajorTv, pJobAddressTv, pJobExperienceTv, pJobSalaryTv, pJobDescriptionTv, title, salaryTitle,locationTitle;
        ImageButton moreButton;
        Button commentButton, btnPostApply;

        private TextView txvLikeNumber, txvCommentNumber;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uAvatarIv = itemView.findViewById(R.id.uPostAvatarIv);
            postImageIv = itemView.findViewById(R.id.jobImagePost);
            uNameTv = itemView.findViewById(R.id.uPostNameTv);
            title = itemView.findViewById(R.id.jobTitle);
            salaryTitle = itemView.findViewById(R.id.salaryTitle);
            locationTitle = itemView.findViewById(R.id.locationTitle);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pJobNameTv = itemView.findViewById(R.id.jobNamePost);
            pJobMajorTv = itemView.findViewById(R.id.jobMajorPost);
            pJobAddressTv = itemView.findViewById(R.id.jobAddressPost);
            pJobExperienceTv = itemView.findViewById(R.id.jobExperiencePost);
            pJobSalaryTv = itemView.findViewById(R.id.jobSalaryPost);
            pJobDescriptionTv = itemView.findViewById(R.id.jobDescriptionPost);
            moreButton = itemView.findViewById(R.id.postMoreButton);
            btnLike = itemView.findViewById(R.id.btnLike);
            commentButton = itemView.findViewById(R.id.postCommentButton);
            btnPostApply = itemView.findViewById(R.id.btnPostApply);
            txvCommentNumber = itemView.findViewById(R.id.txtCommentNumber);
            txvLikeNumber = itemView.findViewById(R.id.txtLikeNumber);
        }

    }
}
