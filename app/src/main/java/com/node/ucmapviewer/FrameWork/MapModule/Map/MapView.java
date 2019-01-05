package com.node.ucmapviewer.FrameWork.MapModule.Map;

import android.content.Context;
import android.util.AttributeSet;

import com.node.ucmapviewer.Modules.LayerEditModule.MapOperTool;
import com.node.ucmapviewer.Modules.LayerEditModule.ProjectTree;

import java.util.List;
import java.util.Vector;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCRasterLayer;

public class MapView extends UCMapView {

    /**
     * 当前交互工具
     */
    private MapOperTool mapOperTool;

    private UCRasterLayer BaseMapLayer;

    private UCRasterLayer BaseMapLayerLable;


    public UCRasterLayer getBaseMapLayer() {
        return BaseMapLayer;
    }

    public void setBaseMapLayer(UCRasterLayer baseMapLayer) {
        if(BaseMapLayer != null){
            deleteLayer(BaseMapLayer);
        }
        if(BaseMapLayerLable != null){
            deleteLayer(BaseMapLayerLable);
        }
        BaseMapLayer = baseMapLayer;
        moveLayer(BaseMapLayer,0);
        refresh();

    }
    public void setBaseMapLayerLable(UCRasterLayer baseMapLayerLable) {
        if(BaseMapLayerLable != null){
            deleteLayer(BaseMapLayerLable);
        }
        BaseMapLayerLable = baseMapLayerLable;
        moveLayer(BaseMapLayerLable,1);
        refresh();

    }




    public ProjectTree getProjectTree() {
        return projectTree;
    }

    public void setProjectTree(ProjectTree projectTree) {
        this.projectTree = projectTree;
    }

    private ProjectTree projectTree;


    private Vector<UCLayerWapper> mLayers = new Vector<>();
    public MapView(Context context,AttributeSet var2) {
        super(context,var2);
    }

//    public UCRasterLayer addTDMapLayer(String name,String url, int i, int i1, String s1) {
//        UCRasterLayer layer= super.addTDMapLayer(url, i, i1, s1);
//        mLayers.add(new UCLayerWapper(name,layer,UCLayerWapper.UCLAYER_TYPE_RASTER));
//        return layer;
//    }

    public UCFeatureLayer addFeatureLayer(String name,String path,UCFeatureLayerListener ucFeatureLayerListener,boolean canEdit,boolean canQuery) {
        UCFeatureLayer layer = super.addFeatureLayer(ucFeatureLayerListener);
        mLayers.add(new UCLayerWapper(name,layer,UCLayerWapper.UCLAYER_TYPE_FEATURE,path,canEdit,canQuery));
        return layer;
    }


    public UCRasterLayer addMbtiesLayer(String name,String path, int i, int i1) {
        UCRasterLayer layer = super.addMbtiesLayer(path, i, i1);
        mLayers.add(new UCLayerWapper(name,layer,UCLayerWapper.UCLAYER_TYPE_RASTER));

        return layer;
    }

    @Override
    public int getLayerCount() {
        return mLayers.size();
    }

    public UCLayerWapper getLayerWapper(int index){
        return mLayers.get(index);
    }

    public List<UCLayerWapper> getLayerWappers(){
        return mLayers;
    }
    /**
     * 重置
     */
    public void reset(){
        super.moveTo(super.getPosition().getX(),super.getPosition().getY(),super.getScale());
        postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 0);
    }

    /**
     * 以动画方式重置
     * @param time
     */
    public void reset(long time){
        super.animateTo(time,super.getPosition().getX(),super.getPosition().getY(),super.getScale());
    }

    public MapOperTool getMapOperTool() {
        return mapOperTool;
    }

    public void startOperTool(MapOperTool mapOperTool) {
        if(this.mapOperTool != null){
            this.mapOperTool.stop();
            this.mapOperTool = null;
        }
        this.mapOperTool = mapOperTool;
        this.mapOperTool.start();
    }

    public void stopCurrentOperTool(){
        if(this.mapOperTool != null){
            this.mapOperTool.stop();
            this.mapOperTool = null;
        }
    }


}

