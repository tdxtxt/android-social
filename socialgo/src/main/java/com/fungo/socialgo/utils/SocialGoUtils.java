package com.fungo.socialgo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.fungo.socialgo.SocialSdk;
import com.fungo.socialgo.adapter.IJsonAdapter;
import com.fungo.socialgo.exception.SocialError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import bolts.Continuation;
import bolts.Task;

/**
 * @author Pinger
 * @since 下午3:41 下午3:41
 * <p>
 * 社会化登录分享工具类
 */
public class SocialGoUtils {


    public static final String TAG = SocialGoUtils.class.getSimpleName();




    // -------------------------- 分享相关-------------------------------
    // -------------------------- 分享相关-------------------------------
    // -------------------------- 分享相关-------------------------------

    private static final int SHARE_REQ_CODE = 0x123;

    public static boolean shareText(Activity activity, String title, String text, String pkg, String targetActivity) {
        Intent sendIntent = new Intent();
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);
        sendIntent.setType("text/plain");
        return activeShare(activity, sendIntent, pkg, targetActivity);
    }


    public static boolean shareVideo(Activity activity, String path, String pkg, String targetActivity) {
        //由文件得到uri
        Uri videoUri = Uri.fromFile(new File(path));
        Intent shareIntent = new Intent();
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        // shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        shareIntent.setType("video/*");
        printActivitySupport(activity, shareIntent);
        return activeShare(activity, shareIntent, pkg, targetActivity);
    }


    private static boolean activeShare(Activity activity, Intent sendIntent, String pkg, String targetActivity) {
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!TextUtils.isEmpty(targetActivity))
            sendIntent.setClassName(pkg, targetActivity);
        try {
            Intent chooserIntent = Intent.createChooser(sendIntent, "请选择");
            if (chooserIntent == null) {
                return false;
            }
            activity.startActivityForResult(chooserIntent, SHARE_REQ_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }



    private static void printActivitySupport(Activity activity, Intent intent){
        List<ResolveInfo> resolveInfos = activity.getPackageManager()
                .queryIntentActivities(intent,PackageManager.GET_RESOLVED_FILTER);
        for (ResolveInfo resolveInfo : resolveInfos) {
            SocialLogUtils.e(resolveInfo.activityInfo.packageName + " - " + resolveInfo.activityInfo.name);
        }
    }




    // -------------------------- 图片相关-------------------------------
    // -------------------------- 图片相关-------------------------------
    // -------------------------- 图片相关-------------------------------


    static class Size {

        Size() {
        }

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        int width;
        int height;
    }

    /**
     * 根据kb计算缩放后的大约宽高
     *
     * @param originSize  图片原始宽高
     * @param maxSize byte length
     * @return 大小
     */
    private static Size calculateSize(Size originSize, int maxSize) {
        int bw = originSize.width;
        int bh = originSize.height;
        Size size = new Size();
        // 如果本身已经小于，就直接返回
        if (bw * bh <= maxSize) {
            size.width = bw;
            size.height = bh;
            return size;
        }
        // 拿到大于1的宽高比
        boolean isHeightLong = true;
        float bitRatio = bh * 1f / bw;
        if (bitRatio < 1) {
            bitRatio = bw * 1f / bh;
            isHeightLong = false;
        }
        // 较长边 = 较短边 * 比例(>1)
        // maxSize = 较短边 * 较长边 = 较短边 * 较短边 * 比例(>1)
        // 由此计算短边应该为 较短边 = sqrt(maxSize/比例(>1))
        int thumbShort = (int) Math.sqrt(maxSize / bitRatio);
        // 较长边 = 较短边 * 比例(>1)
        int thumbLong = (int) (thumbShort * bitRatio);
        if (isHeightLong) {
            size.height = thumbLong;
            size.width = thumbShort;
        } else {
            size.width = thumbLong;
            size.height = thumbShort;
        }
        return size;
    }

    /**
     * 获取图片大小
     * @param filePath 路径
     * @return Size
     */
    private static Size getBitmapSize(String filePath) {
        // 仅获取宽高
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 该属性设置为 true 只会加载图片的边框进来，并不会加载图片具体的像素点
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // 获得原图的宽和高
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        return new Size(outWidth, outHeight);
    }

    /**
     * 使用 path decode 出来一个差不多大小的，此时因为图片质量的关系，可能大于kbNum
     *
     * @param filePath path
     * @param maxSize  byte
     * @return bitmap
     */
    private static Bitmap getMaxSizeBitmap(String filePath, int maxSize) {
        Size originSize = getBitmapSize(filePath);
        SocialLogUtils.e(TAG, "原始图片大小 = " + originSize.width + " * " + originSize.height);
        int sampleSize = 0;
        // 我们对较小的图片不进行采样，因为采样只是尽量接近 32k 和避免占用大量内存
        // 对较小图片进行采样会导致图片更模糊，所以对不大的图片，直接走后面的细节调整
        if (originSize.height * originSize.width < 400 * 400) {
            sampleSize = 1;
        } else {
            Size size = calculateSize(originSize, maxSize * 5);
            SocialLogUtils.e(TAG, "目标图片大小 = " + size.width + " * " + size.height);
            while (sampleSize == 0
                    || originSize.height / sampleSize > size.height
                    || originSize.width / sampleSize > size.width) {
                sampleSize += 2;
            }
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        SocialLogUtils.e(TAG, "sample size = " + sampleSize + " 采样后 bitmap大小 = " + bitmap.getByteCount());
        return bitmap;
    }


    /**
     * 创建指定大小的bitmap的byte流，大小 <= maxSize
     *
     * @param srcBitmap bitmap
     * @param maxSize   kb,example 32kb
     * @return byte流
     */
    private static byte[] getStaticSizeBitmapByteByBitmap(Bitmap srcBitmap, int maxSize, Bitmap.CompressFormat format) {
        // 首先进行一次大范围的压缩
        Bitmap tempBitmap;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // 设置矩阵数据
        Matrix matrix = new Matrix();
        srcBitmap.compress(format, 100, output);
        // 如果进行了上面的压缩后，依旧大于32K，就进行小范围的微调压缩
        byte[] bytes = output.toByteArray();
        SocialLogUtils.e(TAG, "开始循环压缩之前 bytes = " + bytes.length);
        while (bytes.length > maxSize) {
            matrix.setScale(0.9f, 0.9f);//每次缩小 1/10
            tempBitmap = srcBitmap;
            srcBitmap = Bitmap.createBitmap(
                    tempBitmap, 0, 0,
                    tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
            recyclerBitmaps(tempBitmap);
            output.reset();
            srcBitmap.compress(format, 100, output);
            bytes = output.toByteArray();
            SocialLogUtils.e(TAG, "压缩一次 bytes = " + bytes.length);
        }
        SocialLogUtils.e(TAG, "压缩后的图片输出大小 bytes = " + bytes.length);
        recyclerBitmaps(srcBitmap);
        return bytes;
    }

    /**
     * 根据路径获取指定大小的图片
     * @param path 路径
     * @param maxSize 最大尺寸
     * @return byte[]
     */
    public static byte[] getStaticSizeBitmapByteByPath(final String path, final int maxSize) {
        Bitmap srcBitmap = getMaxSizeBitmap(path, maxSize);
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if (isPngFile(path)) format = Bitmap.CompressFormat.PNG;
        return getStaticSizeBitmapByteByBitmap(srcBitmap, maxSize, format);
    }

    public static Task<byte[]> getStaticSizeBitmapByteByPathTask(final String path, final int maxSize) {
        return Task.callInBackground(new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return getStaticSizeBitmapByteByPath(path, maxSize);
            }
        });
    }

    public static void recyclerBitmaps(Bitmap... bitmaps) {
        try {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    // -------------------------- 文件相关-------------------------------
    // -------------------------- 文件相关-------------------------------
    // -------------------------- 文件相关-------------------------------

    public static final String POINT_GIF = ".gif";
    public static final String POINT_JPG = ".jpg";
    public static final String POINT_JPEG = ".jpeg";
    public static final String POINT_PNG = ".png";

    /**
     * 文件后缀
     *
     * @param path 路径
     * @return 后缀名
     */
    private static String getSuffix(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lineIndex = path.lastIndexOf("/");
            if (lineIndex != -1) {
                String fileName = path.substring(lineIndex, path.length());
                if (!TextUtils.isEmpty(fileName)) {
                    int pointIndex = fileName.lastIndexOf(".");
                    if (pointIndex != -1) {
                        String suffix = fileName.substring(pointIndex, fileName.length());
                        if (!TextUtils.isEmpty(suffix)) {
                            return suffix;
                        }
                    }

                }
            }
        }
        return "";
    }

    /**
     * @param path 路径
     * @return 是否是 gif 文件
     */
    public static boolean isGifFile(String path) {
        return path.toLowerCase().endsWith(POINT_GIF);
    }

    /**
     * @param path 路径
     * @return 是不是 jpg || png
     */
    private static boolean isJpgPngFile(String path) {
        return isJpgFile(path) || isPngFile(path);
    }

    /**
     * @param path 路径
     * @return 是不是 jpg 文件
     */
    private static boolean isJpgFile(String path) {
        return path.toLowerCase().endsWith(POINT_JPG) || path.toLowerCase().endsWith(POINT_JPEG);
    }

    /**
     * @param path 路径
     * @return 是不是 png 文件
     */
    public static boolean isPngFile(String path) {
        return path.toLowerCase().endsWith(POINT_PNG);
    }

    /**
     * @param path 路径
     * @return 是不是 图片 文件
     */
    public static boolean isPicFile(String path) {
        return isJpgPngFile(path) || isGifFile(path);
    }

    /**
     * @param path 路径
     * @return 文件是否存在
     */
    public static boolean isExist(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        return file.exists() && file.length() > 0;
    }

    /**
     * @param file 文件
     * @return 文件是否存在
     */
    public static boolean isExist(File file) {
        return file != null && isExist(file.getAbsolutePath());
    }

    /**
     * @param path 路径
     * @return 是不是 http 路径
     */
    public static boolean isHttpPath(String path) {
        return path.toLowerCase().startsWith("http");
    }

    /**
     * 网络路径映射本地路径
     *
     * @param url 网络路径
     * @return 映射的本地路径
     */
    public static String mapUrl2LocalPath(String url) {
        // 映射文件名
        String suffix = getSuffix(url);
        suffix = TextUtils.isEmpty(suffix) ? ".png" : suffix;
        String fileName = getMD5(url) + suffix;
        File saveFile = new File(SocialSdk.getConfig().getCacheDir(), fileName);
        return saveFile.getAbsolutePath();
    }


    /**
     * 将资源图片映射到本地文件存储，同一张图片不必重复decode
     *
     * @param context ctx
     * @param resId   资源ID
     * @return 路径
     */
    public static String mapResId2LocalPath(Context context, int resId) {
        String fileName = getMD5(resId + "") + POINT_PNG;
        File saveFile = new File(SocialSdk.getConfig().getCacheDir(), fileName);
        if (saveFile.exists())
            return saveFile.getAbsolutePath();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(saveFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            SocialGoUtils.recyclerBitmaps(bitmap);
        }
        return saveFile.getAbsolutePath();
    }




    // -------------------------- 数据流相关-------------------------------
    // -------------------------- 数据流相关-------------------------------
    // -------------------------- 数据流相关-------------------------------


    // 关闭流
    public static void closeStream(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 打开一个网络流
     *
     * @param conn 网络连接
     * @return 流
     * @throws IOException error
     */
    public static InputStream openGetHttpStream(HttpURLConnection conn) throws IOException {
        conn.setRequestMethod("GET");
        conn.setReadTimeout(3_000);
        conn.setConnectTimeout(3_000);
        conn.setDoInput(true);
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("connection", "Keep-Alive");
        // 发起连接
        conn.connect();
        return conn.getInputStream();
    }


    /**
     * 保存文件到
     *
     * @param file 文件
     * @param is   流
     * @return 文件
     */
    public static File saveStreamToFile(File file, InputStream is) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        byte[] bs;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            bis = new BufferedInputStream(is);
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bs = new byte[1024 * 10];
            int len;
            while ((len = bis.read(bs)) != -1) {
                bos.write(bs, 0, len);
                bos.flush();
            }
            bis.close();
            bos.close();
        } catch (Exception e) {
            SocialLogUtils.t(e);
            return null;
        } finally {
            closeStream(bis, bos);
            bs = null;
        }
        return file;
    }


    /**
     * 从流中读取为字符串
     * @param is 流
     * @return json
     */
    public static String saveStreamToString(InputStream is) {
        BufferedReader br = null;
        String json = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            json = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream(br);
        }
        return json;
    }




    // -------------------------- Json相关-------------------------------
    // -------------------------- Json相关-------------------------------
    // -------------------------- Json相关-------------------------------


    public interface Callback<T> {

        void onSuccess(@NonNull T object);

        void onFailure(SocialError e);
    }

    public static <T> T getObject(String jsonString, Class<T> cls) {
        IJsonAdapter jsonAdapter = SocialSdk.getJsonAdapter();
        if (jsonAdapter != null) {
            try {
                return jsonAdapter.toObj(jsonString, cls);
            } catch (Exception e) {
                SocialLogUtils.e(TAG, e);
            }
        }
        return null;
    }

    public static String getObject2Json(Object object) {
        IJsonAdapter jsonAdapter = SocialSdk.getJsonAdapter();
        try {
            return jsonAdapter.toJson(object);
        } catch (Exception e) {
            SocialLogUtils.e(TAG, e);
        }
        return null;
    }


    public static <T> void startJsonRequest(final String url, final Class<T> clz, final Callback<T> callback) {
        Task.callInBackground(new Callable<T>() {
            @Override
            public T call() {
                T object = null;
                String json = SocialSdk.getRequestAdapter().getJson(url);
                if (!TextUtils.isEmpty(json)) {
                    object = getObject(json, clz);
                }
                return object;
            }
        }).continueWith(new Continuation<T, Boolean>() {
            @Override
            public Boolean then(Task<T> task) throws Exception {
                if (!task.isFaulted() && task.getResult() != null) {
                    callback.onSuccess(task.getResult());
                } else if (task.isFaulted()) {
                    callback.onFailure(new SocialError(SocialError.CODE_REQUEST_ERROR, task.getError()));
                } else if (task.getResult() == null) {
                    callback.onFailure(new SocialError(SocialError.CODE_PARSE_ERROR, "json 无法解析"));
                } else {
                    callback.onFailure(new SocialError(SocialError.CODE_REQUEST_ERROR, "unKnow error"));
                }
                return true;
            }
        }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if (task.isFaulted()) {
                    callback.onFailure(new SocialError(SocialError.CODE_REQUEST_ERROR, "未 handle 的错误"));
                }
                return null;
            }
        });
    }




    public static boolean hasPermission(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    // 任何一个为空 返回true
    public static boolean isAnyEmpty(String... strings) {
        boolean isEmpty = false;
        for (String string : strings) {
            if (TextUtils.isEmpty(string)) {
                isEmpty = true;
                break;
            }
        }
        return isEmpty;
    }

    // app 是否安装
    public static boolean isAppInstall(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        boolean result = false;
        for (PackageInfo info : packages) {
            if (TextUtils.equals(info.packageName.toLowerCase(), pkgName)) {
                result = true;
                break;
            }
        }
        return result;
    }

    // 根据包名，打开对应app
    public static boolean openApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // 获取 md5
    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();
            StringBuilder strBuf = new StringBuilder();
            for (byte anEncryption : encryption) {
                if (Integer.toHexString(0xff & anEncryption).length() == 1) {
                    strBuf.append("0").append(Integer.toHexString(0xff & anEncryption));
                } else {
                    strBuf.append(Integer.toHexString(0xff & anEncryption));
                }
            }
            return strBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }


}
