package com.node.ucmapviewer.Modules;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.FrameWork.MapModule.Location.GPSUtils;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.FileUtils;
import com.node.ucmapviewer.Utils.ToastUtils;
import com.node.ucmapviewer.Utils.gpx.GPXParser;
import com.node.ucmapviewer.Utils.gpx.beans.GPX;
import com.node.ucmapviewer.Utils.gpx.beans.Track;
import com.node.ucmapviewer.Utils.gpx.beans.Waypoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cn.creable.ucmap.openGIS.UCVectorLayer;


/**
 * 图层编辑挂件
 */
public class TrajManager extends BaseModule implements LocationListener {

    public View mWidgetView = null;//

    private ListView trajListView;
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
        trajListView = mWidgetView.findViewById(R.id.traj_list_view);
        refreshList();

    }




    /**
     * widget组件的初始化操作，包括设置view内容，逻辑等
     * 该方法在应用程序加载完成后执行
     */
    @Override
    public void create() {
        final LayoutInflater mLayoutInflater = LayoutInflater.from(super.context);
        //设置widget组件显示内容
        mWidgetView = mLayoutInflater.inflate(R.layout.widget_view_tra_manager,null);
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, this.getClass().getCanonicalName());
        mWidgetView.findViewById(R.id.open_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button btn =  mWidgetView.findViewById(R.id.open_stop);
                if(routeStoped){
                    startRecord();
                    btn.setText("轨迹记录中...");

                }
                else{
                    stopAndsaveToGpx();
                    refreshList();
                    ToastUtils.showShort(context,"轨迹已保存");
                    btn.setText("开始记录轨迹");
                }
                routeStoped = !routeStoped;
            }
        });


    }

    private void refreshList(){
        final List<FileUtils.FileInfo> fileInfos =  FileUtils.getFileListInfo(projectPath+ File.separator+"gpxs","gpx");
        if(fileInfos == null){
            return;
        }
        trajListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return fileInfos.size();
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(final int i, View view, ViewGroup viewGroup) {

                final LayoutInflater mLayoutInflater = LayoutInflater.from(TrajManager.super.context);
                View con = mLayoutInflater.inflate(R.layout.widget_view_tra_manager_list_item,null);
                TextView tvName = con.findViewById(R.id.gpx_name);
                tvName.setText(fileInfos.get(i).FileName);
                con.findViewById(R.id.load_this_gpx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadgpx(fileInfos.get(i).FilePath);
                    }
                });
                return con;
            }
        });
    }

    private void loadgpx(String gpxfile){
        GPXParser parser = new GPXParser();
        try {
            GPX gpx = parser.parseGPX(new FileInputStream(gpxfile));
            Set<Track> tracks = gpx.getTracks();
            int size=tracks.size();
            ArrayList<Waypoint> points=tracks.iterator().next().getTrackPoints();
            if(vlayer ==null){
                vlayer = mapView.addVectorLayer();
            }
            else{
                if (route!=null) vlayer.remove(route);
            }

            route=null;
            size=points.size();
            if (size<2)
            {
                ToastUtils.showShort(context,"无效的gpx轨迹");
                return;
            }
            Coordinate[] coords=new Coordinate[size];
            for (int i1=0;i1<size;++i1)
            {
                coords[i1]=new Coordinate(points.get(i1).getLongitude(),points.get(i1).getLatitude());
            }
            route=gf.createLineString(coords);
            vlayer.addLine(route, 3, 0xFF00FF00);
            Envelope envelope = route.getEnvelopeInternal();
            if(envelope != null){
                mapView.refresh(500,envelope);
            }
            else {
                mapView.moveTo(points.get(0).getLongitude(),points.get(0).getLatitude(), mapView.getScale());
            }


            mapView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapView.refresh();
                }
            }, 0);
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    /**
     * 组件面板关闭时，执行的操作
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void inactive(){
        super.inactive();


    }

    Thread routeThread;

    PowerManager.WakeLock mWakeLock;

    private ArrayList<Waypoint> routePoints;

    Boolean routeStoped=true;

    private LineString route;

    GeometryFactory gf=new GeometryFactory();

    UCVectorLayer vlayer;

    Location currenLocation;

    /**
     * 停止并保存到gpx文件中
     */
    private void stopAndsaveToGpx(){
        if (vlayer==null) mapView.deleteLayer(vlayer);

        if (routePoints.size()<2) {
            if(mWakeLock != null&&mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            return;
        }
        if(mWakeLock != null&&mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        GPXParser parser = new GPXParser();
        GPX gpx=new GPX();
        Track track=new Track();
        track.addTrackPoints(routePoints);
        gpx.addTrack(track);
        try {
            String date = new Date().toString();
            String gpxFile = projectPath+ File.separator+"gpxs"+File.separator+date+".gpx";
            parser.writeGPX(gpx, new FileOutputStream(gpxFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    LocationManager locationManager;

    private void requestGPS(){

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastUtils.showShort(context, "权限不够");
            return;
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastUtils.showShort(context, "权限不够");
            return;
        }
        for (String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                locationManager.requestLocationUpdates(provider, 0, 0, (LocationListener) this);
            }
        }
    }

    /**
     * 记录轨迹
     */
    private void startRecord(){
        if (vlayer==null) vlayer=mapView.addVectorLayer();
                requestGPS();
                mWakeLock.acquire();
                routePoints=new ArrayList<Waypoint>();
                routeThread=new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (!routeStoped)
                        {
                            routePoints.add(new Waypoint("",(float)currenLocation.getLongitude(),(float)currenLocation.getLatitude()));
                            int size=routePoints.size();
                            if (size>1) {
                                Coordinate[] coords=new Coordinate[size];
                                for (int i1=0;i1<size;++i1)
                                {
                                    coords[i1]=new Coordinate(routePoints.get(i1).getLongitude(),routePoints.get(i1).getLatitude());
                                }
                                if (route==null) {
                                    route=gf.createLineString(coords);
                                    vlayer.addLine(route, 3, 0xFF00FF00);
                                } else {
                                    LineString newgeo=gf.createLineString(coords);
                                    vlayer.updateGeometry(route, newgeo);
                                    route=newgeo;
                                }
                            }
                            try {
                                for (int i=0;i<150;i++)//停15秒
                                {
                                    if (routeStoped) return;
                                    Thread.sleep(100);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                });
                routeThread.start();

    }

    @Override
    public void onLocationChanged(Location location) {
        currenLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

