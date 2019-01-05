package com.node.ucmapviewer.FrameWork.MapModule.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.node.ucmapviewer.FrameWork.MapModule.Location.GPSUtils;
import com.node.ucmapviewer.R;
import com.vividsolutions.jts.geom.Envelope;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCRasterLayer;

public class MapToolBar {

    private Context mContext;
    private Activity mActiviy;
    private Button mBtnCloseBtn;
    private Button mBtnOpenBtn;
    private View mView;
    private MapView mMapView;
    private MapManager mapManager;
    public MapToolBar(Context context,MapView mapView,MapManager mapManager){
        this.mContext = context;
        this.mActiviy = (Activity) context;
        this.mMapView = mapView;
        this.mapManager= mapManager;
        init();
    }

    private void init(){
        mView = mActiviy.findViewById(R.id.map_tool_bar);
        mBtnCloseBtn =  mActiviy.findViewById(R.id.maptoolbar_toggle_close);
        mBtnCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView.setVisibility(View.GONE);

            }
        });
        mBtnOpenBtn =  mActiviy.findViewById(R.id.maptoolbar_toggle_open);
        mBtnOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mView.setVisibility(View.VISIBLE);
            }
        });

        mActiviy.findViewById(R.id.toolbar_zoom_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.zoomTo(mMapView.getZoomLevel()+1);
            }
        });
        mActiviy.findViewById(R.id.toolbar_zoom_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.zoomTo(mMapView.getZoomLevel()-1);
            }
        });
        mActiviy.findViewById(R.id.toolbar_full_extent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int layerCount = mMapView.getLayerCount();
                Envelope env=null;
                for(int i =0 ; i < layerCount; i++){
                  UCLayerWapper ucLayerWapper =  mMapView.getLayerWapper(i);
                  if(ucLayerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_FEATURE){
                      UCFeatureLayer featureLayer = (UCFeatureLayer) ucLayerWapper.getLayer();
                      if(env != null){
                          env.expandToInclude(featureLayer.getFullExtent());
                      }
                      else {
                          env = featureLayer.getFullExtent();
                      }
                  }
                  else if(ucLayerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_RASTER){
                      UCRasterLayer rasterLayer = (UCRasterLayer) ucLayerWapper.getLayer();
                      Envelope rstEnv = rasterLayer.getFullExtent();
                      if(env != null){

                          if(rstEnv != null){
                              env.expandToInclude(rstEnv);
                          }

                      }
                      else {
                          env = rasterLayer.getFullExtent();
                      }
                  }
                }
                if (env!=null) mMapView.refresh(1000, env);

            }
        });

        mActiviy.findViewById(R.id.toolbar_get_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bd = (BitmapDrawable) mActiviy.getResources().getDrawable(R.drawable.route_node);
                MeasureTool mTool = new MeasureTool(mMapView, bd.getBitmap(), 1);
                mMapView.startOperTool(mTool);
            }
        });

        mActiviy.findViewById(R.id.toolbar_get_length).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapDrawable bd = (BitmapDrawable) mActiviy.getResources().getDrawable(R.drawable.route_node);
                MeasureTool mTool = new MeasureTool(mMapView, bd.getBitmap(), 0);
                mMapView.startOperTool(mTool);
            }
        });

        mActiviy.findViewById(R.id.toolbar_gps_pos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(mActiviy, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActiviy, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

               Location location = mapManager.getCurlocation();
                if(location != null){
                    mMapView.moveTo(location.getLongitude(),location.getLatitude(),2048);
                }
            }
        });
        //清除
        mActiviy.findViewById(R.id.toolbar_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMapView.startOperTool(new PanTool(mMapView));
            }
        });




    }
}
