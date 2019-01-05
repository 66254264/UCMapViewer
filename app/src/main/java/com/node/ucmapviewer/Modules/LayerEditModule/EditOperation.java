package com.node.ucmapviewer.Modules.LayerEditModule;

import org.jeo.vector.Feature;

import cn.creable.ucmap.openGIS.UCFeatureLayer;

public class EditOperation
{
    EditOperation(int type, UCFeatureLayer layer, Feature oldFeature, Feature newFeature)
    {
        this.type=type;
        this.layer=layer;
        this.oldFeature=oldFeature;
        this.newFeature=newFeature;
    }

    int type;
    Feature oldFeature;
    Feature newFeature;
    UCFeatureLayer layer;

    public static final int AddFeature=0;//添加要素
    public static final int DeleteFeature=1;//删除要素
    public static final int UpdateFeature=2;//更新要素

    @Override
    public String toString() {
        return type+",layerName="+layer.getName()+",oldFID="+(oldFeature!=null?oldFeature.id():"null")+",newFID"+(newFeature!=null?newFeature.id():"null");
    }
}
