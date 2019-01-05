package com.node.ucmapviewer.FrameWork.RootAct;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.ProjectsModule.View.MainActivity;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *  应用程序初始化页面
 */
public class InitActivity extends AppCompatActivity {


    private static final int REQUEST_CODE = 0; // 请求码
    // 所需的全部权限
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写入存储
            Manifest.permission.ACCESS_FINE_LOCATION,//位置信息
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA, //相机
            Manifest.permission.WAKE_LOCK //唤醒锁
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_init);


        TextView textView = (TextView)this.findViewById(R.id.activity_init_versionTxt);
        String version = AppUtils.getVersionName(this);
        textView.setText("版本号:"+version);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            checkPermissions(PERMISSIONS);

        }
        else {
            handler.sendEmptyMessageDelayed(0,2000);
        }

    }



    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            getHome();
            super.handleMessage(msg);
        }
    };

    public void getHome(){
        Intent intent = new Intent(InitActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void checkPermissions(String[] permissionArray){
        //检查所有的权限，如果所有权限具备，则初始化MapGIS开发环境
        if (hasGetAllPermission(PERMISSIONS)) {
            //handler.sendEmptyMessageDelayed(0,2000);
            handler.sendEmptyMessageDelayed(0,1000);
        }
        //不是所有权限都具备，则继续检查请求
        else {
            for (String permission:permissionArray){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                    //没有授权
                    requestPermissions(permission);
                }
            }
        }
    }

    /**
     * 检验是否所有需要的权限都已申请
     */
    public boolean hasGetAllPermission(String[] permissionArray){
        List<String> needPermission=new ArrayList<String>();
        for(String permission:permissionArray){
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needPermission.add(permission);
            }
        }
        //如果所有权限都已具备，则返回true，否则返回false
        if (needPermission.size()==0) {
            return true;
        }
        else return false;
    }

    /**
     * 请求权限
     * @param permission
     */
    public void requestPermissions(String permission){
        ActivityCompat.requestPermissions(this,new String[]{permission},REQUEST_CODE);
    }

    /**
     * 请求权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0){
                //权限没有授予
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    switch (permissions[0]){
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                            showMyDialog("此程序需要存储的读写权限，请点击设置前往权限模块授予。\n未授予权限程序无法正常工作");
                            break;
                        case Manifest.permission.READ_PHONE_STATE:
                            showMyDialog("此程序需要电话权限，请点击设置前往权限模块授予。\n未授予权限程序无法正常工作");
                            break;
                        default:
                            break;
                    }
                }
                //权限已授予
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //如果权限授予了，则重新判断所有权限，没有授予的继续请求
                    checkPermissions(PERMISSIONS);
                }
            }
        }
    }

    /**
     * 弹出自定义对话框：提示权限的重要性，并引导用户前往程序的应用管理界面手动开启权限
     * @param message
     */
    private void showMyDialog(String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("权限申请");
        builder.setMessage(message);
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //打开系统中应用设置的界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                //finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //如果取消，则退出应用
                //finish();
            }
        });
        builder.setCancelable(false);//点击返回键和空白区域不取消对话框
        builder.show();
    }


}
