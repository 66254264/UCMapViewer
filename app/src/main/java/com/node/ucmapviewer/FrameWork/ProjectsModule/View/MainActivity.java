package com.node.ucmapviewer.FrameWork.ProjectsModule.View;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.node.ucmapviewer.FrameWork.Config.SystemDirPath;
import com.node.ucmapviewer.FrameWork.ProjectsModule.Adapter.ProjectGridAdapter;
import com.node.ucmapviewer.FrameWork.ProjectsModule.Model.ProjectInfo;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private Context context;
    private List<ProjectInfo> projectInfos = null;
    private ProjectGridAdapter appGridlViewAdapter =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);

        gridView = (GridView)this.findViewById(R.id.activity_mian_gridview);

        projectInfos = getProjectInfos();
        appGridlViewAdapter = new ProjectGridAdapter(context,projectInfos);
        gridView.setAdapter(appGridlViewAdapter);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshTask refreshTask =new RefreshTask(context);
                refreshTask.execute();
            }
        });

    }

    /**
     * 获取工程信息列表
     * @return
     */
    private List<ProjectInfo> getProjectInfos() {
        List<FileUtils.FileInfo> fileInfos = FileUtils.getFileListInfo(SystemDirPath.getProjectPath(context),"folder");
        // 获取文件名列表
        List<String> fileNames = new ArrayList<>();
        if (fileInfos!=null){
            for (int i=0;i<fileInfos.size();i++){
                fileNames.add(fileInfos.get(i).FileName);
            }
        }
        Collections.sort(fileNames);//排序

        List<ProjectInfo> infos = new ArrayList<>();
        if (fileInfos!=null){

            for (int i=0;i<fileNames.size();i++){
                String name = fileNames.get(i);
                for (int j=0;j<fileInfos.size();j++){
                    FileUtils.FileInfo fileInfo = fileInfos.get(j);
                    if (fileInfo.FileName.equals(name)){
                        ProjectInfo projectInfo = new ProjectInfo();
                        projectInfo.DirName = fileInfo.FileName;
                        projectInfo.DirPath = fileInfo.FilePath;
                        infos.add(projectInfo);
                    }
                }
            }
        }
        return infos;
    }








    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出系统
     */
    private void exitActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("是否退出应用程序？");
        builder.setTitle("系统提示");
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((Activity)context).finish();
            }
        });
        builder.create().show();

    }

    /**
     * 刷新项目
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public class RefreshTask extends AsyncTask<Integer, Integer, Boolean> {

        private Context context;
        private ProgressDialog progressDialog;//等待对话框

        public RefreshTask(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
            progressDialog= ProgressDialog.show(context, null, "工程列表刷新...");
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            SystemClock.sleep(1000);
            try{
                projectInfos = getProjectInfos();
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if(result){
                appGridlViewAdapter.setAdapterList(projectInfos);//重新设置任务列表
                appGridlViewAdapter.refreshData();//刷新数据
                Toast.makeText(context, "工程列表刷新成功", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "工程列表刷新失败", Toast.LENGTH_SHORT).show();
            }

        }
    }

}
