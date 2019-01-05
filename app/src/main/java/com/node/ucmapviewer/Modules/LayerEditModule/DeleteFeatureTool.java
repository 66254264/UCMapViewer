package com.node.ucmapviewer.Modules.LayerEditModule;

import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;

import org.jeo.vector.Feature;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;

/**
 * 删除要素工具
 */
public class DeleteFeatureTool implements UCFeatureLayerListener,MapOperTool {

    private MapView mapView;

    private UCLayerWapper layer;
    @Override
    public boolean onItemSingleTapUp(UCFeatureLayer ucFeatureLayer, Feature feature, double distance) {

        //手指触摸误差,忽略
        if (distance>30) return false;
        if(feature != null){
            boolean ok = ucFeatureLayer.deleteFeature(feature);
            UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, ucFeatureLayer, feature, null);
            mapView.refresh();
        }

        return false;
    }

    @Override
    public boolean onItemLongPress(UCFeatureLayer ucFeatureLayer, Feature feature, double v) {
        return false;
    }

    public DeleteFeatureTool(MapView mapView,UCLayerWapper layer){

        this.mapView = mapView;
        this.layer = layer;
    }

    @Override
    public void start() {
        if(layer != null){
            ((UCFeatureLayer)layer.getLayer()).setListener(this);
        }
    }

    @Override
    public void stop() {
        if(layer != null){
            ((UCFeatureLayer)layer.getLayer()).setListener(null);
        }

    }
}
