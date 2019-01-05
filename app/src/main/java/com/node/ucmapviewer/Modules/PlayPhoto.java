package com.node.ucmapviewer.Modules;


import android.app.AlertDialog;
import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.node.ucmapviewer.Modules.subscaleview.ImageSource;
import com.node.ucmapviewer.Modules.subscaleview.SubsamplingScaleImageView;
import com.node.ucmapviewer.R;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public class PlayPhoto {

    public static void show(final Context context, String path) {


        if (new File(path).exists()) {
            final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
            final View digView = LayoutInflater.from(context).inflate(R.layout.showphoto, null);
            SubsamplingScaleImageView ssiv = (SubsamplingScaleImageView) digView.findViewById(R.id.ssiv_showphoto);
            ssiv.setMaxScale(5f);
            ssiv.setImage(ImageSource.uri(Uri.fromFile(new File(path))));
            ssiv.setRotation(ssiv.getRotation() + getBitmapDegree(path));
            opacityDialog.setView(digView);
            opacityDialog.setTitle("照片");
            opacityDialog.show();

        }
    }

    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
        }
        return degree;
    }
}
