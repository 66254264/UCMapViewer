package com.node.ucmapviewer.Modules.LayerEditModule;

import java.util.Hashtable;
import java.util.Vector;

import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class MergeTool implements UCFeatureLayerListener,MapOperTool{

    private UCMapView mMapView;
    private UCFeatureLayer layer;
    private Feature first;

    public MergeTool(UCMapView mapView,UCFeatureLayer layer)
    {
        this.mMapView=mapView;
        this.layer=layer;

    }

    @Override
    public void start() {
        layer.setListener(this);
    }

    @Override
    public void stop() {
        if (layer!=null) layer.setListener(null);
    }

    @Override
    public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
        if (distance>30) return false;
        if (first!=null) {
            Hashtable<String,Object> values=new Hashtable<String,Object>();
            for (Field f:first.schema())
                if (first.get(f.name())!=null)
                    values.put(f.name(), first.get(f.name()));
            values.put("geometry", first.geometry().union(feature.geometry()));
            UndoRedo.getInstance().beginAddUndo();
            layer.deleteFeature(first);
            UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, first, null);
            layer.deleteFeature(feature);
            UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, feature, null);
            feature=layer.addFeature(values);
            UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, feature);
            UndoRedo.getInstance().endAddUndo();
        }
        Vector<Feature> features=new Vector<Feature>();
        features.add(feature);
        first=feature;
        mMapView.getMaskLayer().setCoordinateReferenceSystem(layer.getCRS(), layer.getOutputCRS());
        mMapView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
        mMapView.refresh();
        return true;
    }

}
