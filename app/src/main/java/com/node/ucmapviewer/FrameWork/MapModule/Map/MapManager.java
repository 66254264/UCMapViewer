package com.node.ucmapviewer.FrameWork.MapModule.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;


import com.node.ucmapviewer.FrameWork.MapModule.Location.GPSUtils;
import com.node.ucmapviewer.FrameWork.Config.Entity.ConfigEntity;

import com.node.ucmapviewer.FrameWork.MapModule.PartView.Compass;
import com.node.ucmapviewer.FrameWork.MapModule.Resource.ResourceConfig;


import java.text.DecimalFormat;

import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.ToastUtils;

import cn.creable.ucmap.openGIS.UCMapViewListener;
import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;


/**
 * 地图组件管理类

 */

public class MapManager implements LocationListener{

    private static String TAG = "MapManager";

    private Context context;
    private ResourceConfig resourceConfig;

    private ConfigEntity configEntity;

    private String projectPath;

    private UCMarkerLayer locationLayer = null;
    private UCMarker locationMaker = null;


    public MapManager(Context context, ResourceConfig resourceConfig, ConfigEntity ce, String dirPath) {
        this.context = context;
        this.resourceConfig = resourceConfig;
        this.configEntity = ce;
        this.projectPath = dirPath;


        //resourceConfig.mapView.setMap(map);

        initMapResource();//初始化配置
    }

    LocationManager locationManager;
    /**
     * 初始化地图资源
     */
    private void initMapResource() {


        /**指北针*/
        final Compass mCompass = new Compass(context, null, resourceConfig.mapView);
        mCompass.setClickable(true);
        resourceConfig.compassView.addView(mCompass);
        mCompass.setRotationAngle(-resourceConfig.mapView.getAngle());
        // Set a single tap listener on the MapView.
        mCompass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompass.setRotationAngle(0);
                resourceConfig.mapView.reset(1000);
            }
        });
        resourceConfig.mapView.bind(new UCMapViewListener() {
            @Override
            public void onMapViewEvent() {
                mCompass.setRotationAngle(-resourceConfig.mapView.getAngle());
            }
        });

        //GPS位置信息



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
        resourceConfig.mapView.addLocationLayer(Color.BLUE);
        this.locationLayer = resourceConfig.mapView.addMarkerLayer(null);


        final Azimuth a=new Azimuth();
        a.start(context,new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (locationMaker!=null) {
                    if (Math.abs(locationMaker.getAngle()-a.get())>3) {//变化超过一定的值才会重设角度
                        locationMaker.setAngle((float)a.get());
                    }
                    resourceConfig.mapView.refresh();
                }
                //Point pt=mView.getPosition();
                //mView.moveTo(pt.getX(),pt.getY(),mView.getScale(),0,(float)a.get());
                //System.out.println(a.get());
            }

        },100);

    }

    public Location getCurlocation(){
        return curlocation;
    }

    public double getAngle(double x,double y,double x2,double y2)
    {
        double dx=y2-y;
        double dy=x2-x;
        if (dx==0)
        {
            if (dy>0) return 90;
            else return 270;
        }
        double a=180-Math.atan(Math.abs(dy/dx))/Math.PI*180;
        if (dx>0 && dy>=0)
        {

        }
        else if (dx<0 && dy>=0)
        {
            a=180-a;
        }
        else if (dx<0 && dy<0)
        {
            a=180+a;
        }
        else if (dx>0 && dy<0)
        {
            a=360-a;
        }
        System.out.println("a="+a);
        return a;
    }

    private Location curlocation;
    @Override
    public void onLocationChanged(Location location) {
        curlocation = location;
        if(curlocation == null){
            return;
        }
//        float angle = 0;
        if(locationMaker == null){
            Bitmap pos=BitmapFactory.decodeResource(context.getResources(),R.mipmap.arrow);
            locationMaker =locationLayer.addBitmapItem(pos, curlocation.getLongitude(),curlocation.getLatitude(),"","");
        }
        else{
//            angle=(float)(getAngle(locationMaker.getX(),locationMaker.getY(),curlocation.getLongitude(),curlocation.getLongitude()))-90;
//            if (angle>90 && angle<270) angle=angle-180;

            locationMaker.setXY(curlocation.getLongitude(),curlocation.getLatitude());
            locationLayer.refresh();
        }
//        locationMaker.setAngle(angle);
        resourceConfig.mapView.setLocationPosition(curlocation.getLongitude(),curlocation.getLatitude(),curlocation.getAccuracy());
        new Thread() {
            public void run() {

                final StringBuilder sb = new StringBuilder();
                sb.append("经度:");
                sb.append(new DecimalFormat("######0.00").format(curlocation.getLongitude()));
                sb.append("纬度:");
                sb.append(new DecimalFormat("######0.00").format(curlocation.getLatitude()));
                sb.append("高程:");
                sb.append(new DecimalFormat("######0.0").format(curlocation.getAltitude()));
                sb.append("误差:");
                sb.append(new DecimalFormat("######0.0").format(curlocation.getAccuracy()));
                ((Activity) context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        resourceConfig.txtLocation.setText(sb.toString());


                    }

                });
            }
        }.start();
        resourceConfig.mapView.moveLayer(locationLayer,200);
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
