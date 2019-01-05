package com.node.ucmapviewer.Modules.LayerEditModule;

import com.node.ucmapviewer.Utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjectTree {
    private final String mapJsonPath;
    private static ProjectTree instance;

    public static ProjectTree getInstance(String json)
    {
        if (instance==null) instance=new ProjectTree(json);
        return instance;
    }

    public ProjectTree(String mapJsonPath){
        this.mapJsonPath = mapJsonPath;
    }

    public ProjectExtent getExtent(){
        ProjectExtent extent = new ProjectExtent();
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);
           JSONObject extentJson =  jsonObject.getJSONObject("extent");
            extent.setXmin(extentJson.getDouble("xmin"));
            extent.setXmax(extentJson.getDouble("xmax"));
            extent.setYmax(extentJson.getDouble("ymax"));
            extent.setYmin(extentJson.getDouble("ymin"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return extent;
    }

    public int getLayerIndexByName(String name){
        List<ProjectLayer> layers =  getLayers();
        for(int i=0; i < layers.size(); i++){
            ProjectLayer layer = layers.get(i);
            if(layer.getName().equals(name)){
                return i;
            }
        }
        return -1;
    }

    public void setLayerVisible(int index,boolean visible){
        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            shapelayerJson.put("visible",visible);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取所有图层
     * @return
     */
    public List<ProjectLayer> getLayers(){
        List<ProjectLayer> layerList = new ArrayList<>();
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);

            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            for(int i=0; i < jsonArray.length(); i++){
                JSONObject layerJson = jsonArray.getJSONObject(i);
                ProjectLayer layer = new ProjectLayer();
                layer.setName(layerJson.getString("name"));
                layer.setPath(layerJson.getString("path"));
                String layerTypeStr = layerJson.getString("type");
                if(layerTypeStr.equals("shapefile")){
                    layer.setType(ProjectLayer.PROJECT_LAYER_TYPE_SHAPEFILE);
                }
                else if(layerTypeStr.equals("mbtiles")){
                    layer.setType(ProjectLayer.PROJECT_LAYER_TYPE_MBTILES);
                }
                else{
                    layer.setType(ProjectLayer.PROJECT_LAYER_TYPE_UNKNOW);
                }
                layer.setLayerIndex(layerJson.getInt("layerIndex"));
                layer.setVisible(layerJson.getBoolean("visible"));
                layerList.add(layer);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return layerList;
    }

    /**
     * 获取shp图层
     * @param index
     * @return
     */
    public ProjectShapeFileLayer getShapeFileLayer(int index){
        List<ProjectLayer> layers = getLayers();
        ProjectLayer layer = layers.get(index);
        ProjectShapeFileLayer shapeFileLayer = new ProjectShapeFileLayer();
        shapeFileLayer.setName(layer.getName());
        shapeFileLayer.setPath(layer.getPath());
        shapeFileLayer.setLayerIndex(layer.getLayerIndex());
        shapeFileLayer.setType(layer.getType());
        shapeFileLayer.setVisible(layer.isVisible());

            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(JSON);
                JSONArray jsonArray=  jsonObject.getJSONArray("layers");
                JSONObject shapelayerJson = jsonArray.getJSONObject(index);
                shapeFileLayer.setEditable(shapelayerJson.getBoolean("editable"));
                shapeFileLayer.setQueryable(shapelayerJson.getBoolean("queryable"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return shapeFileLayer;
    }
    public ProjectMbtilesLayer getMbtilesLayer(int index){
        List<ProjectLayer> layers = getLayers();
        ProjectLayer layer = layers.get(index);
        ProjectMbtilesLayer mbtilesLayer = new ProjectMbtilesLayer();
        mbtilesLayer.setName(layer.getName());
        mbtilesLayer.setPath(layer.getPath());
        mbtilesLayer.setLayerIndex(layer.getLayerIndex());
        mbtilesLayer.setType(layer.getType());
        mbtilesLayer.setVisible(layer.isVisible());

        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            mbtilesLayer.setMinLevel(shapelayerJson.getInt("minLevel"));
            mbtilesLayer.setMaxLevel(shapelayerJson.getInt("maxLevel"));
            mbtilesLayer.setAlpha(shapelayerJson.getDouble("alpha"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mbtilesLayer;
    }


    /**
     * 获取点图层样式
     * @param index
     * @return
     */
    public PointStyle getPointLayerStyle(int index){
        PointStyle style = new PointStyle();
        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            style.setPointColor(styleJson.getString("pointColor"));
            style.setPointSize(styleJson.getInt("pointSize"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return style;
    }

    /**
     * 设置点图层样式
     * @param index
     * @param style
     */
    public void setPointLayerStyle(int index,PointStyle style){

        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            styleJson.put("pointSize",style.getPointSize());
            styleJson.put("pointColor",style.getPointColor());
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取点图层样式
     * @param index
     * @return
     */
    public LineStyle getLineLayerStyle(int index){
        LineStyle style = new LineStyle();
        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            style.setLineColor(styleJson.getString("lineColor"));
            style.setLineWidth(styleJson.getInt("lineWidth"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return style;
    }

    /**
     * 设置线图层样式
     * @param index
     * @param style
     */
    public void setLineLayerStyle(int index,LineStyle style){

        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            styleJson.put("lineWidth",style.getLineWidth());
            styleJson.put("lineColor",style.getLineColor());
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取面图层样式
     * @param index
     * @return
     */
    public PolygonStyle getPolygonLayerStyle(int index){
        PolygonStyle style = new PolygonStyle();
        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            style.setLineColor(styleJson.getString("lineColor"));
            style.setLineWidth(styleJson.getInt("lineWidth"));
            style.setFillColor(styleJson.getString("fillColor"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return style;
    }

    /**
     * 设置面图层样式
     * @param index
     * @param style
     */
    public void setPolygonLayerStyle(int index,PolygonStyle style){

        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            JSONObject styleJson = shapelayerJson.getJSONObject("style");
            styleJson.put("lineWidth",style.getLineWidth());
            styleJson.put("lineColor",style.getLineColor());
            styleJson.put("fillColor",style.getFillColor());
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置栅格图层透明度
     * @param index
     * @param alpha
     */
    public void setMbtilesStyle(int index,float alpha){

        String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(JSON);
            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject shapelayerJson = jsonArray.getJSONObject(index);
            shapelayerJson.put("alpha",alpha);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }





    /**
     * 移除指定图层
     * @param index
     */
    public void removeLayer(int index){
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);

            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            jsonArray.remove(index);
            jsonObject.put("layers",jsonArray);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加shp图层
     * @param name
     * @param path
     * @param canEdit
     * @param canQuery
     * @param pointStyle
     */
    public void addShapeFileLayer(String name,String path,boolean canEdit,boolean canQuery,PointStyle pointStyle){
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);

            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject newLayer = new JSONObject();
            newLayer.put("name",name);
            newLayer.put("type","shapefile");
            newLayer.put("path",path);
            newLayer.put("layerIndex",jsonArray.length());
            newLayer.put("visible",true);
            newLayer.put("editable",canEdit);
            newLayer.put("queryable",canQuery);
            JSONObject ps = new JSONObject();
            ps.put("pointSize",pointStyle.pointSize);
            ps.put("pointColor",pointStyle.pointColor);
            newLayer.put("style",ps);
            jsonArray.put(newLayer);
            jsonObject.put("layers",jsonArray);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加shp图层
     * @param name
     * @param path
     * @param canEdit
     * @param canQuery
     * @param lineStyle
     */
    public void addShapeFileLayer(String name,String path,boolean canEdit,boolean canQuery,LineStyle lineStyle){
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);

            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject newLayer = new JSONObject();
            newLayer.put("name",name);
            newLayer.put("type","shapefile");
            newLayer.put("path",path);
            newLayer.put("layerIndex",jsonArray.length());
            newLayer.put("visible",true);
            newLayer.put("editable",canEdit);
            newLayer.put("queryable",canQuery);
            JSONObject ps = new JSONObject();
            ps.put("lineWidth",lineStyle.lineWidth);
            ps.put("lineColor",lineStyle.lineColor);
            newLayer.put("style",ps);
            jsonArray.put(newLayer);
            jsonObject.put("layers",jsonArray);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加shp图层
     * @param name
     * @param path
     * @param canEdit
     * @param canQuery
     * @param polygonStyle
     */
    public void addShapeFileLayer(String name,String path,boolean canEdit,boolean canQuery,PolygonStyle polygonStyle){
        try {
            String JSON = FileUtils.openTxt(mapJsonPath,"utf8");
            JSONObject jsonObject = new JSONObject(JSON);

            JSONArray jsonArray=  jsonObject.getJSONArray("layers");
            JSONObject newLayer = new JSONObject();
            newLayer.put("name",name);
            newLayer.put("type","shapefile");
            newLayer.put("path",path);
            newLayer.put("layerIndex",jsonArray.length());
            newLayer.put("visible",true);
            newLayer.put("editable",canEdit);
            newLayer.put("queryable",canQuery);
            JSONObject ps = new JSONObject();
            ps.put("lineWidth",polygonStyle.lineWidth);
            ps.put("lineColor",polygonStyle.lineColor);
            ps.put("fillColor",polygonStyle.fillColor);
            newLayer.put("style",ps);
            jsonArray.put(newLayer);
            jsonObject.put("layers",jsonArray);
            String newJson = jsonObject.toString();
            FileUtils.saveTxt(mapJsonPath,newJson,"utf8");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ProjectShapeFileLayer extends ProjectLayer{
        boolean editable;
        boolean queryable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public boolean isQueryable() {
            return queryable;
        }

        public void setQueryable(boolean queryable) {
            this.queryable = queryable;
        }
    }

    public class ProjectMbtilesLayer extends ProjectLayer{
        double alpha;
        int minLevel;
        int maxLevel;

        public double getAlpha() {
            return alpha;
        }

        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        public int getMinLevel() {
            return minLevel;
        }

        public void setMinLevel(int minLevel) {
            this.minLevel = minLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }
    }



    /**
     * 工程图层
     */
    public class ProjectLayer{
        /**
         * shapefile图层
         */
        public static final int PROJECT_LAYER_TYPE_SHAPEFILE = 0;

        /**
         * MBTILES图层
         */
        public static final int PROJECT_LAYER_TYPE_MBTILES = 1;

        /**
         * 未知图层
         */
        public static final int PROJECT_LAYER_TYPE_UNKNOW = -1;


        String name;
        int type;
        String path;
        int layerIndex;
        boolean visible;




        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getLayerIndex() {
            return layerIndex;
        }

        public void setLayerIndex(int layerIndex) {
            this.layerIndex = layerIndex;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }


    }









    public class ProjectExtent{
        double xmin;

        public double getXmin() {
            return xmin;
        }

        public void setXmin(double xmin) {
            this.xmin = xmin;
        }

        public double getXmax() {
            return xmax;
        }

        public void setXmax(double xmax) {
            this.xmax = xmax;
        }

        public double getYmin() {
            return ymin;
        }

        public void setYmin(double ymin) {
            this.ymin = ymin;
        }

        public double getYmax() {
            return ymax;
        }

        public void setYmax(double ymax) {
            this.ymax = ymax;
        }

        double xmax;
        double ymin;
        double ymax;
    }


}

