package com.node.ucmapviewer.Modules;


import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.node.ucmapviewer.R;

import java.io.File;
import java.io.IOException;


public class PlayVoice {
    private static int flag = 0;
    private static Handler handler;
    private static Runnable runnable_play;
    private static MediaPlayer mp;

    /**
     * 播放视频
     * path：录音文件路径
     * isDelete：是否显示删除按钮
     * s：点击删除按钮后执行的回调
     */
    public static void play(final Context context, final String path) {
        if (new File(path).exists()) {
            final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
            final View digView = LayoutInflater.from(context).inflate(R.layout.playvoice, null);


            TextView tv_title = (TextView) digView.findViewById(R.id.tv_playvoice_filename);
            tv_title.setText(path);
            final SeekBar sb = (SeekBar) digView.findViewById(R.id.sb_playvoice);
            final ImageView iv = (ImageView) digView.findViewById(R.id.iv_playvoice_recorder);
            final ImageView iv_play = (ImageView) digView.findViewById(R.id.iv_playvoice_play);
            final TextView tv_position = (TextView) digView.findViewById(R.id.tv_playvoice_position);
            handler = new Handler();
            mp = new MediaPlayer();
            try {
                mp.setDataSource(path);
                mp.prepare();
            } catch (IOException e) {
            }
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    flag = 0;
                    iv_play.setVisibility(View.VISIBLE);
                    iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                    tv_position.setText("00:00");
                    sb.setProgress(0);
                    sb.setEnabled(false);
                    handler.removeCallbacks(runnable_play);
                }
            });
            runnable_play = new Runnable() {
                @Override
                public void run() {
                    int ll = mp.getCurrentPosition();
                    sb.setProgress(ll);
                    int ffz = ll / 1000 / 60;
                    int mmz = ll / 1000 % 60;
                    String ssfz = ffz + "";
                    if (ffz < 10) {
                        ssfz = "0" + ffz;
                    }
                    String ssmz = mmz + "";
                    if (mmz < 10) {
                        ssmz = "0" + mmz;
                    }
                    tv_position.setText(ssfz + ":" + ssmz);
                    handler.postDelayed(runnable_play, 500);
                }
            };
            int l = mp.getDuration();
            sb.setMax(l);
            sb.setProgress(0);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mp.seekTo(sb.getProgress());
                }
            });
            sb.setEnabled(false);
            int fz = l / 1000 / 60;
            int mz = l / 1000 % 60;
            String sfz = fz + "";
            if (fz < 10) {
                sfz = "0" + fz;
            }
            String smz = mz + "";
            if (mz < 10) {
                smz = "0" + mz;
            }
            TextView tv_length = (TextView) digView.findViewById(R.id.tv_playvoice_length);
            tv_length.setText(sfz + ":" + smz);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag == 0) {
                        mp.start();
                        handler.postDelayed(runnable_play, 500);
                        flag = 1;
                        sb.setEnabled(true);
                        iv_play.setVisibility(View.GONE);
                        iv.setBackgroundResource(R.drawable.record_round_red_bg);
                    } else {
                        mp.stop();
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                        }
                        sb.setEnabled(false);
                        handler.removeCallbacks(runnable_play);
                        flag = 0;
                        iv_play.setVisibility(View.VISIBLE);
                        iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                    }
                }
            });
            TextView tv_back = (TextView) digView.findViewById(R.id.tv_playvoice_title);
            tv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                    }
                    handler.removeCallbacks(runnable_play);
                   opacityDialog.dismiss();
                }
            });

            opacityDialog.setView(digView);
            opacityDialog.show();

        }
    }


}

