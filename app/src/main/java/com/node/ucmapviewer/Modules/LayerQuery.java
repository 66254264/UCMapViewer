package com.node.ucmapviewer.Modules;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.natewickstrom.rxactivityresult.ActivityResult;
import com.natewickstrom.rxactivityresult.RxActivityResult;
import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;
import com.node.ucmapviewer.Modules.LayerEditModule.QueryFeatureMutiLayerTool;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.BluRecorder;
import com.vividsolutions.jts.geom.Envelope;

import org.jeo.data.Cursor;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCVectorLayer;
import rx.functions.Action1;


/**
 * 地图查询组件
 */
public class LayerQuery extends BaseModule implements GestureDetector.OnGestureListener {

    public View mWidgetView = null;//

    private GridView singleAttlistView;
    private AutoFillGridView searchAttlistView;


    private Spinner spinner;
    private List<LayerAtt> layerAtts = new ArrayList<>();
    private List<LayerAtt> allsearchAtts = new ArrayList<>();

    /**
     * 点击查询
     */
    public static final int QUERY_TYPE_TAP = 1;

    /**
     * 搜索查询
     */
    public static final int QUERY_TYPE_SEARCH = 2;

    private int QueryType;

    QueryFeatureMutiLayerTool tool;

    private UCVectorLayer highLightLayer;

    private Handler handler;
    private int lastVisibleIndex;

    SearchQueryResultGridAdapter searchQueryResultGridAdapter;
    Cursor<Feature> searchResultCursor;
    View bottomMooreView;

    /**
     * 组件面板打开时，执行的操作
     * 当点击widget按钮是, WidgetManager将会调用这个方法，面板打开后的代码逻辑.
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void active() {

        super.active();//默认需要调用，以保证切换到其他widget时，本widget可以正确执行inactive()方法并关闭
        super.showWidget(mWidgetView);//加载UI并显示
        super.showMessageBox(super.name);//显示组件名称
        bindMapQuery();
        QueryType = QUERY_TYPE_TAP;

    }

    private void bindMapQuery(){
        int count = super.mapView.getLayerCount();

        List<UCLayerWapper> listLayer = new ArrayList<>();
        for(int i =0; i < count; i++){
            if(super.mapView.getLayerWapper(i).getLayerType() == UCLayerWapper.UCLAYER_TYPE_FEATURE
                    && super.mapView.getLayerWapper(i).isCanQuery()
                    &&((UCFeatureLayer)super.mapView.getLayerWapper(i).getLayer()).getVisible() ){
                listLayer.add(super.mapView.getLayerWapper(i));
            }
        }
        if(tool == null){
            tool = new QueryFeatureMutiLayerTool(mapView, listLayer, new QueryFeatureMutiLayerTool.QueryFeatureListener() {
                @Override
                public void hit(UCFeatureLayer layerFeature, Feature feature) {
                    showAtt(layerFeature,feature);
                }
            });

        }
        mapView.startOperTool(tool);
        super.mapView.setListener(this,null);

    }

    private void unBindMapQuery(){
        if(tool != null){
            tool.stop();
        }
        super.mapView.setListener(null,null);
    }


    /**
     * widget组件的初始化操作，包括设置view内容，逻辑等
     * 该方法在应用程序加载完成后执行
     */
    @Override
    public void create() {
        final LayoutInflater mLayoutInflater = LayoutInflater.from(super.context);
        //设置widget组件显示内容
        mWidgetView = mLayoutInflater.inflate(R.layout.widget_view_mapquery,null);
        singleAttlistView = mWidgetView.findViewById(R.id.single_feature_att_list);
        searchAttlistView = mWidgetView.findViewById(R.id.search_query_resultListview);
        bottomMooreView = mLayoutInflater.inflate(R.layout.more_data_bottom,null);
        handler = new Handler();
        mWidgetView.findViewById(R.id.tap_query_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindMapQuery();
                QueryType = QUERY_TYPE_TAP;
                mWidgetView.findViewById(R.id.tap_query).setVisibility(View.VISIBLE);
                mWidgetView.findViewById(R.id.search_query).setVisibility(View.GONE);
                mWidgetView.findViewById(R.id.tap_query_btn_bg).setVisibility(View.VISIBLE);
                mWidgetView.findViewById(R.id.search_query_btn_bg).setVisibility(View.GONE);
            }
        });
        mWidgetView.findViewById(R.id.search_query_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unBindMapQuery();
                QueryType = QUERY_TYPE_TAP;
                mWidgetView.findViewById(R.id.tap_query).setVisibility(View.GONE);
                mWidgetView.findViewById(R.id.search_query).setVisibility(View.VISIBLE);
                mWidgetView.findViewById(R.id.tap_query_btn_bg).setVisibility(View.GONE);
                mWidgetView.findViewById(R.id.search_query_btn_bg).setVisibility(View.VISIBLE);
            }
        });


        /**
         * 初始化可查询图层列表
         */
        List<UCLayerWapper> queryLayers = new ArrayList<>();
       spinner = mWidgetView.findViewById(R.id.query_layer_spinner);
        int layerCount = super.mapView.getLayerCount();
        for(int i=0; i < layerCount; i++){
            UCLayerWapper layerWapper =  super.mapView.getLayerWapper(i);
            if(layerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_FEATURE && layerWapper.isCanQuery()){
                queryLayers.add(layerWapper);
            }
        }

        spinner.setAdapter(new LayerSpinnerAdapter(super.context,queryLayers));

        //绑定查询
        mWidgetView.findViewById(R.id.start_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchTask task = new SearchTask(context);
                UCLayerWapper curLayer = (UCLayerWapper)spinner.getSelectedItem();
                EditText keyWord =  mWidgetView.findViewById(R.id.search_key);
                Object[] params = new Object[2];
                params[0] = curLayer;
                params[1] = keyWord.getText().toString();
                allsearchAtts.clear();
                searchQueryResultGridAdapter = new SearchQueryResultGridAdapter(allsearchAtts);
                searchAttlistView.setAdapter(searchQueryResultGridAdapter);
                task.execute(params);
            }
        });

    }



    private List<FeatureAtt> getFeatureAtt(UCFeatureLayer ucFeatureLayer,Feature feature){
        final List<FeatureAtt> list = new ArrayList<>();

        int fldCount = ucFeatureLayer.getFieldCount();
        for(int i=0; i < fldCount; i++){
            String strVal="";
            Field f = ucFeatureLayer.getField(i);
            if(f.name().equals("geometry")){
                continue;
            }
            Object value = feature.get(i);
            if (value!=null)
            {
                if (value.getClass()==Byte.class)
                    strVal=Byte.toString((Byte)value);
                else if (value.getClass()==Short.class)
                    strVal=Short.toString((Short)value);
                else if (value.getClass()==Integer.class)
                    strVal=Integer.toString((Integer)value);
                else if (value.getClass()==Long.class)
                    strVal=Long.toString((Long)value);
                else if (value.getClass()==Float.class)
                    strVal=Float.toString((Float)value);
                else if (value.getClass()==Double.class)
                    strVal=Double.toString((Double)value);
                else if (value.getClass()==java.sql.Date.class)
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    strVal=format.format((java.sql.Date)value);
                }
                else if (value.getClass()==java.sql.Time.class)
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    strVal=format.format((java.sql.Time)value);
                }
                else if (value.getClass()==String.class)
                    strVal=(String)value;
            }


				
            list.add(new FeatureAtt(f.name(),strVal,ucFeatureLayer,feature.geometry()));
        }

        return list;

    }

    private Cursor<Feature> startSearch(UCLayerWapper layerWapper,String key) {

        UCLayerWapper curLayer = layerWapper;
       return searchByKey(curLayer,key);



    }

    public class SearchTask extends AsyncTask<Object, Void, List<LayerAtt>> {

        private Context context;
        private ProgressDialog progressDialog;//等待对话框

        public SearchTask(Context context){
            this.context = context;
        }


        @Override
        protected List<LayerAtt> doInBackground(Object... ucLayerWappers) {


            List<LayerAtt> all = new ArrayList<>();
            Cursor<Feature> features= startSearch((UCLayerWapper) ucLayerWappers[0],(String)ucLayerWappers[1]);


            try
            {
                UCLayerWapper curLayer = (UCLayerWapper)spinner.getSelectedItem();
                boolean bhasNext = features.hasNext();
                while ( bhasNext) {
                    try {
                        Feature ft = features.next();
                        List<FeatureAtt> list =  getFeatureAtt((UCFeatureLayer) curLayer.getLayer(),ft);
                        LayerAtt layerAtt = new LayerAtt();
                        layerAtt.layerName = "";
                        layerAtt.setFlds(list);
                        all.add(layerAtt);

                        bhasNext = features.hasNext();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                features.close();
                return all;
            }
            catch (IOException e){
                e.printStackTrace();
                return all;
            }


        }

        @Override
        protected void onPreExecute() {
            //第一个执行方法
            super.onPreExecute();
            progressDialog= ProgressDialog.show(context, null, "正在检索");
        }



        @Override
        protected void onPostExecute(List<LayerAtt> result) {
            super.onPostExecute(result);

            allsearchAtts = result;
            TextView tvCount = mWidgetView.findViewById(R.id.search_result_text);
            if(result == null){
                tvCount.setText("未检索到满足条件的记录");
            }
            else{
                tvCount.setText("检索到"+allsearchAtts.size() +"个结果:");
            }
            //仅加载前50个
            if(allsearchAtts.size()>50){
                allsearchAtts= allsearchAtts.subList(0,50);
            }

            //searchQueryResultGridAdapter.notifyDataSetChanged();
            searchAttlistView.setAdapter(new SearchQueryResultGridAdapter(allsearchAtts));
            //buttonSearch.setText("查询");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            },0);


        }
    }

    private List<LayerAtt> load10rows(){
        List<LayerAtt> paged = allsearchAtts.subList(0,10);
        return paged;

    }

    private Cursor<Feature> searchByKey(UCLayerWapper layer, String key){
        String where = buildWhere(layer,key);
        UCFeatureLayer featureLayer = (UCFeatureLayer)layer.getLayer();
        Cursor<Feature> cursor = featureLayer.searchFeature(where,0,0,0,0,0,0);
        return cursor;
    }

    private String buildWhere(UCLayerWapper layer,String key){
        StringBuilder sb = new StringBuilder();

        UCFeatureLayer featureLayer = (UCFeatureLayer)layer.getLayer();
        if(featureLayer != null){
            int fldCount = featureLayer.getFieldCount();
            for(int i=0; i < fldCount; i++){
                Field fld = featureLayer.getField(i);
                if(fld.type() == String.class){
                    sb.append("or ");
                    sb.append(fld.name());
                    sb.append(" like ");
                    sb.append("'%");
                    sb.append(key);
                    sb.append("%'");
                }
            }
        }
        if(key != null && !key.equals("")){
            return sb.toString().substring(3);
        }
        else{
            return "1=1";
        }

    }




    @Override
    public void inactive(){
        super.inactive();
        unBindMapQuery();

    }



    private void showAtt(UCFeatureLayer ucFeatureLayer, Feature feature){
        final LayoutInflater mLayoutInflater = LayoutInflater.from(super.context);

        final List<FeatureAtt> list = new ArrayList<>();

        int fldCount = ucFeatureLayer.getFieldCount();
        for(int i=0; i < fldCount; i++){
            String strVal="";
            Field f = ucFeatureLayer.getField(i);
            if(f.name().equals("geometry")){
                continue;
            }
            Object value = feature.get(i);
            if (value!=null)
            {
                if (value.getClass()==Byte.class)
                    strVal=Byte.toString((Byte)value);
                else if (value.getClass()==Short.class)
                    strVal=Short.toString((Short)value);
                else if (value.getClass()==Integer.class)
                    strVal=Integer.toString((Integer)value);
                else if (value.getClass()==Long.class)
                    strVal=Long.toString((Long)value);
                else if (value.getClass()==Float.class)
                    strVal=Float.toString((Float)value);
                else if (value.getClass()==Double.class)
                    strVal=Double.toString((Double)value);
                else if (value.getClass()==java.sql.Date.class)
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    strVal=format.format((java.sql.Date)value);
                }
                else if (value.getClass()==java.sql.Time.class)
                {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    strVal=format.format((java.sql.Time)value);
                }
                else if (value.getClass()==String.class)
                    strVal=(String)value;
            }

            list.add(new FeatureAtt(f.name(),strVal,ucFeatureLayer,feature.geometry()));
        }
        LayerAtt layerAtt = new LayerAtt();
        layerAtt.layerName = ucFeatureLayer.getName();
        layerAtt.setFlds(list);
        layerAtts.add(layerAtt);
        //handler.dispatchMessage(new Message());
        //singleAttlistView.setAdapter(new GroupAdapter(super.context,layerAtts));
        singleAttlistView.setAdapter(new TapQueryResultGridAdapter(layerAtts));

    }

    @Override
    public boolean onDown(MotionEvent e) {
        layerAtts.clear();
        if(highLightLayer != null){
            mapView.deleteLayer(highLightLayer);
        }
        singleAttlistView.setAdapter(new TapQueryResultGridAdapter(layerAtts));
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }



    public class SearchQueryResultGridAdapter extends BaseAdapter {

        private List<LayerAtt> layerAtts;
        public SearchQueryResultGridAdapter(List<LayerAtt> layerAtts){
            this.layerAtts = layerAtts;
        }
        @Override
        public int getCount() {
            return this.layerAtts.size();
        }

        @Override
        public Object getItem(int position) {
            return this.layerAtts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = View.inflate(context, R.layout.widget_view_query_attributequery_result_list, null);
            }

            TextView tvName =  convertView.findViewById(R.id.layer_name);
            tvName.setText(layerAtts.get(position).layerName);
            if(layerAtts.get(position).layerName.equals("")){
                tvName.setVisibility(View.GONE);
            }

            List<FeatureAtt> flds = layerAtts.get(position).flds;
            TextView tvFldList = convertView.findViewById(R.id.fld_list);
            StringBuilder stringBuilder = new StringBuilder();
            for (FeatureAtt att: flds){
                stringBuilder.append(att.name);
                stringBuilder.append(":");
                if(att.value == null || "".equals(att.value)){
                    stringBuilder.append("<空>");
                }
                else{
                    stringBuilder.append(att.value);
                }
                stringBuilder.append("\n");
            }
            if(flds.size()>0){
                final FeatureAtt featureAtt = flds.get(0);
                if(featureAtt!= null){
                    tvFldList.setText(stringBuilder);
                    tvFldList.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(highLightLayer != null){
                                mapView.deleteLayer(highLightLayer);
                            }
                            highLightLayer = mapView.addVectorLayer();

                            UCLayerWapper ucLayerWapper = new UCLayerWapper("临时",featureAtt.layer,UCLayerWapper.UCLAYER_TYPE_FEATURE);
                            if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE){
                                highLightLayer.addLine(featureAtt.geomerty,5,0xffff0000);
                            }
                            else if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT){
                                highLightLayer.addPoint(featureAtt.geomerty,0.1,0xffff0000,0.8f);
                            }
                            else if(ucLayerWapper.getGeomertyType() == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON){
                                highLightLayer.addPolygon(featureAtt.geomerty,5,0xffff0000,0xff00ff00,0.8f);
                            }
                            mapView.refresh();
                            Envelope envelope = featureAtt.geomerty.getEnvelopeInternal();
                            if(envelope != null){
                                mapView.refresh(500,envelope);
                            }
                            else{
                                mapView.animateTo(500,featureAtt.geomerty.getCentroid().getX(),featureAtt.geomerty.getCentroid().getY(),4096);
                            }


                        }
                    });
                }
            }



            return convertView;

        }


    }

    public class AttMedia{

        public static final int MEDIA_TYPE_VIDEO = 0;
        public static final int MEDIA_TYPE_VOICE = 1;
        public static final int MEDIA_TYPE_PHOTO = 2;
        public String name;

        public AttMedia(String name,int type){
            this.name = name;
            this.MediaType = type;
        }
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMediaType() {
            return MediaType;
        }

        public void setMediaType(int mediaType) {
            MediaType = mediaType;
        }

        public int MediaType;
    }


    public class TapQueryResultGridAdapter extends BaseAdapter {

        private List<LayerAtt> layerAtts;
        public TapQueryResultGridAdapter(List<LayerAtt> layerAtts){
            this.layerAtts = layerAtts;
        }
        @Override
        public int getCount() {
            return this.layerAtts.size();
        }

        @Override
        public Object getItem(int position) {
            return this.layerAtts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final List<AttMedia> medias = new ArrayList<>();
            if(convertView == null){
                convertView = View.inflate(context, R.layout.widget_view_query_attributequery_result_list, null);
            }

            TextView tvName =  convertView.findViewById(R.id.layer_name);
            tvName.setText(layerAtts.get(position).layerName);
            if(layerAtts.get(position).layerName.equals("")){
                tvName.setVisibility(View.GONE);
            }

            List<FeatureAtt> flds = layerAtts.get(position).flds;
            TextView tvFldList = convertView.findViewById(R.id.fld_list);
            StringBuilder stringBuilder = new StringBuilder();
            for (FeatureAtt att: flds){
                if(att.isVoice()){
                    medias.add(new AttMedia(att.value,AttMedia.MEDIA_TYPE_VOICE));
                    continue;
                }
                if(att.isPhoto()){
                    medias.add(new AttMedia(att.value,AttMedia.MEDIA_TYPE_PHOTO));
                    continue;
                }
                if(att.isVideo()){
                    medias.add(new AttMedia(att.value,AttMedia.MEDIA_TYPE_VIDEO));
                    continue;
                }
                stringBuilder.append(att.name);
                stringBuilder.append(":");
                if(att.value == null || "".equals(att.value)){
                    stringBuilder.append("<空>");
                }
                else{
                    stringBuilder.append(att.value);
                }
                stringBuilder.append("\n");
            }
            tvFldList.setText(stringBuilder);
            if(medias.size() >0){
                convertView.findViewById(R.id.medias).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
                        final View digView = LayoutInflater.from(context).inflate(R.layout.medias_dlg, null);
                        GridView listView = digView.findViewById(R.id.media_grid);
                        listView.setAdapter(new BaseAdapter() {
                            @Override
                            public int getCount() {
                                return medias.size();
                            }

                            @Override
                            public Object getItem(int position) {
                                return medias.get(position);
                            }

                            @Override
                            public long getItemId(int position) {
                                return 0;
                            }

                            @Override
                            public View getView(final int position, View convertView, ViewGroup parent) {
                                View itemView = LayoutInflater.from(context).inflate(R.layout.medias_dlg_item, null);
                                final String path = projectPath + File.separator + "media"+File.separator +medias.get(position).name;
                                TextView tvName =  itemView.findViewById(R.id.media_name);
                                ImageView imageView =  itemView.findViewById(R.id.media_icon);
                                tvName.setText(medias.get(position).name);
                                if(medias.get(position).MediaType == AttMedia.MEDIA_TYPE_VIDEO){
                                    imageView.setImageResource(R.drawable.video_item);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {


                                            PlayVideo.play(context,path);
                                        }
                                    });
                                }
                                else if(medias.get(position).MediaType == AttMedia.MEDIA_TYPE_PHOTO){
                                    imageView.setImageResource(R.drawable.video_item);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            PlayPhoto.show(context,path);
                                        }
                                    });
                                }
                                else if(medias.get(position).MediaType == AttMedia.MEDIA_TYPE_VOICE){
                                    imageView.setImageResource(R.drawable.voice_item);
                                    imageView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            PlayVoice.play(context,path);
                                        }
                                    });
                                }

                                return itemView;
                            }
                        });

                        opacityDialog.setTitle("多媒体附件");
                        opacityDialog.setView(digView);
                        opacityDialog.show();
                    }
                });
            }
            else{
                convertView.findViewById(R.id.medias).setVisibility(View.INVISIBLE);
            }



            return convertView;
        }


    }

}

