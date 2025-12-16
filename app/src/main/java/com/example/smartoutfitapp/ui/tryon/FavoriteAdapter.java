package com.example.smartoutfitapp.ui.tryon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.model.Outfit;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private Context context;
    private List<Outfit> list;
    private int selectedPosition = -1;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        // 注意：这里的 outfit 可能为 null (表示取消选中)
        void onItemClick(Outfit outfit);
    }

    public FavoriteAdapter(Context context, List<Outfit> list, OnItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void setSelectedOutfit(Outfit outfit) {
        if (outfit == null) {
            selectedPosition = -1;
        } else {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id == outfit.id) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Outfit outfit = list.get(position);
        Glide.with(context).load(outfit.imageUrl).into(holder.ivImage);

        // UI显示状态
        if (selectedPosition == position) {
            holder.viewSelected.setVisibility(View.VISIBLE);
        } else {
            holder.viewSelected.setVisibility(View.GONE);
        }

        // 点击事件 (核心修改逻辑)
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();

            if (selectedPosition == currentPos) {
                // 情况A：点击了“已经选中”的项 -> 取消选中
                selectedPosition = -1;
                notifyItemChanged(currentPos); // 刷新当前项(去边框)

                if (listener != null) {
                    listener.onItemClick(null); // 传回 null，表示取消
                }
            } else {
                // 情况B：点击了“没选中”的项 -> 选中它
                int previous = selectedPosition;
                selectedPosition = currentPos;

                notifyItemChanged(previous);   // 刷新旧的(去边框)
                notifyItemChanged(currentPos); // 刷新新的(加边框)

                if (listener != null) {
                    listener.onItemClick(outfit); // 传回当前衣服
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        View viewSelected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_outfit_small);
            viewSelected = itemView.findViewById(R.id.view_selected);
        }
    }
}