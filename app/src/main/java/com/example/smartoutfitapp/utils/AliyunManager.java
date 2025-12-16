package com.example.smartoutfitapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AliyunManager {

    // 你的 API Key
    private static final String API_KEY = "sk-d0b21c12251a407992eec66e7cfe12a1";

    // aitryon 专用接口地址
    private static final String URL_SUBMIT = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis/";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface AiCallback {
        void onSuccess(String resultImageUrl);
        void onFail(String error);
    }

    // 1. 提交任务
    public static void submitTryOnTask(String personUrl, String garmentUrl, AiCallback callback) {
        Log.d("AliyunAI", ">>> 1. 准备提交任务");
        Log.d("AliyunAI", "人图: " + personUrl);
        Log.d("AliyunAI", "衣图: " + garmentUrl);

        JSONObject json = new JSONObject();
        try {
            json.put("model", "aitryon");

            JSONObject input = new JSONObject();
            input.put("person_image_url", personUrl);
            input.put("top_garment_url", garmentUrl); // 默认作为上衣/全身
            json.put("input", input);

            JSONObject parameters = new JSONObject();
            parameters.put("resolution", -1);
            parameters.put("restore_face", true);
            json.put("parameters", parameters);

        } catch (JSONException e) {
            callback.onFail("JSON构建失败");
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(URL_SUBMIT)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("X-DashScope-Async", "enable")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("AliyunAI", "提交网络失败: " + e.getMessage());
                callback.onFail("提交网络失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respStr = response.body().string();
                Log.d("AliyunAI", ">>> 2. 提交响应: " + respStr);

                try {
                    JSONObject respJson = new JSONObject(respStr);

                    if (respJson.has("code") && !respJson.isNull("code")) {
                        callback.onFail("提交报错: " + respJson.optString("message"));
                        return;
                    }

                    if (respJson.has("output")) {
                        JSONObject output = respJson.getJSONObject("output");
                        if (output.has("task_id")) {
                            String taskId = output.getString("task_id");
                            Log.d("AliyunAI", ">>> 3. 获取到 TaskID: " + taskId + "，开始轮询...");
                            pollTaskResult(taskId, callback);
                        } else {
                            callback.onFail("未返回 Task ID");
                        }
                    } else {
                        callback.onFail("响应异常: 无output字段");
                    }
                } catch (JSONException e) {
                    callback.onFail("提交解析失败");
                }
            }
        });
    }

    // 2. 轮询结果
    private static void pollTaskResult(String taskId, AiCallback callback) {
        String urlTask = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;

        Request request = new Request.Builder()
                .url(urlTask)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                retryPoll(taskId, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respStr = response.body().string();
                // 开启上帝视角：打印每一次轮询的结果
                Log.d("AliyunAI", ">>> 轮询详情: " + respStr);

                try {
                    JSONObject json = new JSONObject(respStr);
                    if (json.has("output")) {
                        JSONObject output = json.getJSONObject("output");
                        String status = output.optString("task_status", "");

                        if ("SUCCEEDED".equals(status)) {
                            // === 成功！万能解析开始 ===
                            String finalUrl = "";

                            // 尝试方案 A: results 数组
                            if (output.has("results")) {
                                finalUrl = output.getJSONArray("results").getJSONObject(0).getString("url");
                            }
                            // 尝试方案 B: image_url 字段
                            else if (output.has("image_url")) {
                                finalUrl = output.getString("image_url");
                            }
                            // 尝试方案 C: url 字段
                            else if (output.has("url")) {
                                finalUrl = output.getString("url");
                            }

                            if (!finalUrl.isEmpty()) {
                                Log.d("AliyunAI", ">>> 解析成功！图片地址: " + finalUrl);
                                callback.onSuccess(finalUrl);
                            } else {
                                // 如果所有方案都失败，把整个 JSON 给用户看，方便 debug
                                Log.e("AliyunAI", "解析失败，找不到url字段");
                                callback.onFail("未找到图片，服务器返回: " + output.toString());
                            }

                        } else if ("FAILED".equals(status) || "CANCELED".equals(status)) {
                            String msg = output.optString("message", "未知错误");
                            Log.e("AliyunAI", "任务失败: " + msg);
                            callback.onFail("AI处理失败: " + msg);
                        } else {
                            // PENDING 或 RUNNING
                            retryPoll(taskId, callback);
                        }
                    } else {
                        if (json.has("code")) {
                            callback.onFail("查询报错: " + json.optString("message"));
                        } else {
                            retryPoll(taskId, callback);
                        }
                    }
                } catch (JSONException e) {
                    retryPoll(taskId, callback);
                }
            }
        });
    }

    private static void retryPoll(String taskId, AiCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            pollTaskResult(taskId, callback);
        }, 2000);
    }
}