package com.node.ucmapviewer.Modules;

import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.Config.SystemDirPath;
import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.Modules.LayerEditModule.ProjectTree;
import com.node.ucmapviewer.R;
import com.vividsolutions.jts.geom.Envelope;

import java.io.File;

import cn.creable.ucmap.openGIS.UCCoordinateFilter;
import cn.creable.ucmap.openGIS.UCRasterLayer;


public class BaseMapModule extends BaseModule {

    public View mWidgetView = null;//
    /**
     * 组件面板打开时，执行的操作
     * 当点击widget按钮是, WidgetManager将会调用这个方法，面板打开后的代码逻辑.
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void active() {

        super.active();//默认需要调用，以保证切换到其他widget时，本widget可以正确执行inactive()方法并关闭
        super.showWidget(mWidgetView);//加载UI并显示

        super.showMessageBox(super.name);//显示组件名称

    }

    /**
     * widget组件的初始化操作，包括设置view内容，逻辑等
     * 该方法在应用程序加载完成后执行
     */
    @Override
    public void create() {
        String mapJson = getCurProjectJsonFilePath();
        ProjectTree projectTree = ProjectTree.getInstance(mapJson);
        mapView.setProjectTree(projectTree);

        final LayoutInflater mLayoutInflater = LayoutInflater.from(super.context);
        //设置widget组件显示内容
        mWidgetView = mLayoutInflater.inflate(R.layout.widget_view_helloworld,null);
        GridView gridView = mWidgetView.findViewById(R.id.base_map_grid);

        final int[] types = new int[]{
                R.drawable.google_tra,
                R.drawable.google_vec,
                R.drawable.google_img,
                R.drawable.tianditu_vec,
                R.drawable.tianditu_tra,
                R.drawable.tianditu_img
        };

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                com.vividsolutions.jts.geom.Point oldPosition = mapView.getPosition();
                double oldScale = mapView.getScale();
                switch (position){
                    case 0:
                    {
                        UCRasterLayer googleTra=mapView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=p&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 1, 18,getBaseMapDir()+"/谷歌地形.db");
                        mapView.setBaseMapLayer(googleTra);
                        break;
                    }

                    case 1:
                    {
                        UCRasterLayer googleVec=mapView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=m&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 1, 18,getBaseMapDir()+"/谷歌道路.db");
                        mapView.setBaseMapLayer(googleVec);
                        break;
                    }
                    case 2:
                    {
                        UCRasterLayer googleImg=mapView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=y&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 1, 18,getBaseMapDir()+"/谷歌影像.db");
                        mapView.setBaseMapLayer(googleImg);
                        break;
                    }
                    case 3:
                    {
                        UCRasterLayer tiandituvec=mapView.addTDMapLayer("http://t0.tianditu.cn/vec_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=vec&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图道路w.db");

                        mapView.setBaseMapLayer(tiandituvec);

                        break;
                    }
                    case 4:
                    {
                        UCRasterLayer tianditutra=mapView.addTDMapLayer("http://t0.tianditu.cn/ter_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=ter&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图地形w.db");

                        mapView.setBaseMapLayer(tianditutra);

                        break;
                    }
                    case 5:
                    {
                        UCRasterLayer tiandituimg=mapView.addTDMapLayer("http://t0.tianditu.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图影像w.db");
                        mapView.setBaseMapLayer(tiandituimg);
                        break;
                    }

                }
                if(position == 3){
                    UCRasterLayer tiandituvecLable=mapView.addTDMapLayer("http://t0.tianditu.cn/cva_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cva&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图道路注记w.db");

                    mapView.setBaseMapLayerLable(tiandituvecLable);
                }
                if(position == 4){
                    UCRasterLayer tiandituvecLable=mapView.addTDMapLayer("http://t0.tianditu.cn/cta_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cta&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图影像注记w.db");

                    mapView.setBaseMapLayerLable(tiandituvecLable);
                }

                if(position == 5){
                    UCRasterLayer tiandituvecLable=mapView.addTDMapLayer("http://t0.tianditu.cn/cia_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cia&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图影像注记w.db");

                    mapView.setBaseMapLayerLable(tiandituvecLable);
                }


                if(position < 3){
                    final double ee = 0.00669342162296594323;
                    final double a = 6378245.0;
                    double x_PI = 3.14159265358979324 * 3000.0 / 180.0;

                    UCCoordinateFilter filter=new UCCoordinateFilter() {

                        @Override
                        public double[] to(double x, double y) {
                            double[] result=new double[2];
                            double dlat = transformlat(x - 105.0, y - 35.0);
                            double dlng = transformlng(x - 105.0, y - 35.0);
                            double radlat = y / 180.0 * Math.PI;
                            double magic = Math.sin(radlat);
                            magic = 1 - ee * magic * magic;
                            double sqrtmagic = Math.sqrt(magic);
                            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
                            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
                            double mglat = y + dlat;
                            double mglng = x + dlng;
                            result[0]=mglng;
                            result[1]=mglat;
                            return result;
                        }

                        @Override
                        public double[] from(double x, double y) {
                            double[] result=new double[2];
                            double dlat = transformlat(x - 105.0, y - 35.0);
                            double dlng = transformlng(x - 105.0, y - 35.0);
                            double radlat = y / 180.0 * Math.PI;
                            double magic = Math.sin(radlat);
                            magic = 1 - ee * magic * magic;
                            double sqrtmagic = Math.sqrt(magic);
                            dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
                            dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
                            double mglat = y + dlat;
                            double mglng = x + dlng;
                            result[0]=x * 2 - mglng;
                            result[1]=y * 2 - mglat;
                            return result;
                        }

                    };
                    mapView.setCoordinateFilter(filter);
                }
                else{
                    mapView.setCoordinateFilter(null);
                }
                mapView.moveTo(oldPosition.getX(),oldPosition.getY(),oldScale);
                mapView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mapView.refresh();
                    }
                }, 0);

               // mapView.reset(1000);
            }
        });

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return types.length;
            }

            @Override
            public Object getItem(int position) {
                return types[position];
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = LayoutInflater.from(context).inflate(R.layout.base_map_item, null);
                ImageView imageView = convertView.findViewById(R.id.map_type_icon);
                TextView yvName = convertView.findViewById(R.id.map_type_name);
                imageView.setBackgroundResource(types[position]);
                switch (types[position]){
                    case R.drawable.tianditu_img:
                        yvName.setText("天地图影像");
                        break;
                    case R.drawable.tianditu_vec:
                        yvName.setText("天地图道路");
                        break;
                    case R.drawable.tianditu_tra:
                        yvName.setText("天地图地形");
                        break;
                    case R.drawable.google_img:
                        yvName.setText("谷歌影像");
                        break;
                    case R.drawable.google_tra:
                        yvName.setText("谷歌地形");
                        break;
                    case R.drawable.google_vec:
                        yvName.setText("谷歌道路");
                        break;
                }
                return convertView;
            }
        });
        //默认是天地图影像
        UCRasterLayer tiandituimg=mapView.addTDMapLayer("http://t0.tianditu.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图影像w.db");
        mapView.setBaseMapLayer(tiandituimg);
        UCRasterLayer tiandituvecLable=mapView.addTDMapLayer("http://t0.tianditu.cn/cta_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=cta&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&tk=8def3f7faf6692b23bc854cb90041acb", 1, 18,getBaseMapDir()+"/天地图影像注记w.db");
        mapView.setBaseMapLayerLable(tiandituvecLable);
        com.vividsolutions.jts.geom.Point oldPosition = mapView.getPosition();
        double oldScale = mapView.getScale();



        final ProjectTree.ProjectExtent projectExtent = mapView.getProjectTree().getExtent();
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.refresh(1000,new Envelope(projectExtent.getXmin(),projectExtent.getXmax(),projectExtent.getYmin(),projectExtent.getYmax()));
            }
        },0);
    }

    /**
     * 组件面板关闭时，执行的操作
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void inactive(){
        super.inactive();
    }

    private String getBaseMapDir(){
        String path =SystemDirPath.getMainWorkSpacePath(context) + File.separator+"baseMap"+File.separator;
        File p = new File(path);
        if(!p.exists()){
            p.mkdirs();
        }

        return path;
    }

    public double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    };

    public double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    };

    private String getCurProjectJsonFilePath(){
        return projectPath+ File.separator+"map.json";
    }
}

