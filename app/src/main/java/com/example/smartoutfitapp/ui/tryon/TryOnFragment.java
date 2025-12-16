package com.example.smartoutfitapp.ui.tryon;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartoutfitapp.R;
import com.example.smartoutfitapp.db.AppDatabase;
import com.example.smartoutfitapp.model.Outfit;
import com.example.smartoutfitapp.utils.AliyunManager;

import java.util.ArrayList;
import java.util.List;

public class TryOnFragment extends Fragment {

    private ImageView ivUserPhoto;   // 左边：底图
    private ImageView ivResultPhoto; // 右边：结果图 【新增】
    private TextView tvUploadHint;
    private TextView tvResultHint;   // 右边文字提示 【新增】
    private RecyclerView rvFavorites;
    private Button btnStart;
    private ProgressBar loadingBar;

    private TryOnViewModel viewModel;
    private FavoriteAdapter adapter;
    private List<Outfit> favoriteList = new ArrayList<>();

    // 阿里云官方示例文档的模特图 (必胜链接)
    private static final String TEST_PERSON_URL = "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20250626/ubznva/model_person.png";

    // 1. 相册选择回调
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    viewModel.userPhotoUri.setValue(uri);
                    viewModel.resultImageUrl.setValue(null); // 清空旧结果
                }
            }
    );

    // 2. 相机拍照回调
    private final ActivityResultLauncher<Void> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    ivUserPhoto.setImageBitmap(bitmap);
                    tvUploadHint.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "照片已更新", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_try_on, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(TryOnViewModel.class);

        // 绑定控件
        ivUserPhoto = view.findViewById(R.id.iv_user_photo);
        ivResultPhoto = view.findViewById(R.id.iv_result_photo); // 右边图片
        tvUploadHint = view.findViewById(R.id.tv_upload_hint);
        tvResultHint = view.findViewById(R.id.tv_result_hint);   // 右边提示

        rvFavorites = view.findViewById(R.id.rv_favorites);
        btnStart = view.findViewById(R.id.btn_start_tryon);
        loadingBar = view.findViewById(R.id.loading_bar);

        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 1. 观察底图 (左边)
        viewModel.userPhotoUri.observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                Glide.with(this).load(uri).into(ivUserPhoto);
                tvUploadHint.setVisibility(View.GONE);
            }
        });

        // 2. 观察 AI 结果 (右边)
        viewModel.resultImageUrl.observe(getViewLifecycleOwner(), url -> {
            if (url != null) {
                // 有结果了，加载到右边
                Glide.with(this).load(url).into(ivResultPhoto);
                tvResultHint.setVisibility(View.GONE); // 隐藏提示字
                btnStart.setText("试衣完成！点击再来一次");
            } else {
                // 没结果(比如刚换了底图)，重置右边
                ivResultPhoto.setImageDrawable(null);
                tvResultHint.setVisibility(View.VISIBLE);
                btnStart.setText("开始 AI 换装");
            }
        });

        // 点击左边 -> 选图
        view.findViewById(R.id.card_user_photo).setOnClickListener(v -> showImageSourceDialog());

        // 点击右边 -> 也可以看大图 (可选功能，暂不加，保持简单)

        btnStart.setOnClickListener(v -> startAiTryOn());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        favoriteList = AppDatabase.getDatabase(getContext()).outfitDao().getAllFavorites();
        adapter = new FavoriteAdapter(getContext(), favoriteList, outfit -> {
            viewModel.selectedOutfit.setValue(outfit);
        });
        rvFavorites.setAdapter(adapter);

        viewModel.selectedOutfit.observe(getViewLifecycleOwner(), outfit -> {
            if (adapter != null) adapter.setSelectedOutfit(outfit);
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"拍摄新照片", "从相册替换"};
        new AlertDialog.Builder(getContext())
                .setTitle("更换底图")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) takePhotoLauncher.launch(null);
                    else pickImageLauncher.launch("image/*");
                })
                .show();
    }

    private void startAiTryOn() {
        Uri currentUri = viewModel.userPhotoUri.getValue();
        Outfit currentOutfit = viewModel.selectedOutfit.getValue();

        if (currentUri == null && ivUserPhoto.getDrawable() == null) {
            Toast.makeText(getContext(), "请先上传一张照片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentOutfit == null) {
            Toast.makeText(getContext(), "请选择一件衣服", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        btnStart.setEnabled(false);
        btnStart.setText("AI 生成中 (约15秒)...");

        AliyunManager.submitTryOnTask(TEST_PERSON_URL, currentOutfit.imageUrl, new AliyunManager.AiCallback() {
            @Override
            public void onSuccess(String resultImageUrl) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    btnStart.setEnabled(true);

                    // 【核心修改】成功后，更新 ViewModel，观察者会自动更新右边的图片
                    viewModel.resultImageUrl.setValue(resultImageUrl);

                    Toast.makeText(getContext(), "生成成功！请看右图", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFail(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    btnStart.setEnabled(true);
                    btnStart.setText("重试");
                    Toast.makeText(getContext(), "AI 错误: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}