package com.example.smartoutfitapp.ui.outfit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.db.AppDatabase;
import com.example.smartoutfitapp.db.OutfitDao;
import com.example.smartoutfitapp.model.Outfit;

import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.ViewHolder> {

    private List<Outfit> mList;
    private Context mContext;
    private OutfitDao outfitDao;
    private int currentUid; // 当前登录的用户ID

    public OutfitAdapter(Context context, List<Outfit> list) {
        this.mContext = context;
        this.mList = list;
        this.outfitDao = AppDatabase.getDatabase(context).outfitDao();

        // 【新增】获取当前用户ID
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        this.currentUid = prefs.getInt("current_uid", -1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<Outfit> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = mList.get(position);

        holder.tvTitle.setText(outfit.title);
        Glide.with(mContext)
                .load(outfit.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivImage);

        // 【修改】根据 userId 检查收藏状态
        if (currentUid != -1) {
            Outfit savedOutfit = outfitDao.getOutfitByTitleAndUser(outfit.title, currentUid);
            boolean isFav = (savedOutfit != null);
            updateFavoriteIcon(holder.ivFavorite, isFav);
        }

        holder.ivFavorite.setOnClickListener(v -> {
            if (currentUid == -1) {
                Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }

            Outfit currentCheck = outfitDao.getOutfitByTitleAndUser(outfit.title, currentUid);

            if (currentCheck == null) {
                // 【修改】收藏时，写入当前 userId
                outfit.userId = currentUid;
                outfitDao.insert(outfit);
                updateFavoriteIcon(holder.ivFavorite, true);
                Toast.makeText(mContext, "已收藏", Toast.LENGTH_SHORT).show();
            } else {
                // 取消收藏
                outfitDao.delete(currentCheck);
                updateFavoriteIcon(holder.ivFavorite, false);
                Toast.makeText(mContext, "取消收藏", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon(ImageView iv, boolean isFav) {
        if (isFav) {
            iv.setImageResource(R.drawable.ic_favorite);
        } else {
            iv.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivFavorite;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_outfit);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}