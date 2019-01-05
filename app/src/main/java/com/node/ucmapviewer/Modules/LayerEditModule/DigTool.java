package com.node.ucmapviewer.Modules.LayerEditModule;

import java.util.Hashtable;

import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import com.vividsolutions.jts.geom.Geometry;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class DigTool extends AddFeatureTool2 implements UCFeatureLayerListener {

    private Feature oFeature;

    public DigTool(UCMapView mapView, UCFeatureLayer layer, Bitmap pointImage) {
        super(mapView, layer, pointImage);

    }

    @Override
    public void start()
    {
        layer.setListener(this);
        super.start();
    }

    @Override
    public void onLongPress(MotionEvent arg0) {

        if (feature!=null && oFeature!=null)
        {
            Hashtable<String,Object> values=new Hashtable<String,Object>();
            for (Field f:oFeature.schema())
                if (oFeature.get(f.name())!=null)
                    values.put(f.name(), oFeature.get(f.name()));
            values.put("geometry", oFeature.geometry().difference(feature.geometry()));
            UndoRedo.getInstance().beginAddUndo();
            layer.deleteFeature(oFeature);
            UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, oFeature, null);
            layer.deleteFeature(feature);//’‚ «“ª∏ˆ¡Ÿ ±“™Àÿ£¨≤ª–Ë“™ÃÌº”µΩundo
            //UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, feature, null);
            feature=layer.addFeature(values);
            UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, feature);
            UndoRedo.getInstance().endAddUndo();
        }
        mlayer.removeAllItems();
        coords.clear();
        coords=null;
        feature=null;
        this.oFeature=null;
        mapView.refresh();
    }

    @Override
    public boolean onItemLongPress(UCFeatureLayer arg0, Feature arg1, double arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
        if (oFeature!=null || distance>30) return false;
        this.oFeature=feature;
        return false;
    }

    @Override
    public void stop() {
        layer.setListener(null);
        super.stop();
    }

}

