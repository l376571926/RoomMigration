package group.tonight.firimsdk;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

/**
 * 示例用法
 * Author：376571926
 * Date: 2019/1/16 0016 10:58
 */
public class SampleUpdateDialog extends DialogFragment {

    private String mAppTag;

    public static SampleUpdateDialog getInstance() {
        return getInstance(null);
    }

    public static SampleUpdateDialog getInstance(String appTag) {
        SampleUpdateDialog dialog = new SampleUpdateDialog();

        Bundle bundle = new Bundle();
        if (appTag != null && appTag.length() == 4) {
            bundle.putString("data", appTag);
        }

        dialog.setArguments(bundle);

        return dialog;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAppTag = getArguments().getString("data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            } else {
                checkUpdate();
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void checkUpdate() {
        final Context context = getContext();
        FirIm.get().checkUpdateInfo(mAppTag == null ? "1r7z" : mAppTag, new FirIm.OnUpdateBeanGetListener() {
            @Override
            public void OnUpdateBeanGet(final UpdateBean updateBean) {
                dismiss();
                if (updateBean == null) {
                    return;
                }
                if (context == null) {
                    return;
                }
                if (!FirIm.hasNewVersion(context, updateBean)) {
                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(context)
                        .setTitle("发现新版本")
                        .setMessage(FirIm.getFormatUpdateMessage(updateBean))
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                                progressDialog.show();
                                progressDialog.setMax(100);
                                FirIm.get().downloadApk(updateBean.getApkUrl(), new FirIm.OnDownloadProgressListener() {
                                    @Override
                                    public void downloadProgress(float progress) {
                                        progressDialog.setProgress(((int) (progress * 100)));
                                        if (progress == 1) {
                                            progressDialog.dismiss();
                                            String apkFilePath = Environment.getExternalStorageDirectory() + File.separator + "/update.apk";
                                            FirIm.installApk(context, apkFilePath);
                                        }
                                    }
                                });
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println();
        boolean hasPermission = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        if (hasPermission) {
            checkUpdate();
        }
    }
}
