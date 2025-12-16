package com.example.smartoutfitapp.ui.tryon;

import android.net.Uri;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.smartoutfitapp.model.Outfit;

public class TryOnViewModel extends ViewModel {
    // 1. 用户上传的底图 Uri
    public MutableLiveData<Uri> userPhotoUri = new MutableLiveData<>();

    // 2. 用户选中的衣服
    public MutableLiveData<Outfit> selectedOutfit = new MutableLiveData<>();

    // 3. AI 生成后的结果图 URL (记忆生成结果)
    public MutableLiveData<String> resultImageUrl = new MutableLiveData<>();
}