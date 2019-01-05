package com.node.ucmapviewer.Modules;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.node.ucmapviewer.Utils.DialogUtils;

import java.io.File;


public class PlayVideo {
    public static void play(final Context context, final String path) {
        if (new File(path).exists()) {
            Uri uri = Uri.parse(path);
//调用系统自带的播放器
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(uri, "video/mp4");
            ((Activity)context).startActivity(intent);
        }
    }
}
