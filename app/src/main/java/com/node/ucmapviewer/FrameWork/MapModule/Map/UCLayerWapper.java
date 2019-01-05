package com.node.ucmapviewer.FrameWork.MapModule.Map;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCLayer;

public class UCLayerWapper{

    /**
     * 栅格图层
     */
    public static final int UCLAYER_TYPE_RASTER = 0;

    /**
     * 要素图层
     */
    public static final int UCLAYER_TYPE_FEATURE = 1;


    /**
     * 点
     */
    public static final int FEATURE_GEOMERTY_TYPE_POINT = 1;

    /**
     * 线
     */
    public static final int FEATURE_GEOMERTY_TYPE_LINE = 2;

    /**
     * 面
     */
    public static final int FEATURE_GEOMERTY_TYPE_POLYGON = 3;

    /**
     * 未知
     */
    public static final int FEATURE_GEOMERTY_TYPE_UNKNOW = 4;

    /**
     * 图层原型
     */
    private Object layer;

    /**
     * 图层名称
     */
    private String name;

    public void setLayer(Object layer) {
        this.layer = layer;
    }

    /**
     * 图层类型
     */
    private int layerType;

    /**
     * 是否可编辑
     */
    private boolean canEdit;

    /**
     * 是否可查询
     */
    private boolean canQuery;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 数据路径
     */
    private String path;

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanQuery() {
        return canQuery;
    }

    public void setCanQuery(boolean canQuery) {
        this.canQuery = canQuery;
    }

    public boolean getVisible(){
        return ((UCLayer)layer).getVisible();
    }
    public void setVisible(boolean b){
        ((UCLayer)layer).setVisible(b);
    }

    public int getGeomertyType(){
        if(layerType != UCLAYER_TYPE_FEATURE){
            return FEATURE_GEOMERTY_TYPE_UNKNOW;
        }
        UCFeatureLayer featureLayer = (UCFeatureLayer)layer;
        if(featureLayer == null){
            return FEATURE_GEOMERTY_TYPE_UNKNOW;
        }
        int featureType = featureLayer.getGeometryType();
        if(featureType == 1|| featureType == 4){
            return FEATURE_GEOMERTY_TYPE_POINT;
        }
        else if(featureType == 2|| featureType == 5){
            return FEATURE_GEOMERTY_TYPE_LINE;
        }
        else if(featureType == 3|| featureType == 6){
            return FEATURE_GEOMERTY_TYPE_POLYGON;
        }
        else {
            return FEATURE_GEOMERTY_TYPE_UNKNOW;
        }
    }

    public static int getGeomertyType(UCFeatureLayer layer){

        UCFeatureLayer featureLayer = (UCFeatureLayer)layer;
        if(featureLayer == null){
            return FEATURE_GEOMERTY_TYPE_UNKNOW;
        }
        int featureType = featureLayer.getGeometryType();
        if(featureType == 1|| featureType == 4){
            return FEATURE_GEOMERTY_TYPE_POINT;
        }
        else if(featureType == 2|| featureType == 5){
            return FEATURE_GEOMERTY_TYPE_LINE;
        }
        else if(featureType == 3|| featureType == 6){
            return FEATURE_GEOMERTY_TYPE_POLYGON;
        }
        else {
            return FEATURE_GEOMERTY_TYPE_UNKNOW;
        }
    }



    public UCLayerWapper(String name,UCLayer layer,int layerType){
        this.name = name;
        this.layer = layer;
        this.layerType = layerType;
        this.canEdit = false;
    }
    public UCLayerWapper(String name,UCLayer layer,int layerType,String path,boolean canEdit,boolean canQuery){
        this.name = name;
        this.layer = layer;
        this.layerType = layerType;
        this.canEdit = canEdit;
        this.canQuery = canQuery;
        this.path = path;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Object getLayer() {
        return layer;
    }

    public String getName() {
        return name;
    }

    public int getLayerType() {
        return layerType;
    }
}
