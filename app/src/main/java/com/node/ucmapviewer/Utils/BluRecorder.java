package com.node.ucmapviewer.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.node.ucmapviewer.R;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;



/**
 * Created by BluceLee on 2016-10-20.
 */

public class BluRecorder {
    private MediaRecorder mr = null;
    private MediaPlayer mp = null;
    private int flag = 0;
    private int fz = 0;
    private int mz = 0;
    private Handler handler = null;
    private Runnable runnable_recorder = null;
    private Runnable runnable_play = null;

    public void take(Context context, final String path, final Supplier s) {
        if (path != null && !"".equals(path)) {
            final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
            final View digView = LayoutInflater.from(context).inflate( R.layout.recorderview, null);


            TextView tv_back = (TextView) digView.findViewById(R.id.tv_recorder_title);
            tv_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mr != null) {
                        mr.stop();
                        mr.release();
                        mr = null;
                    }
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    if (handler != null) {
                        handler.removeCallbacks(runnable_recorder);
                        handler.removeCallbacks(runnable_play);
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    opacityDialog.dismiss();
                }
            });
            TextView tv_name = (TextView) opacityDialog.findViewById(R.id.tv_recorder_filename);
            tv_name.setText("文件名称:" + path);
            final TextView tv_time = (TextView) opacityDialog.findViewById(R.id.tv_recorder_time);
            handler = new Handler();
            runnable_recorder = new Runnable() {
                @Override
                public void run() {
                    mz++;
                    if (mz > 59) {
                        mz = 0;
                        fz++;
                    }
                    String sfz = fz + "";
                    if (fz < 10) {
                        sfz = "0" + fz;
                    }
                    String smz = mz + "";
                    if (mz < 10) {
                        smz = "0" + mz;
                    }
                    tv_time.setText(sfz + ":" + smz);
                    handler.postDelayed(runnable_recorder, 1000);
                }
            };
            final LinearLayout ll_play = (LinearLayout) opacityDialog.findViewById(R.id.layout_listen);
            final TextView tv_position = (TextView) opacityDialog.findViewById(R.id.tv_recorder_position);
            final TextView tv_length = (TextView) opacityDialog.findViewById(R.id.tv_recorder_length);
            final ImageView ivr = (ImageView) opacityDialog.findViewById(R.id.iv_recorder_recorder);
            final ImageView iv = (ImageView) opacityDialog.findViewById(R.id.iv_recorder_play);
            final SeekBar sb = (SeekBar) opacityDialog.findViewById(R.id.sb_recorder);
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
            final LinearLayout rl_reset = (LinearLayout) opacityDialog.findViewById(R.id.btn_record_reset);
            RelativeLayout rl_mc = (RelativeLayout) opacityDialog.findViewById(R.id.rl_recorder_mc);
            final LinearLayout rl_complete = (LinearLayout) opacityDialog.findViewById(R.id.btn_record_complete);
            runnable_play = new Runnable() {
                @Override
                public void run() {
                    int l = mp.getCurrentPosition();
                    sb.setProgress(l);
                    mz = l / 1000;
                    fz = mz / 60;
                    mz %= 60;
                    String sfz = fz + "";
                    if (fz < 10) {
                        sfz = "0" + fz;
                    }
                    String smz = mz + "";
                    if (mz < 10) {
                        smz = "0" + mz;
                    }
                    tv_position.setText(sfz + ":" + smz);
                    handler.postDelayed(runnable_play, 500);
                }
            };
            rl_mc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag == 0) {
                        mr = new MediaRecorder();
                        mr.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mr.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        mr.setOutputFile(path);
                        try {
                            mr.prepare();
                            mr.start();
                            handler.postDelayed(runnable_recorder, 1000);
                            flag = 1;
                            ivr.setBackgroundResource(R.drawable.record_round_red_bg);
                        } catch (IOException e) {
                        }
                    } else if (flag == 1) {
                        mr.stop();
                        mr.release();
                        mr = null;
                        handler.removeCallbacks(runnable_recorder);
                        mp = new MediaPlayer();
                        try {
                            mp.setDataSource(path);
                            mp.prepare();
                        } catch (IOException e) {
                        }
                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                flag = 2;
                                iv.setVisibility(View.VISIBLE);
                                iv.setImageResource(R.drawable.record_audio_play);
                                ivr.setBackgroundResource(R.drawable.record_round_blue_bg);
                                tv_position.setText("00:00");
                                sb.setProgress(0);
                                sb.setEnabled(false);
                                handler.removeCallbacks(runnable_play);
                            }
                        });
                        int l = mp.getDuration();
                        sb.setMax(l);
                        sb.setProgress(0);
                        mz = l / 1000;
                        fz = mz / 60;
                        mz %= 60;
                        String sfz = fz + "";
                        if (fz < 10) {
                            sfz = "0" + fz;
                        }
                        String smz = mz + "";
                        if (mz < 10) {
                            smz = "0" + mz;
                        }
                        tv_length.setText(sfz + ":" + smz);
                        flag = 2;
                        rl_reset.setVisibility(View.VISIBLE);
                        rl_complete.setVisibility(View.VISIBLE);
                        tv_time.setVisibility(View.GONE);
                        ll_play.setVisibility(View.VISIBLE);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageResource(R.drawable.record_audio_play);
                        ivr.setBackgroundResource(R.drawable.record_round_blue_bg);
                    } else if (flag == 2) {
                        mp.start();
                        handler.postDelayed(runnable_play, 500);
                        flag = 3;
                        sb.setEnabled(true);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageResource(R.drawable.record_audio_play_pause);
                        ivr.setBackgroundResource(R.drawable.record_round_blue_bg);
                    } else if (flag == 3) {
                        mp.stop();
                        try {
                            mp.prepare();
                        } catch (IOException e) {
                        }
                        sb.setEnabled(false);
                        handler.removeCallbacks(runnable_play);
                        flag = 2;
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageResource(R.drawable.record_audio_play);
                        ivr.setBackgroundResource(R.drawable.record_round_blue_bg);
                    }
                }
            });
            rl_reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mr != null) {
                        mr.stop();
                        mr.release();
                        mr = null;
                    }
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    if (handler != null) {
                        handler.removeCallbacks(runnable_recorder);
                        handler.removeCallbacks(runnable_play);
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    flag = 0;
                    rl_complete.setVisibility(View.INVISIBLE);
                    rl_reset.setVisibility(View.INVISIBLE);
                    iv.setVisibility(View.GONE);
                    ivr.setBackgroundResource(R.drawable.record_round_blue_bg);
                    ll_play.setVisibility(View.GONE);
                    tv_time.setText("00:00");
                    tv_time.setVisibility(View.VISIBLE);
                    fz = 0;
                    mz = 0;
                    sb.setProgress(0);
                    tv_position.setText("00:00");
                    tv_length.setText("00:00");
                }
            });
            rl_complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mr != null) {
                        mr.stop();
                        mr.release();
                        mr = null;
                    }
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    if (handler != null) {
                        handler.removeCallbacks(runnable_recorder);
                        handler.removeCallbacks(runnable_play);
                    }
                    opacityDialog.dismiss();
                    if (s != null) {
                        s.get();
                    }
                }
            });

            opacityDialog.setView(digView);
            opacityDialog.show();
            opacityDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "返回", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mr != null) {
                        mr.stop();
                        mr.release();
                        mr = null;
                    }
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                    }
                    if (handler != null) {
                        handler.removeCallbacks(runnable_recorder);
                        handler.removeCallbacks(runnable_play);
                    }
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    opacityDialog.dismiss();
                }
            });


        }
    }
}

