package com.node.ucmapviewer.FrameWork.ProjectsModule.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.MapModule.View.MapActivity;
import com.node.ucmapviewer.FrameWork.ProjectsModule.Model.ProjectInfo;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.FileUtils;

import java.util.List;


/**
 * 工程列表 item
 */
public class ProjectGridAdapter extends BaseAdapter {

    private static String PROJECT_IMAGE_NAME_JPG = "image.jpg";
    private static String PROJECT_IMAGE_NAME_PNG = "image.png";

    public class HomeListViewAdapterHolder{//列表绑定项
        public View itemView;
        public ImageView imageView;
        public TextView txtName;
        public TextView txtRemark;
    }

    private  List<ProjectInfo> projectInfos =null;
    private  Context context;

    public ProjectGridAdapter(Context c, List<ProjectInfo> list) {

        this.projectInfos = list;
        this.context = c;
    }

    public void setAdapterList(List<ProjectInfo> projectInfos) {
        this.projectInfos = projectInfos;
    }


    @Override
    public int getCount() {
        return projectInfos.size();
    }

    /**
     * 刷新数据
     */
    public void refreshData(){
        notifyDataSetChanged();//刷新数据
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HomeListViewAdapterHolder holder = new HomeListViewAdapterHolder();;
        convertView = LayoutInflater.from(context).inflate(R.layout.activity_main_gridview_item, null);
        holder.imageView = (ImageView) convertView.findViewById(R.id.activity_main_gridview_item_img);
        holder.txtName = (TextView)convertView.findViewById(R.id.activity_main_gridview_item_txtName);
        holder.txtRemark = (TextView)convertView.findViewById(R.id.activity_main_gridview_item_txtRemark);

        //设置缩略图信息
        String picPath = projectInfos.get(position).DirPath + "/" +PROJECT_IMAGE_NAME_JPG;
        boolean isExist = FileUtils.isExist(picPath);
        if(isExist){
            Bitmap bitmap = BitmapFactory.decodeFile(picPath);
            holder.imageView.setImageBitmap(bitmap);
        }
        String pngPath = projectInfos.get(position).DirPath + "/" +PROJECT_IMAGE_NAME_PNG;
        boolean isExistPNG = FileUtils.isExist(pngPath);
        if(isExistPNG){
            Bitmap bitmap = BitmapFactory.decodeFile(pngPath);
            holder.imageView.setImageBitmap(bitmap);
        }

        final ProjectInfo projectInfo = projectInfos.get(position);
        holder.txtName.setText(projectInfo.DirName);

        holder.txtRemark.setText("物理路径："+projectInfo.DirPath);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra("DirName",projectInfo.DirName);
                intent.putExtra("DirPath",projectInfo.DirPath);
                context.startActivity(intent);
                ((Activity)context).finish();

            }
        });

        return convertView;
    }
}
