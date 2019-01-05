package com.node.ucmapviewer.Modules.LayerManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;
import com.node.ucmapviewer.Modules.LayerEditModule.LineStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.PointStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.PolygonStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.ProjectTree;
import com.node.ucmapviewer.R;

import org.gdal.ogr.ogr;
import org.jeo.vector.Field;

import java.util.ArrayList;
import java.util.List;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCLayer;
import cn.creable.ucmap.openGIS.UCRasterLayer;
import top.defaults.colorpicker.ColorPickerView;

public class LayerListviewAdapter extends BaseAdapter {

    public class AdapterHolder{//列表绑定项
        public View itemView;
        public Button btnMore;
        public CheckBox cbxLayer;//图层是否选中
        public ImageView layerTypeIcon;//图层类型图标
    }

    private Context context;
    private MapView mapview;
    private List<UCLayerWapper> layers;

    public LayerListviewAdapter(Context c, MapView mapview) {
        this.mapview = mapview;
        this.context = c;
        this.layers = mapview.getLayerWappers();
    }

    /**
     * 刷新数据
     */
    public void refreshData(){
        notifyDataSetChanged();//刷新数据
    }

    @Override
    public int getCount() {
        return this.layers.size();
    }

    @Override
    public Object getItem(int position) {
       return this.layers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        final UCLayerWapper layerWapper = this.layers.get(position);
        AdapterHolder holder = new AdapterHolder();
        convertView = LayoutInflater.from(context).inflate(R.layout.widget_view_layer_managet_layers_item, null);
        holder.itemView = convertView.findViewById(R.id.widget_view_layer_managet_item_view);
        holder.btnMore = (Button)convertView.findViewById(R.id.widget_view_layer_managet_item_btnMore);
        holder.cbxLayer = (CheckBox)convertView.findViewById(R.id.widget_view_layer_managet_item_cbxLayer);
        holder.cbxLayer.setText(mapview.getLayerWapper(position).getName());
        holder.layerTypeIcon = convertView.findViewById(R.id.layer_type_icon);

        if(layerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_RASTER){
            holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_raster);
            holder.btnMore.setVisibility(View.VISIBLE);
        }
        else if(layerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_FEATURE){

            int featureType = layerWapper.getGeomertyType();
            if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT){
                holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_pnt);
            }
            else if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE){
                holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_line);
            }
            else if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON){
                holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_polygon);
            }
            else {
                holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_unknow);
            }

        }
        else {
            holder.layerTypeIcon.setImageResource(R.mipmap.layer_type_unknow);

        }

        holder.cbxLayer.setChecked(layerWapper.getVisible());//设置是否显示

        holder.cbxLayer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layerWapper.setVisible(isChecked);
                ProjectTree projectTree =  mapview.getProjectTree();
                int layerIndex = projectTree.getLayerIndexByName(layers.get(position).getName());
                if(layerIndex > -1){
                    projectTree.setLayerVisible(layerIndex,isChecked);
                }
                mapview.refresh();
            }
        });
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(context, v);
                if(layerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_RASTER){
                    ShowOpacityUtilView(layerWapper);

                }
                else{
                    int featureType = layerWapper.getGeomertyType();
                    if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT){
                        showPointStyleView(layerWapper);
                    }
                    else if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE){
                        showLineStyleView(layerWapper);
                    }
                    else if(featureType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON){
                        showPolygonStyleView(layerWapper);
                    }
                    else{
                        //TODO 暂不支持该类型图层样式的设置
                    }
                }
            }
        });

        return convertView;
    }



    /**
     * 显示透明度操作View
     */
    private void ShowOpacityUtilView(final UCLayerWapper layer){

        final UCRasterLayer rasterLayer = (UCRasterLayer)layer.getLayer();
        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
        View view = LayoutInflater.from(context).inflate( R.layout.widget_alert_opacity, null);
        TextView txtTitle = (TextView)view.findViewById(R.id.opactiy_element_layout_layerName);

        final String layerName = layer.getName();
        txtTitle.setText(layerName);
        final TextView txtOp = (TextView)view.findViewById(R.id.opactiy_element_layout_layerOpacity);
        //float op = layer.getOpacity();
        float op = rasterLayer.getAlpha();
        txtOp.setText(String.valueOf(op));
        SeekBar seekBar = (SeekBar)view.findViewById(R.id.opactiy_element_layout_layerOpactiySeekBar);
        seekBar.setMax(100);
        seekBar.setProgress((int) (op*100));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float op = (float)progress/100;
                txtOp.setText(String.valueOf(op));
                rasterLayer.setAlpha(op);
               ProjectTree projectTree =  mapview.getProjectTree();
               int layerIndex = projectTree.getLayerIndexByName(layerName);
               if(layerIndex > -1){
                   projectTree.setMbtilesStyle(layerIndex,op);
               }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        opacityDialog.setView(view);
        //opacityDialog.getWindow().setBackgroundDrawableResource(R.drawable.apl_bg);

        opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                opacityDialog.dismiss();
            }
        });
        opacityDialog.show();
    }


    private void showPolygonStyleView(final UCLayerWapper layer){
        //TODO 初始化原始参数
        final UCFeatureLayer ucFeatureLayer = (UCFeatureLayer)layer.getLayer();
        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
        final View view = LayoutInflater.from(context).inflate( R.layout.widget_view_layer_manager_polygon_style, null);
        opacityDialog.setTitle("面图层样式");
        SeekBar linWidth = view.findViewById(R.id.line_width);
        ColorPickerView fillColor= view.findViewById(R.id.fill_color);
        ColorPickerView lineColor= view.findViewById(R.id.line_color);
        final CheckBox checkBoxIslLable =  view.findViewById(R.id.check_fldName);
        final Spinner fldNamesSpinner = view.findViewById(R.id.fld_names);
        checkBoxIslLable.setChecked(false);
        checkBoxIslLable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    List<String> listFlds = new ArrayList<>();
                    int fldNum = ucFeatureLayer.getFieldCount();
                    for(int i=0; i < fldNum; i++){
                        Field fld = ucFeatureLayer.getField(i);
                        if(!fld.name().equals("geomerty") && fld.type() == String.class){
                            //目前仅支持标注字符串类型的字段
                            listFlds.add(fld.name());
                        }
                    }
                    ArrayAdapter<String> layerTypeAdapter=new ArrayAdapter<String>(context,R.layout.spinner_style,listFlds);
                    layerTypeAdapter.setDropDownViewResource(R.layout.spinner_drown);
                    fldNamesSpinner.setAdapter(layerTypeAdapter);

                }
            }
        });

        linWidth.setProgress((int)ucFeatureLayer.getStyle().lineWidth);
        fillColor.setInitialColor(colorStr2int(ucFeatureLayer.getStyle().fillColor));
        lineColor.setInitialColor(colorStr2int(ucFeatureLayer.getStyle().lineColor));

        opacityDialog.setView(view);

        opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SeekBar linWidth = view.findViewById(R.id.line_width);
                ColorPickerView fillColor= view.findViewById(R.id.fill_color);
                ColorPickerView lineColor= view.findViewById(R.id.line_color);
                String fillColorStr = intToHexValue(fillColor.getColor());
                String lineColorStr = intToHexValue(lineColor.getColor());
                int lineWidth = linWidth.getProgress();
                ucFeatureLayer.setStyle(0,lineWidth,lineColorStr,fillColorStr);
                ProjectTree projectTree =  mapview.getProjectTree();
                int layerIndex = projectTree.getLayerIndexByName(layer.getName());
                if(layerIndex > -1){
                    PolygonStyle polygonStyle = new PolygonStyle();
                    polygonStyle.setFillColor(fillColorStr);
                    polygonStyle.setLineColor(lineColorStr);
                    polygonStyle.setLineWidth(lineWidth);
                    projectTree.setPolygonLayerStyle(layerIndex,polygonStyle);
                }
                if(checkBoxIslLable.isChecked()){
                    String fld = (String)fldNamesSpinner.getSelectedItem();
                    if(fld != null && !"".equals(fld)){
                        ucFeatureLayer.setNameField(fld);
                    }
                }

                mapview.refresh();
                opacityDialog.dismiss();
            }
        });
        opacityDialog.show();
    }

    private void showPointStyleView(final UCLayerWapper layer){
        //TODO 初始化原始参数
        final UCFeatureLayer ucFeatureLayer = (UCFeatureLayer)layer.getLayer();
        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
        final View view = LayoutInflater.from(context).inflate( R.layout.widget_view_layer_manager_point_style, null);
        opacityDialog.setTitle("点图层样式");

        SeekBar seekBar = view.findViewById(R.id.point_size);
        ColorPickerView colorPickerView= view.findViewById(R.id.point_color);
        final CheckBox checkBoxIslLable =  view.findViewById(R.id.check_fldName);
        final Spinner fldNamesSpinner = view.findViewById(R.id.fld_names);
        checkBoxIslLable.setChecked(false);
        checkBoxIslLable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    List<String> listFlds = new ArrayList<>();
                    int fldNum = ucFeatureLayer.getFieldCount();
                    for(int i=0; i < fldNum; i++){
                        Field fld = ucFeatureLayer.getField(i);
                        if(!fld.name().equals("geomerty") && fld.type() == String.class){
                            //目前仅支持标注字符串类型的字段
                            listFlds.add(fld.name());
                        }
                    }
                    ArrayAdapter<String> layerTypeAdapter=new ArrayAdapter<String>(context,R.layout.spinner_style,listFlds);
                    layerTypeAdapter.setDropDownViewResource(R.layout.spinner_drown);
                    fldNamesSpinner.setAdapter(layerTypeAdapter);

                }
            }
        });


        seekBar.setProgress((int)ucFeatureLayer.getStyle().pointSize);
        colorPickerView.setInitialColor(colorStr2int(ucFeatureLayer.getStyle().fillColor));
        opacityDialog.setView(view);

        //opacityDialog.getWindow().setBackgroundDrawableResource(R.drawable.apl_bg);
        opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SeekBar seekBar = view.findViewById(R.id.point_size);
                ColorPickerView colorPickerView= view.findViewById(R.id.point_color);
                String pointColor = intToHexValue(colorPickerView.getColor());
                int pointSize = seekBar.getProgress();
                ucFeatureLayer.setStyle(pointSize,0,pointColor,pointColor);
                ProjectTree projectTree =  mapview.getProjectTree();
                int layerIndex = projectTree.getLayerIndexByName(layer.getName());
                if(layerIndex > -1){
                    PointStyle pointStyle = new PointStyle();
                    pointStyle.setPointSize(pointSize);
                    pointStyle.setPointColor(pointColor);

                    projectTree.setPointLayerStyle(layerIndex,pointStyle);
                }
                if(checkBoxIslLable.isChecked()){
                    String fld = (String)fldNamesSpinner.getSelectedItem();
                    if(fld != null && !"".equals(fld)){
                        ucFeatureLayer.setNameField(fld);
                    }
                }
                mapview.refresh();
                opacityDialog.dismiss();
            }
        });
        opacityDialog.show();
    }

    private void showLineStyleView(final UCLayerWapper layer){

        //TODO 初始化原始参数
        final UCFeatureLayer ucFeatureLayer = (UCFeatureLayer)layer.getLayer();
        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
        final View view = LayoutInflater.from(context).inflate( R.layout.widget_view_layer_manager_line_style, null);

        final SeekBar seekBar = view.findViewById(R.id.line_width);
        final ColorPickerView colorPickerView= view.findViewById(R.id.line_color);
        final CheckBox checkBoxIslLable =  view.findViewById(R.id.check_fldName);
        final Spinner fldNamesSpinner = view.findViewById(R.id.fld_names);
        checkBoxIslLable.setChecked(false);
        checkBoxIslLable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    List<String> listFlds = new ArrayList<>();
                    int fldNum = ucFeatureLayer.getFieldCount();
                    for(int i=0; i < fldNum; i++){
                        Field fld = ucFeatureLayer.getField(i);
                        if(!fld.name().equals("geomerty") && fld.type() == String.class){
                            //目前仅支持标注字符串类型的字段
                            listFlds.add(fld.name());
                        }
                    }
                    ArrayAdapter<String> layerTypeAdapter=new ArrayAdapter<String>(context,R.layout.spinner_style,listFlds);
                    layerTypeAdapter.setDropDownViewResource(R.layout.spinner_drown);
                    fldNamesSpinner.setAdapter(layerTypeAdapter);

                }
            }
        });

        seekBar.setProgress((int)ucFeatureLayer.getStyle().lineWidth);
        colorPickerView.setInitialColor(colorStr2int(ucFeatureLayer.getStyle().lineColor));
        opacityDialog.setTitle("线图层样式");
        opacityDialog.setView(view);
        opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                String lineColor = intToHexValue(colorPickerView.getColor());
                int lineWidth = seekBar.getProgress();
                ucFeatureLayer.setStyle(0,lineWidth,lineColor,"#ff000000");
                ProjectTree projectTree =  mapview.getProjectTree();
                int layerIndex = projectTree.getLayerIndexByName(layer.getName());
                if(layerIndex > -1){
                    LineStyle lineStyle = new LineStyle();
                    lineStyle.setLineColor(lineColor);
                    lineStyle.setLineWidth(lineWidth);
                    projectTree.setLineLayerStyle(layerIndex,lineStyle);
                }
                if(checkBoxIslLable.isChecked()){
                    String fld = (String)fldNamesSpinner.getSelectedItem();
                    if(fld != null && !"".equals(fld)){
                        ucFeatureLayer.setNameField(fld);
                    }
                }

                mapview.refresh();
                opacityDialog.dismiss();
            }
        });
        //opacityDialog.getWindow().setBackgroundDrawableResource(R.drawable.apl_bg);
        opacityDialog.show();
    }




    private static String intToHexValue(int color) {

        //int alpha = (color & 0xff000000) >>> 24;
        //int red   = (color & 0x00ff0000) >> 16;
        //int green = (color & 0x0000ff00) >> 8;
        //int blue  = (color & 0x000000ff);
        String hexColor = String.format("#%08X", (0xFFFFFFFF & color));

        return hexColor.toUpperCase();
    }





    private static String AlphaintToHexValue(int number) {
        String result = Integer.toHexString(number & 0xff);
        while (result.length() < 2) {
            result = "F" + result;
        }
        return result.toUpperCase();
    }


    private static int colorStr2int(String color) {

        String astr = color.substring(1,3);
        String rstr = color.substring(3,5);
        String gstr = color.substring(5,7);
        String bstr = color.substring(7);
        int alpha = Integer.parseInt(astr , 16);
        int r = Integer.parseInt( rstr, 16);
        int g = Integer.parseInt( gstr, 16);
        int b = Integer.parseInt( bstr, 16);

        return Color.argb(alpha,r,g,b);

    }
    private static float colorStr2getAlpha(String color) {
        String str = color.substring(1,3);
        int i = Integer.parseInt(str, 16);
        return i/255.0f;

    }
}
