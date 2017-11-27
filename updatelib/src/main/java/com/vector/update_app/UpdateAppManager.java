package com.vector.update_app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.vector.update_app.service.DownloadService;
import com.vector.update_app.utils.AppUpdateUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 版本更新管理器
 */
public class UpdateAppManager {
    private Map<String, String> mParams;
    private Activity mActivity;
    private HttpManager mHttpManager;
    private String mUpdateUrl;
    private String mAppKey;
    private UpdateAppBean mUpdateApp;
    private String mTargetPath;
    private boolean isPost;
    //自定义参数

    private UpdateAppManager(Builder builder) {
        mActivity = builder.getActivity();
        mHttpManager = builder.getHttpManager();
        mUpdateUrl = builder.getUpdateUrl();
        mTargetPath = builder.getTargetPath();
        isPost = builder.isPost();
        mParams = builder.getParams();
    }


    /**
     * 检测是否有新版本
     *
     * @param callback 更新回调
     */
    public void checkNewApp(final UpdateCallback callback) {
        if (callback == null) {
            return;
        }
        callback.onBefore();

        if (DownloadService.isRunning) {
            callback.onAfter();
            Toast.makeText(mActivity, "app正在更新", Toast.LENGTH_SHORT).show();
            return;
        }

        //拼接参数
        Map<String, String> params = new HashMap<>();

        params.put("appKey", mAppKey);
        String versionName = AppUpdateUtils.getVersionName(mActivity);
        if (versionName.endsWith("-debug")) {
            versionName = versionName.substring(0, versionName.lastIndexOf('-'));
        }
        params.put("version", versionName);


        //添加自定义参数，其实可以实现HttManager中添加
        if (mParams != null && !mParams.isEmpty()) {
            //清空，那就使用自定参数
            params.clear();
            params.putAll(mParams);
        }

        //网络请求
        if (isPost) {
            mHttpManager.asyncPost(mUpdateUrl, params, new HttpManager.Callback() {
                @Override
                public void onResponse(String result) {
                    callback.onAfter();
                    if (result != null) {
                        processData(result, callback);
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onAfter();
                    callback.noNewApp();
                }
            });
        } else {
            mHttpManager.asyncGet(mUpdateUrl, params, new HttpManager.Callback() {
                @Override
                public void onResponse(String result) {
                    callback.onAfter();
                    if (result != null) {
                        processData(result, callback);
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onAfter();
                    callback.noNewApp();
                }
            });
        }
    }

    /**
     * 后台下载
     *
     * @param downloadCallback 后台下载回调
     */
    public void download(@Nullable final DownloadService.DownloadCallback downloadCallback) {
        if (mUpdateApp == null) {
            throw new NullPointerException("updateApp 不能为空");
        }
        mUpdateApp.setTargetPath(mTargetPath);
        mUpdateApp.setHttpManager(mHttpManager);
        DownloadService.bindService(mActivity.getApplicationContext(), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ((DownloadService.DownloadBinder) service).start(mUpdateApp, downloadCallback);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
    }

    /**
     * 后台下载
     */
    public void download() {
        download(null);
    }

    /**
     * 解析
     *
     * @param result
     * @param callback
     */
    private void processData(String result, @NonNull UpdateCallback callback) {
        try {
            mUpdateApp = callback.parseJson(result);
            if (mUpdateApp.isUpdate()) {
                callback.hasNewApp(mUpdateApp, this);
                //假如是静默下载，可能需要判断，
                //是否wifi,
                //是否已经下载，如果已经下载直接提示安装
                //没有则进行下载，监听下载完成，弹出安装对话框

            } else {
                callback.noNewApp();
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
            callback.noNewApp();
        }
    }


    public static class Builder {
        //必须有
        private Activity mActivity;
        //必须有
        private HttpManager mHttpManager;
        //必须有
        private String mUpdateUrl;

        //4,apk的下载路径
        private String mTargetPath;
        //5,是否是post请求，默认是get
        private boolean isPost;
        //6,自定义参数
        private Map<String, String> params;

        public Map<String, String> getParams() {
            return params;
        }

        /**
         * 自定义请求参数
         *
         * @param params 自定义请求参数
         * @return Builder
         */
        public Builder setParams(Map<String, String> params) {
            this.params = params;
            return this;
        }

        public boolean isPost() {
            return isPost;
        }

        /**
         * 是否是post请求，默认是get
         *
         * @param post 是否是post请求，默认是get
         * @return Builder
         */
        public Builder setPost(boolean post) {
            isPost = post;
            return this;
        }

        public String getTargetPath() {
            return mTargetPath;
        }

        /**
         * apk的下载路径，
         *
         * @param targetPath apk的下载路径，
         * @return Builder
         */
        public Builder setTargetPath(String targetPath) {
            mTargetPath = targetPath;
            return this;
        }


        public Activity getActivity() {
            return mActivity;
        }

        /**
         * 是否是post请求，默认是get
         *
         * @param activity 当前提示的Activity
         * @return Builder
         */
        public Builder setActivity(Activity activity) {
            mActivity = activity;
            return this;
        }

        public HttpManager getHttpManager() {
            return mHttpManager;
        }

        /**
         * 设置网络工具
         *
         * @param httpManager 自己实现的网络对象
         * @return Builder
         */
        public Builder setHttpManager(HttpManager httpManager) {
            mHttpManager = httpManager;
            return this;
        }

        public String getUpdateUrl() {
            return mUpdateUrl;
        }

        /**
         * 更新地址
         *
         * @param updateUrl 更新地址
         * @return Builder
         */
        public Builder setUpdateUrl(String updateUrl) {
            mUpdateUrl = updateUrl;
            return this;
        }

        /**
         * @return 生成app管理器
         */
        public UpdateAppManager build() {
            //校验
            if (getActivity() == null || getHttpManager() == null || TextUtils.isEmpty(getUpdateUrl())) {
                throw new NullPointerException("必要参数不能为空");
            }
            if (TextUtils.isEmpty(getTargetPath())) {
                //sd卡是否存在
                String path = "";
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageRemovable()) {
                    try {
                        path = getActivity().getExternalCacheDir().getAbsolutePath();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (TextUtils.isEmpty(path)) {
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    }
                } else {
                    path = getActivity().getCacheDir().getAbsolutePath();
                }
                setTargetPath(path);
            }
            return new UpdateAppManager(this);
        }
    }

}
