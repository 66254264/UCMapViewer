package com.node.ucmapviewer.FrameWork.MapModule.View;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.Config.AppConfig;
import com.node.ucmapviewer.FrameWork.Config.Entity.ConfigEntity;
import com.node.ucmapviewer.FrameWork.Config.Entity.WidgetEntity;
import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.FrameWork.MapModule.Base.ModuleManager;
import com.node.ucmapviewer.FrameWork.MapModule.Map.MapManager;
import com.node.ucmapviewer.FrameWork.MapModule.Map.MapToolBar;
import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.FrameWork.MapModule.Resource.ResourceConfig;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.SysUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.creable.ucmap.openGIS.UCFeatureLayer;

public class MapActivity extends AppCompatActivity {

    private Context context;
    private ResourceConfig resourceConfig;//UI资源绑定

    private ConfigEntity mConfigEntity;//应用程序配置信息
    private MapManager mMapManager;//地图管理器
    private ModuleManager mModuleManager;//组件管理器
    private MapToolBar mMapToolBar; //地图工具条

    private Map<Integer,Object> mWidgetEntityMenu = new HashMap<>();//Widget Menu列表信息

    private String DirName;//工程名称
    private String DirPath;//工程路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UCFeatureLayer.setSimplifyTolerance(0.1f);
        MapView.setTileScale(0.5f);

        setContentView(R.layout.activity_map);
        //StatusBarCompat.translucentStatusBar(this);
        context = this;

        Intent intent = getIntent();
        DirName = intent.getStringExtra("DirName");
        DirPath = intent.getStringExtra("DirPath");
        //titleTextView.setText(DirName);//显示工程文件夹名称

        resourceConfig = new ResourceConfig(context);//初始化应用程序资源列表
        resourceConfig.mapView.rotation(false);//禁止旋转
        //resourceConfig.mapView.setBackgroundColor(0xffffff);
        init();//初始化应用程序

        mMapToolBar = new MapToolBar(context,resourceConfig.mapView,mMapManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * 初始化该地图和应用程序组件
     */
    private void init() {
        //读取应用程序配置信息
        mConfigEntity = AppConfig.getConfig(context);
        //初始化底图组件信息-利用配置文件
        mMapManager = new MapManager(context, resourceConfig, mConfigEntity,DirPath);

        //初始化应用程序组件
        mModuleManager = new ModuleManager(context, resourceConfig, mMapManager, mConfigEntity,DirPath);
        //实例化Widget功能模块
        mModuleManager.instanceAllClass();

        //构建菜单
        buildLeftMenu();

    }

    /***
     * 初始化应用程序状态栏显示
     * @param toolbar
     */
//    @Override
//    public void onCreateCustomToolBar(Toolbar toolbar) {
//        super.onCreateCustomToolBar(toolbar);
////        toolbar.setNavigationIcon(null);//设置不显示回退按钮
//        getLayoutInflater().inflate(R.layout.activity_toobar_view, toolbar);
//        titleTextView = (TextView) toolbar.findViewById(R.id.activity_baseview_toobar_view_txtTitle);
//        titleTextView.setTextSize(18);
//        titleTextView.setPadding(0, 0, 0, 0);
//    }

    /**
     * 根据配置文件初始化系统功能菜单栏
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        boolean isPad = SysUtils.isPad(context);
        if (isPad){
            /***
             * 方案二模式，平板左侧菜单
             */
            //根据配置文件初始化系统功能菜单栏
            if (mConfigEntity != null) {
                final List<WidgetEntity> mListWidget = mConfigEntity.getListWidget();

                for (int i = 0; i < mListWidget.size(); i++) {
                    final WidgetEntity widgetEntity = mListWidget.get(i);

                    //widget按钮初始化操作
                    View view = LayoutInflater.from(context).inflate(R.layout.base_widget_view_tools_widget_btn, null);
                    final LinearLayout ltbtn = view.findViewById(R.id.base_widget_view_tools_widget_btn_lnbtnWidget);
                    TextView textViewName = (TextView) view.findViewById(R.id.base_widget_view_tools_widget_btn_txtWidgetToolName);
                    ImageView imageView = (ImageView)view.findViewById(R.id.base_widget_view_tools_widget_btn_imgWidgetToolIcon);
                    //设置按钮对应UI
                    mModuleManager.setWidgetBtnView(widgetEntity.getId(),textViewName,imageView);

                    ltbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int id = widgetEntity.getId();
                            BaseModule widget = mModuleManager.getSelectWidget();//当前选中
                            if (widget!=null){
                                //当前有选中的widget
                                if (id == widget.id){
                                    //判断当前是否显示状态
                                    if (mModuleManager.getSelectWidget().isActiveView()){
                                        mModuleManager.startWidgetByID(id);//显示widget
                                    }else {
                                        mModuleManager.hideSelectWidget();
                                    }

                                }else {
                                    mModuleManager.startWidgetByID(id);//显示widget
                                }
                            }else {
                                //当前未选中widget
                                mModuleManager.startWidgetByID(id);//显示widget
                            }

                        }
                    });
                    textViewName.setText(widgetEntity.getLabel());

                    try {
                        String name = widgetEntity.getIconName();
                        if (name!=null){
                            InputStream is = getAssets().open(name);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageView.setImageBitmap(bitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    resourceConfig.baseWidgetToolsView.addView(view);

                }
            }
        }
        return true;
    }

    /**
     * 构建菜单
     */
    private void buildLeftMenu(){
        boolean isPad = SysUtils.isPad(context);
        if (isPad){
            /***
             * 方案二模式，平板左侧菜单
             */
            //根据配置文件初始化系统功能菜单栏
            if (mConfigEntity != null) {
                final List<WidgetEntity> mListWidget = mConfigEntity.getListWidget();

                for (int i = 0; i < mListWidget.size(); i++) {
                    final WidgetEntity widgetEntity = mListWidget.get(i);

                    //widget按钮初始化操作
                    View view = LayoutInflater.from(context).inflate(R.layout.base_widget_view_tools_widget_btn, null);
                    final LinearLayout ltbtn = view.findViewById(R.id.base_widget_view_tools_widget_btn_lnbtnWidget);
                    TextView textViewName = (TextView) view.findViewById(R.id.base_widget_view_tools_widget_btn_txtWidgetToolName);
                    ImageView imageView = (ImageView)view.findViewById(R.id.base_widget_view_tools_widget_btn_imgWidgetToolIcon);
                    //设置按钮对应UI
                    mModuleManager.setWidgetBtnView(widgetEntity.getId(),textViewName,imageView);

                    ltbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int id = widgetEntity.getId();
                            BaseModule widget = mModuleManager.getSelectWidget();//当前选中
                            if (widget!=null){
                                //当前有选中的widget
                                if (id == widget.id){
                                    //判断当前是否显示状态
                                    if (mModuleManager.getSelectWidget().isActiveView()){
                                        mModuleManager.startWidgetByID(id);//显示widget
                                    }else {
                                        mModuleManager.hideSelectWidget();
                                    }

                                }else {
                                    mModuleManager.startWidgetByID(id);//显示widget
                                }
                            }else {
                                //当前未选中widget
                                mModuleManager.startWidgetByID(id);//显示widget
                            }

                        }
                    });
                    textViewName.setText(widgetEntity.getLabel());

                    try {
                        String name = widgetEntity.getIconName();
                        if (name!=null){
                            InputStream is = getAssets().open(name);
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            imageView.setImageBitmap(bitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    resourceConfig.baseWidgetToolsView.addView(view);

                }
            }
        }
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
        builder.setMessage("要退出UCMapViewer吗？");
        builder.setTitle("系统提示");
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		//预留处理拍照后回调问题
        //EventBus.getDefault().post(new MessageEvent("camera-"+requestCode,0));
    }


}
