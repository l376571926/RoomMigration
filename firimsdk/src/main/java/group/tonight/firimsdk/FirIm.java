package group.tonight.firimsdk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Author：376571926
 * Date: 2019/1/15 0015 17:16
 */
public class FirIm {
    private static FirIm INSTANCE;
    private OkHttpClient okHttpClient;
    private Handler handler;

    public static FirIm get() {
        if (INSTANCE == null) {
            INSTANCE = new FirIm();
        }
        return INSTANCE;
    }

    public static FirIm get(OkHttpClient okHttpClient) {
        if (INSTANCE == null) {
            INSTANCE = new FirIm(okHttpClient);
        }
        return INSTANCE;
    }

    private FirIm() {
        this(null);
    }

    private FirIm(OkHttpClient okHttpClient) {
        if (okHttpClient == null) {
            this.okHttpClient = new OkHttpClient();
        } else {
            this.okHttpClient = okHttpClient;
        }
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 获取最新版本信息
     *
     * @param appTag   app信息，https://fir.im/96ln中的96ln
     * @param listener
     */
    public void checkUpdateInfo(String appTag, final OnUpdateBeanGetListener listener) {
        Request request = new Request.Builder()
                .url("https://download.fir.im/" + appTag)
                .build();
        this.okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String json = response.body().string();

                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            JSONObject app = jsonObject.getJSONObject("app");
                            String id = app.getString("id");
                            String download_token = app.getString("token");

                            JSONObject master = app.getJSONObject("releases").getJSONObject("master");
                            final String release_id = master.getString("id");
                            final String version = master.getString("version");
                            final int build = master.getInt("build");
                            final String changelog = master.getString("changelog");
                            final long fsize = master.getLong("fsize");
                            final long created_at = master.getLong("created_at");

                            FormBody body = new FormBody.Builder()
                                    .add("download_token", download_token)
                                    .add("release_id", release_id)
                                    .build();
                            Request request1 = new Request.Builder()
                                    .url("https://download.fir.im/apps/" + id + "/install")
                                    .post(body)
                                    .build();
                            okHttpClient.newCall(request1).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    System.out.println(e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            String json = response.body().string();

                                            try {
                                                JSONObject jsonObject1 = new JSONObject(json);
                                                final String apkUrl = jsonObject1.getString("url");
                                                System.out.println("apk下载地址：" + apkUrl);

                                                final UpdateBean updateBean = new UpdateBean();
                                                updateBean.setId(release_id);
                                                updateBean.setVersion(version);
                                                updateBean.setBuild(build);
                                                updateBean.setChangelog(changelog);
                                                updateBean.setFsize(fsize);
                                                updateBean.setCreated_at(created_at);
                                                updateBean.setApkUrl(apkUrl);

                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (listener != null) {
                                                            listener.OnUpdateBeanGet(updateBean);
                                                        }
                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        });
    }

    public void downloadApk(String apkUrl, final OnDownloadProgressListener listener) {
        Request request = new Request.Builder()
                .url(apkUrl)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        final long contentLength = responseBody.contentLength();
                        MediaType mediaType = responseBody.contentType();
                        InputStream inputStream = responseBody.byteStream();

                        FileOutputStream fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "/update.apk");
                        byte[] buf = new byte[1024];
                        int ch;
                        int process = 0;
                        while ((ch = inputStream.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            process += ch;
                            final int finalProcess = process;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (listener != null) {
                                        listener.downloadProgress(finalProcess * 1.0f / contentLength);
                                    }
                                }
                            });
                        }
                        fileOutputStream.close();
                        inputStream.close();
                    }
                }
            }
        });

    }

    /**
     * 安装apk
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("无存储卡写入权限");
            return;
        }
        try {
            /**
             * provider
             * 处理android 7.0 及以上系统安装异常问题
             */
            File file = new File(apkPath);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".firim.FileProvider", file);//在AndroidManifest中的android:authorities值
                Log.d("======", "apkUri=" + apkUri);
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
                install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            context.startActivity(install);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("======", e.getMessage());
        }
    }

    public static boolean hasNewVersion(Context context, UpdateBean updateBean) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        String versionName = packageInfo.versionName;
        int versionCode = packageInfo.versionCode;

        String version = updateBean.getVersion();
        long build = updateBean.getBuild();

        return !versionName.equals(version) || versionCode != build;
    }

    /**
     * 格式化版本信息，用于版本信息弹窗
     *
     * @param updateBean
     * @return
     */
    public static String getFormatUpdateMessage(UpdateBean updateBean) {
        String version = updateBean.getVersion();
        long build = updateBean.getBuild();
        String changelog = updateBean.getChangelog();
        long created_at = updateBean.getCreated_at();

        DecimalFormat format = new DecimalFormat("0.00");
        double fileSize = updateBean.getFsize() / 1024.0f / 1024.f;

        return "版本号：" +
                build +
                "\n" +
                "版本名称：" +
                version +
                "\n" +
                "Apk大小：" +
                format.format(fileSize) + "MB" +
                "\n" +
                "发布时间：" +
                "\n" +
                DateFormat.getDateTimeInstance().format(new Date(created_at * 1000)) +
                "\n" +
                "\n" +
                "更新内容：" +
                "\n" +
                changelog;
    }

    public interface OnUpdateBeanGetListener {
        void OnUpdateBeanGet(UpdateBean updateBean);
    }

    public interface OnDownloadProgressListener {
        void downloadProgress(float progress);
    }
}
