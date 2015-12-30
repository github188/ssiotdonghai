
package com.ssiot.donghai;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.WeakHashMap;

//http://my.oschina.net/ryanhoo/blog/93406
public class MyCache {
    private static final String TAG = "MyCache";
    private File cacheDir;

    // 弱引用
    private WeakHashMap<String, Object> cache = new WeakHashMap<String, Object>();

    public MyCache(Context context) {
        if (android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    SsiotConfig.CACHE_DIR);
        }
        else {
            cacheDir = context.getCacheDir();
            Log.v(TAG, "can not find sdcard----!!!!");
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        Log.d(TAG, "cache dir: " + cacheDir.getAbsolutePath());
    }

    public Object get(String key) {
        if (key != null) {
            return cache.get(key);
        }
        return null;
    }

    public void put(String key, Object value) {
        if (key != null && !"".equals(key) && value != null) {
            cache.put(key, value);
            // Log.i(TAG, "cache bitmap: " + key);
            Log.d(TAG, "size of memory cache: " + cache.size());
        }
    }

    public void clear() {
        cache.clear();
    }

    // ------------------------------------------
    public File getFile(String key) {
        File f = new File(cacheDir, key);
        if (f.exists()) {
            Log.i(TAG, "the file you wanted exists " + f.getAbsolutePath());
            return f;
        } else {
            Log.w(TAG, "the file you wanted does not exists: " + f.getAbsolutePath());
        }

        return null;
    }

    /**
     * Put a bitmap into cache with a unique key.
     * 
     * @param key Should be unique.
     * @param value A bitmap.
     */
    public void putHardCache(String key, Object value) {
        File f = new File(cacheDir, key);
        if (!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        // Use the util's function to save the bitmap.
        if (saveData(f, value)) {
            Log.d(TAG, "Save file to sdcard successfully!");
        } else {
            Log.e(TAG, "Save file to sdcard failed!!!!");
        }
    }

    /**
     * Clear the cache directory on sdcard.
     */
    public void clearHardCache() {
        File[] files = cacheDir.listFiles();
        for (File f : files)
            f.delete();
    }
    
    private boolean saveData(File f, Object data){
        if (null == f || null == data){
            return false;
        } else {
            try {
                String str = (String) data;
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));//TODO byte
                out.write(str.getBytes(), 0, str.length());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
