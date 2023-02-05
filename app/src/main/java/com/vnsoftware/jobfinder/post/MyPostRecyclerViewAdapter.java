package com.vnsoftware.jobfinder.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vnsoftware.jobfinder.R;
import com.vnsoftware.jobfinder.database.model.post.PostModel;
import com.vnsoftware.jobfinder.database.repository.PostRepository;
import com.vnsoftware.jobfinder.database.repository.UserRepository;
import com.vnsoftware.jobfinder.dialog.ConfirmDialog;

import java.text.DecimalFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<PostModel> postList;
    private PostRepository postRepository;
    private UserRepository userRepository;
    DecimalFormat format = new DecimalFormat("0.#");

    public MyPostRecyclerViewAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
        this.postRepository = PostRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.item_recyclerview_my_apply, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel postModel = postList.get(position);

        userRepository.getUserByUID(postModel.getUserId(), user -> {
            if (!user.getAvatar().equals("")) {
                Picasso.get().load(user.getAvatar()).into(holder.imgPostOwnerAvatar);
            }
            holder.txvPostOwnerName.setText(user.getFullName());
        });
        holder.txvJobDescription.setText("Mô tả công việc: " + postModel.getPostDescription());
        holder.txvJobSalary.setText("Mức lương tối thiểu: " + postModel.getPostSalary() + " triệu đồng");
        holder.txvJobExperience.setText("Kinh nghiệm: " + format.format(postModel.getPostExperience()) + " năm");
        holder.txvJobAddress.setText("Địa chỉ: " + postModel.getPostAddress());
        holder.txvJobName.setText("Tên công việc: " + postModel.getPostName());
        holder.txvJobMajor.setText("Chuyên ngành: " + postModel.getPostMajor());
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmDialog confirmDialog = new ConfirmDialog(
                        context, "Xóa bài đăng",
                        "Bạn có chắc chắn muốn xóa bài đăng không",
                        new ConfirmDialog.OnEventListener() {
                            @Override
                            public void onConfirm() {
                                postRepository.deletePost(postModel);
                                postList.remove(holder.getBindingAdapterPosition());
                                notifyItemRemoved(holder.getBindingAdapterPosition());
                            }

                            @Override
                            public void onCancel() {

                            }
                        }
                );
                confirmDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgPostOwnerAvatar;
        private TextView txvPostOwnerName;
        private TextView txvJobName;
        private TextView txvJobMajor;
        private TextView txvJobAddress;
        private TextView txvJobExperience;
        private TextView txvJobSalary;
        private TextView txvJobDescription;
        private ImageView btnRemove;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostOwnerAvatar = itemView.findViewById(R.id.imgPostOwnerAvatar);
            txvPostOwnerName = itemView.findViewById(R.id.txvPostOwnerName);
            txvJobName = itemView.findViewById(R.id.txvJobName);
            txvJobMajor = itemView.findViewById(R.id.txvJobMajor);
            txvJobAddress = itemView.findViewById(R.id.txvJobAddress);
            txvJobExperience = itemView.findViewById(R.id.txvJobExperience);
            txvJobSalary = itemView.findViewById(R.id.txvJobSalary);
            txvJobDescription = itemView.findViewById(R.id.txvJobDescription);
            btnRemove = itemView.findViewById(R.id.btnDelete);
        }
    }
}
