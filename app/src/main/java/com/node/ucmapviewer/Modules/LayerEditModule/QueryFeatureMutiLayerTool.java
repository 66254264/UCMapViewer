package com.node.ucmapviewer.Modules.LayerEditModule;

import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;

import org.jeo.vector.Feature;

import java.util.List;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCVectorLayer;

/**
 * 要素查询工具
 */
public class QueryFeatureMutiLayerTool implements UCFeatureLayerListener,MapOperTool {

    public interface QueryFeatureListener{
        void hit(UCFeatureLayer layerFeature, Feature feature);
    }

    private MapView mapView;

    private List<UCLayerWapper> layers;

    private QueryFeatureListener listener;

    private UCVectorLayer highLightLayer;
    @Override
    public boolean onItemSingleTapUp(UCFeatureLayer ucFeatureLayer, Feature feature, double distance) {

        if(highLightLayer != null){
            mapView.deleteLayer(highLightLayer);
        }
        highLightLayer = mapView.addVectorLayer();

		
        //手指触摸误差,忽略
        if (distance>30) return false;
        if(feature != null){
            if(this.listener != null){

                this.listener.hit(ucFeatureLayer,feature);
                UCLayerWapper ucLayerWapper = new UCLayerWapper("临时",ucFeatureLayer,UCLayerWapper.UCLAYER_TYPE_FEATURE);
                if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE){
                    highLightLayer.addLine(feature.geometry(),5,0xffff0000);
                }
                else if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT){
                    highLightLayer.addPoint(feature.geometry(),0.02,0xffff0000,0.8f);
                }
                else if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON){
                    highLightLayer.addPolygon(feature.geometry(),5,0xffff0000,0xff00ff00,0.8f);
                }
                mapView.refresh();
                


            }
        }

        return false;
    }
    public QueryFeatureMutiLayerTool(MapView mapView, List<UCLayerWapper> layers, QueryFeatureListener listener){

        this.mapView = mapView;
        this.layers = layers;
        this.listener = listener;
    }



    @Override
    public boolean onItemLongPress(UCFeatureLayer ucFeatureLayer, Feature feature, double v) {
        return false;
    }

    @Override
    public void start() {
        if(layers != null){
            for (UCLayerWapper layer : layers){
                //((UCFeatureLayer)layer.getLayer()).setSearchRadius(10);
                ((UCFeatureLayer)layer.getLayer()).setListener(this);
            }


        }
    }

    @Override
    public void stop() {
        if(layers != null){
            for (UCLayerWapper layer : layers){
                ((UCFeatureLayer)layer.getLayer()).setListener(null);
            }
            if(highLightLayer != null){
                mapView.deleteLayer(highLightLayer);
            }
        }
    }
}
