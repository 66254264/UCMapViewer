package com.node.ucmapviewer.FrameWork.MapModule.Resource;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.R;

/**
 * 资源绑定注册类
 */
public class ResourceConfig {

    public Context context;
    private Activity activiy;

    public ResourceConfig(Context context){
        this.context = context;
        this.activiy = (Activity)context;
        initConfig();
    }

    /**资源列表**/
    public MapView mapView = null;//地图控件
    public RelativeLayout compassView =null;//指北针控件
    public View baseWidgetView = null;//widget组件
    public TextView txtLocation = null;
    public LinearLayout baseWidgetToolsView;//widget组件工具列表


    /**
     * 初始化资源列表
     */
    private void initConfig() {
        this.mapView = (MapView)activiy.findViewById(R.id.activity_map_mapview);
        this.compassView = (RelativeLayout)activiy.findViewById(R.id.activity_map_compass);

        this.baseWidgetView = activiy.findViewById(R.id.base_widget_view_baseview);


        this.txtLocation = (TextView)activiy.findViewById(R.id.activity_map_mapview_locationInfo);

        this.baseWidgetToolsView = (LinearLayout)activiy.findViewById(R.id.base_widget_view_tools_linerview);


    }


}
