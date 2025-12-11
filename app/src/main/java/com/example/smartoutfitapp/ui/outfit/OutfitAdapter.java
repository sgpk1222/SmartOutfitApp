package com.example.smartoutfitapp.ui.outfit;

import android.annotation.SuppressLint;
import android.content.Context;
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
    private OutfitDao outfitDao; // 数据库操作工具

    public OutfitAdapter(Context context, List<Outfit> list) {
        this.mContext = context;
        this.mList = list;
        // 获取数据库工具实例，方便后面查表
        this.outfitDao = AppDatabase.getDatabase(context).outfitDao();
    }

    // 设置新数据的方法
    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<Outfit> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载刚才写的卡片布局 item_outfit
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outfit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = mList.get(position);

        // 1. 设置标题
        holder.tvTitle.setText(outfit.title);

        // 2. 使用 Glide 加载网络图片
        Glide.with(mContext)
                .load(outfit.imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // 加载过程中显示的占位图
                .into(holder.ivImage);

        // 3. 检查数据库：这件衣服之前收藏过吗？
        // 根据标题去查数据库 (这里为了演示简单直接在主线程查，实际项目建议用异步)
        Outfit savedOutfit = outfitDao.getOutfitByTitle(outfit.title);
        boolean isFav = (savedOutfit != null);

        // 更新爱心图标状态 (实心红 or 空心灰)
        updateFavoriteIcon(holder.ivFavorite, isFav);

        // 4. 点击爱心图标事件
        holder.ivFavorite.setOnClickListener(v -> {
            // 再次查询最新状态
            Outfit currentCheck = outfitDao.getOutfitByTitle(outfit.title);

            if (currentCheck == null) {
                // 没收藏 -> 执行收藏 (存入数据库)
                outfitDao.insert(outfit);
                updateFavoriteIcon(holder.ivFavorite, true);
                Toast.makeText(mContext, "已收藏 ❤️", Toast.LENGTH_SHORT).show();
            } else {
                // 已收藏 -> 取消收藏 (从数据库删除)
                outfitDao.delete(currentCheck);
                updateFavoriteIcon(holder.ivFavorite, false);
                Toast.makeText(mContext, "取消收藏 💔", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 辅助方法：切换图标显示
    private void updateFavoriteIcon(ImageView iv, boolean isFav) {
        if (isFav) {
            iv.setImageResource(R.drawable.ic_favorite); // 实心红
        } else {
            iv.setImageResource(R.drawable.ic_favorite_border); // 空心灰
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    // 缓存视图类
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