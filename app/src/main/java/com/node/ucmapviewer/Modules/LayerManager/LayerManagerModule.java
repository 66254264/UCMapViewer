package com.node.ucmapviewer.Modules.LayerManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.FrameWork.MapModule.Map.PanTool;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;
import com.node.ucmapviewer.Modules.LayerEditModule.LineStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.PointStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.PolygonStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.ProjectTree;
import com.node.ucmapviewer.R;
import com.vividsolutions.jts.geom.Envelope;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCRasterLayer;

/**
 * 图层挂件
 */
public class LayerManagerModule extends BaseModule {

    private static String TAG = "LayerManagerModule";
    public View mWidgetView = null;//
    public ListView baseMapLayerListView = null;
    private Context context;
    private LayerListviewAdapter mapLayerListviewAdapter =null;


    @Override
    public void active() {
        super.active();
        super.showWidget(mWidgetView);
    }

    @Override
    public void create() {
        context = super.context;
        initMapResource();
        initWidgetView();
        //添加比例尺控件
        super.mapView.addScaleBar();
        mapView.startOperTool(new PanTool(mapView));

    }

    /**
     * UI初始化
     */
    private void initWidgetView() {
        mWidgetView = LayoutInflater.from(super.context).inflate(R.layout.widget_view_layer_manager,null);
        final RelativeLayout viewContent = mWidgetView.findViewById(R.id.widget_view_layer_manager_contentView);
        final View layerManagerView = LayoutInflater.from(super.context).inflate(R.layout.widget_view_layer_manager_layers,null);
        this.baseMapLayerListView = layerManagerView.findViewById(R.id.widget_view_layer_manager_layers_basemapLayerListview);
        mapLayerListviewAdapter = new LayerListviewAdapter(context,super.mapView);
        this.baseMapLayerListView.setAdapter(mapLayerListviewAdapter);
        viewContent.addView(layerManagerView);
    }


    /**
     * 初始化基础底图资源
     */
    private void initMapResource() {

        String mapJson = getCurProjectJsonFilePath();
        ProjectTree projectTree = ProjectTree.getInstance(mapJson);
        mapView.setProjectTree(projectTree);
        List<ProjectTree.ProjectLayer> mylayerList = projectTree.getLayers();
        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                ProjectTree.ProjectLayer p1 = (ProjectTree.ProjectLayer) o1;
                ProjectTree.ProjectLayer p2 = (ProjectTree.ProjectLayer) o2;
                return p1.getLayerIndex() >= p2.getLayerIndex()? 0 : -1;
            }
        };
        Collections.sort(mylayerList,comp);
        for(int i=0; i < mylayerList.size(); i++){
            ProjectTree.ProjectLayer layer = mylayerList.get(i);

            if(layer.getType() == ProjectTree.ProjectLayer.PROJECT_LAYER_TYPE_MBTILES){
                ProjectTree.ProjectMbtilesLayer mbtilesLayer = projectTree.getMbtilesLayer(i);
                UCRasterLayer rasterLayer = mapView.addMbtiesLayer(mbtilesLayer.getName(),getLayersPath()+mbtilesLayer.getPath(),mbtilesLayer.getMinLevel(),mbtilesLayer.getMaxLevel());
                rasterLayer.setAlpha((float)mbtilesLayer.getAlpha());
                rasterLayer.setVisible(mbtilesLayer.isVisible());
            }
            else if(layer.getType() == ProjectTree.ProjectLayer.PROJECT_LAYER_TYPE_SHAPEFILE){
                ProjectTree.ProjectShapeFileLayer shapeFileLayer = projectTree.getShapeFileLayer(i);
                String shpPath = getLayersPath()+shapeFileLayer.getPath();
                UCFeatureLayer featureLayer = mapView.addFeatureLayer(shapeFileLayer.getName(),shpPath,null,shapeFileLayer.isEditable(),shapeFileLayer.isQueryable());

                featureLayer.loadShapefile(shpPath,shapeFileLayer.isEditable());
                int geomertyType = UCLayerWapper.getGeomertyType(featureLayer);
                if(geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT){
                    PointStyle pointStyle = projectTree.getPointLayerStyle(i);
                    featureLayer.setStyle(pointStyle.getPointSize(), 0, pointStyle.getPointColor(), pointStyle.getPointColor());
                    //featureLayer.loadShapefile(shpPath,pointStyle.getPointSize(), 0, "#00ffffff", pointStyle.getPointColor(),shapeFileLayer.isEditable());
                }
                else if(geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE){
                    LineStyle lineStyle = projectTree.getLineLayerStyle(i);
                    featureLayer.setStyle( lineStyle.getLineWidth(), lineStyle.getLineWidth(), lineStyle.getLineColor(), lineStyle.getLineColor());

                    //featureLayer.loadShapefile(shpPath,0, lineStyle.getLineWidth(), lineStyle.getLineColor(), "#00ffffff",shapeFileLayer.isEditable());
                }
                else if(geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON){
                    PolygonStyle polygonStyle = projectTree.getPolygonLayerStyle(i);
                    featureLayer.setStyle(polygonStyle.getLineWidth(), polygonStyle.getLineWidth(), polygonStyle.getLineColor(), polygonStyle.getFillColor());

                    //featureLayer.loadShapefile(shpPath,0, polygonStyle.getLineWidth(), polygonStyle.getLineColor(), polygonStyle.getFillColor(),shapeFileLayer.isEditable());
                }

                featureLayer.setVisible(shapeFileLayer.isVisible());
            }
        }

        final ProjectTree.ProjectExtent projectExtent = projectTree.getExtent();
        mapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mapView.refresh(1000,new Envelope(projectExtent.getXmin(),projectExtent.getXmax(),projectExtent.getYmin(),projectExtent.getYmax()));
            }
        },0);
    }

    /**
     * 获取业务图层路径
     * @return
     */
    private String getLayersPath(){
        return projectPath+ File.separator+"layers"+File.separator;
    }

    private String getCurProjectJsonFilePath(){
        return projectPath+ File.separator+"map.json";
    }
}
