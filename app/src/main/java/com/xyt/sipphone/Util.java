package com.xyt.sipphone;

import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.UI_MODE_SERVICE;

/**
 * Created by zhangyangjing on 01/02/2018.
 */

public class Util {

    public static void copyAssets(Context ctx, String src, String dest) {
        AssetManager assetManager = ctx.getAssets();
        try {
            String assets[] = assetManager.list(src);
            if (assets.length == 0) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(src);
                    out = new FileOutputStream(dest);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1)
                        out.write(buffer, 0, read);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    close(in);
                    close(out);
                }
            } else {
                File dir = new File(dest);
                if (!dir.exists())
                    dir.mkdirs();
                for (String name : assets)
                    copyAssets(ctx, src + File.separator +  name, dest  + File.separator + name);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void close(Closeable closeable) {
        try {
            if (null != closeable && closeable instanceof Closeable)
                closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTvPlatform(Context ctx) {
        PackageManager packageManager = ctx.getPackageManager();
        UiModeManager uiModeManager = (UiModeManager) ctx.getSystemService(UI_MODE_SERVICE);
        boolean isTv =  uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
        boolean touchDisabled = !packageManager.hasSystemFeature("android.hardware.touchscreen");
        return isTv || touchDisabled;
    }

    public static String getBaresipConfigPath(Context ctx) {
        return new File(ctx.getFilesDir(), "baresip_config").getAbsolutePath();
    }
}
