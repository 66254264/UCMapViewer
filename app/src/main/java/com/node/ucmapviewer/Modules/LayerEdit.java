package com.node.ucmapviewer.Modules;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.natewickstrom.rxactivityresult.ActivityResult;
import com.natewickstrom.rxactivityresult.RxActivityResult;
import com.node.ucmapviewer.FrameWork.MapModule.Base.BaseModule;
import com.node.ucmapviewer.FrameWork.MapModule.Map.PanTool;
import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;
import com.node.ucmapviewer.Modules.LayerEditModule.AddFeatureTool;
import com.node.ucmapviewer.Modules.LayerEditModule.CutTool;
import com.node.ucmapviewer.Modules.LayerEditModule.DeleteFeatureTool;
import com.node.ucmapviewer.Modules.LayerEditModule.DigTool;
import com.node.ucmapviewer.Modules.LayerEditModule.EditFeatureTool;
import com.node.ucmapviewer.Modules.LayerEditModule.EditOperation;
import com.node.ucmapviewer.Modules.LayerEditModule.LineStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.MapOperTool;
import com.node.ucmapviewer.Modules.LayerEditModule.MergeTool;
import com.node.ucmapviewer.Modules.LayerEditModule.PointStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.PolygonStyle;
import com.node.ucmapviewer.Modules.LayerEditModule.QueryFeatureTool;
import com.node.ucmapviewer.Modules.LayerEditModule.UndoRedo;
import com.node.ucmapviewer.R;
import com.node.ucmapviewer.Utils.BluRecorder;
import com.node.ucmapviewer.Utils.LogUtils;
import com.node.ucmapviewer.Utils.ToastUtils;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import rx.functions.Action1;


/**
 * 图层编辑挂件
 */
public class LayerEdit extends BaseModule {

    public View mWidgetView = null;//

    private Spinner spinner;


    /**
     * 组件面板打开时，执行的操作
     * 当点击widget按钮是, WidgetManager将会调用这个方法，面板打开后的代码逻辑.
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void active() {

        super.active();//默认需要调用，以保证切换到其他widget时，本widget可以正确执行inactive()方法并关闭
        super.showWidget(mWidgetView);//加载UI并显示
        /**
         * 初始化可查询图层列表
         */
        initLayerSpinner();

        //===============绑定工具=======================

        //添加点
        mWidgetView.findViewById(R.id.add_point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(R.drawable.marker_poi);
                BitmapDrawable bd2 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.cross);
                MapOperTool curMapOprTool = new AddFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer(), bd.getBitmap(), bd2.getBitmap());
                //curMapOprTool.start();
                mapView.startOperTool(curMapOprTool);
            }
        });
        //删除点
        mWidgetView.findViewById(R.id.delete_point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new DeleteFeatureTool(mapView, layerWapper);
                //curMapOprTool.start();
                mapView.startOperTool(curMapOprTool);
            }
        });

        //移动点
        mWidgetView.findViewById(R.id.move_point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new EditFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer());
                //curMapOprTool.start();
                mapView.startOperTool(curMapOprTool);
            }
        });


        //点属性
        mWidgetView.findViewById(R.id.point_att).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                //UCLayerWapper layerWapper = editLayers.get(0);
                MapOperTool curMapOprTool = new QueryFeatureTool(mapView, layerWapper, new QueryFeatureTool.QueryFeatureListener() {
                    @Override
                    public void hit(UCFeatureLayer layerFeature, Feature feature) {
                        showFeatureAttDlg(layerFeature, feature);
                    }
                });
                mapView.startOperTool(curMapOprTool);
            }
        });

        //线属性
        mWidgetView.findViewById(R.id.line_att).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                //UCLayerWapper layerWapper = editLayers.get(0);
                MapOperTool curMapOprTool = new QueryFeatureTool(mapView, layerWapper, new QueryFeatureTool.QueryFeatureListener() {
                    @Override
                    public void hit(UCFeatureLayer layerFeature, Feature feature) {
                        showFeatureAttDlg(layerFeature, feature);
                    }
                });
                mapView.startOperTool(curMapOprTool);
            }
        });

        //添加线
        mWidgetView.findViewById(R.id.add_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                //UCLayerWapper layerWapper = editLayers.get(0);
                BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(R.drawable.marker_poi);
                BitmapDrawable bd2 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.cross);
                MapOperTool curMapOprTool = new AddFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer(), bd.getBitmap(), bd2.getBitmap());
                mapView.startOperTool(curMapOprTool);
            }
        });

        //编辑线节点
        mWidgetView.findViewById(R.id.edit_line_geomerty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new EditFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer());
                mapView.startOperTool(curMapOprTool);
            }
        });


        //删除线
        mWidgetView.findViewById(R.id.delete_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new DeleteFeatureTool(mapView, layerWapper);
                mapView.startOperTool(curMapOprTool);
            }
        });

        //添加面
        mWidgetView.findViewById(R.id.add_polygon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();

                BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(R.drawable.marker_poi);
                BitmapDrawable bd2 = (BitmapDrawable) context.getResources().getDrawable(R.drawable.cross);
                MapOperTool curMapOprTool = new AddFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer(), bd.getBitmap(), bd2.getBitmap());
                mapView.startOperTool(curMapOprTool);
            }
        });

        //修改面节点
        mWidgetView.findViewById(R.id.
                edit_polygon_gepmerty).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new EditFeatureTool(mapView, (UCFeatureLayer) layerWapper.getLayer());
                mapView.startOperTool(curMapOprTool);
            }
        });


        //删除面
        mWidgetView.findViewById(R.id.delete_polygon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new DeleteFeatureTool(mapView, layerWapper);
                mapView.startOperTool(curMapOprTool);
            }
        });

        //面属性
        mWidgetView.findViewById(R.id.polygon_att).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                MapOperTool curMapOprTool = new QueryFeatureTool(mapView, layerWapper, new QueryFeatureTool.QueryFeatureListener() {
                    @Override
                    public void hit(UCFeatureLayer layerFeature, Feature feature) {
                        //ToastUtils.showLong(context,"查到了");
                        showFeatureAttDlg(layerFeature, feature);
                    }
                });
                mapView.startOperTool(curMapOprTool);
            }
        });

        //面-裁切
        mWidgetView.findViewById(R.id.polygon_cut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UCLayerWapper layerWapper=(UCLayerWapper)spinner.getSelectedItem();
                CutTool tool=new CutTool(mapView,(UCFeatureLayer)layerWapper.getLayer());
                mapView.startOperTool(tool);
            }
        });

        //面-合并
        mWidgetView.findViewById(R.id.polygon_merge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UCLayerWapper layerWapper=(UCLayerWapper)spinner.getSelectedItem();
                MergeTool tool=new MergeTool(mapView,(UCFeatureLayer)layerWapper.getLayer());
                mapView.startOperTool(tool);
            }
        });

        //面-挖空
        mWidgetView.findViewById(R.id.polygon_dig).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UCLayerWapper layerWapper=(UCLayerWapper)spinner.getSelectedItem();
                BitmapDrawable bd = (BitmapDrawable) context.getResources().getDrawable(R.drawable.marker_poi);
                DigTool tool=new DigTool(mapView,(UCFeatureLayer)layerWapper.getLayer(),bd.getBitmap());
                mapView.startOperTool(tool);
            }
        });

        //撤销
        mWidgetView.findViewById(R.id.undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UndoRedo.getInstance().undo();
                mapView.refresh();
            }
        });

        //重做
        mWidgetView.findViewById(R.id.redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UndoRedo.getInstance().redo();
                mapView.refresh();
            }
        });

        //保存
        mWidgetView.findViewById(R.id.save_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                UCFeatureLayer featureLayer = (UCFeatureLayer) layerWapper.getLayer();
                featureLayer.saveShapefile(layerWapper.getPath());
                ToastUtils.showShort(context, "保存成功");
            }
        });

        //新建图层
        mWidgetView.findViewById(R.id.new_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
                final View digView = LayoutInflater.from(context).inflate(R.layout.create_new_layer, null);

                final ListView fldsList = (ListView) digView.findViewById(R.id.flds_list);
                final ArrayList<FldListItemData> data = new ArrayList<>();
                data.add(new FldListItemData("id", ogr.OFTInteger, true));

                final BaseAdapter fldListAdapter = new BaseAdapter() {


                    @Override
                    public int getCount() {
                        return data.size();
                    }

                    @Override
                    public Object getItem(int i) {
                        return data.get(i);
                    }

                    @Override
                    public long getItemId(int i) {
                        return i;
                    }

                    @Override
                    public View getView(final int i, View view, ViewGroup viewGroup) {
                        final BaseAdapter ag = this;
                        View v = LayoutInflater.from(context).inflate(R.layout.layer_fld_item, null, false);
                        v.findViewById(R.id.btn_remove).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                data.remove(i);
                                ag.notifyDataSetChanged();
                            }
                        });
                        TextView tvFldName = (TextView) v.findViewById(R.id.fld_name);
                        tvFldName.setText(data.get(i).fldName);
                        TextView tvFldType = (TextView) v.findViewById(R.id.fld_type);
                        tvFldType.setText(fldTypeInt2String(data.get(i).fldType));

                        //默认字段不可移除
                        if (data.get(i).isDefault) {
                            v.findViewById(R.id.btn_remove).setVisibility(View.INVISIBLE);
                        } else {
                            v.findViewById(R.id.btn_remove).setVisibility(View.VISIBLE);
                        }

                        return v;
                    }

                    String fldTypeInt2String(int type) {
                        switch (type) {
                            case ogr.OFTInteger:
                                return "整型";
                            case ogr.OFSTFloat32:
                                return "浮点型";
                            case ogr.OFTString:
                                return "字符串型";
                            case ogr.OFTDateTime:
                                return "日期型";
                            default:
                                return "未知类型";
                        }
                    }
                };
                fldsList.setAdapter(fldListAdapter);


                final String[] newfldType = {"整型"};
                final String[] newLayerType = {"点图层"};
                final EditText editTextNewFld = (EditText) digView.findViewById(R.id.new_fld_name);
                final boolean[] flag = {false};
                editTextNewFld.setFilters(new InputFilter[]{new CustomCoinNameFilter(11)}); //字段程度不能超过11
                editTextNewFld.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {


                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (flag[0]) {
                            return;
                        }
                        flag[0] = true;

                        if (newfldType[0].equals("照片类型")) {
                            if (!s.toString().startsWith("P_")) {
                                editTextNewFld.setText("P_" + s);
                            }

                        } else if (newfldType[0].equals("视频类型")) {
                            if (!s.toString().startsWith("V_")) {
                                editTextNewFld.setText("V_" + s);
                            }

                        } else if (newfldType[0].equals("音频类型")) {
                            if (!s.toString().startsWith("S_")) {
                                editTextNewFld.setText("S_" + s);
                            }
                        }
                        else{
                            //新建内容start 处理中文
                            String str = editTextNewFld.getText().toString();

                            Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                            Matcher m = p.matcher(str);
                            editTextNewFld.setText(m.replaceAll("").trim());
                            editTextNewFld.setSelection(editTextNewFld.getText().length());
                        }




                        //新建内容end
                        flag[0] = false;
                    }
                });

                digView.findViewById(R.id.add_fld).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (editTextNewFld.getText() == null) {
                            ToastUtils.showShort(context, "字段名不能为空");
                            return;
                        }

                        String newFldName = editTextNewFld.getText().toString();
                        for (FldListItemData i : data) {
                            if (i.fldName.equals(newFldName)) {
                                ToastUtils.showShort(context, "字段名" + i.fldName + "已经存在!");

                                return;
                            }
                        }
                        int inewfldType = ogr.OFTInteger;
                        if (newfldType[0].equals("整型")) {
                            inewfldType = ogr.OFTInteger;
                        } else if (newfldType[0].equals("浮点型")) {
                            inewfldType = ogr.OFSTFloat32;
                        } else if (newfldType[0].equals("字符串型")) {
                            inewfldType = ogr.OFTString;
                        } else if (newfldType[0].equals("日期型")) {
                            inewfldType = ogr.OFTDateTime;
                        } else if (newfldType[0].equals("照片类型")) {
                            inewfldType = ogr.OFTString;
                        } else if (newfldType[0].equals("视频类型")) {
                            inewfldType = ogr.OFTString;
                        } else if (newfldType[0].equals("音频类型")) {
                            inewfldType = ogr.OFTString;
                        }
                        data.add(new FldListItemData(newFldName, inewfldType));
                        fldListAdapter.notifyDataSetChanged();

                    }
                });
                final Spinner layerType = (Spinner) digView.findViewById(R.id.layer_type);
                ArrayAdapter<String> layerTypeAdapter = new ArrayAdapter<String>(context, R.layout.spinner_style, new String[]{"点图层", "线图层", "面图层"});
                layerTypeAdapter.setDropDownViewResource(R.layout.spinner_drown);
                layerType.setAdapter(layerTypeAdapter);
                layerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        newLayerType[0] = layerType.getSelectedItem().toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                final Spinner fldType = (Spinner) digView.findViewById(R.id.sp_fld_type);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.spinner_style, new String[]{"整型", "浮点型", "字符串型", "日期型", "照片类型", "视频类型", "音频类型"});
                adapter.setDropDownViewResource(R.layout.spinner_drown);
                fldType.setAdapter(adapter);
                fldType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        newfldType[0] = fldType.getSelectedItem().toString();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });


                opacityDialog.setTitle("新建图层");
                opacityDialog.setView(digView);
                //opacityDialog.getWindow().setBackgroundDrawableResource(R.drawable.apl_bg);
                opacityDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newLayerName = ((EditText) digView.findViewById(R.id.new_layer_name)).getText().toString();
                        if (newLayerName.equals(null)) {
                            ToastUtils.showShort(context, "请输入图层名");
                            return;
                        }
                        int inewlayerType = ogr.wkbPoint;
                        if (newLayerType[0].equals("点图层")) {
                            inewlayerType = ogr.wkbPoint;
                        } else if (newLayerType[0].equals("线图层")) {
                            inewlayerType = ogr.wkbLineString;
                        } else if (newLayerType[0].equals("面图层")) {
                            inewlayerType = ogr.wkbPolygon;
                        }

                        final Map<String, Integer> flds = new HashMap<>();
                        for (FldListItemData i1 : data) {
                            flds.put(i1.fldName, i1.fldType);
                        }

                        final int finalInewlayerType = inewlayerType;

                        String path = projectPath + File.separator + "layers" + File.separator + newLayerName + ".shp";
                        boolean ok = createShp(path, newLayerName, finalInewlayerType, flds);
                        if (ok) {
                            opacityDialog.dismiss();
                            ToastUtils.showShort(context, "创建成功");
                            UCFeatureLayer newFeatureLayer = mapView.addFeatureLayer(newLayerName, path, null, true, true);
                            newFeatureLayer.loadShapefile(path, true);
                            initLayerSpinner();
//
                            if (finalInewlayerType == ogr.wkbPoint) {
                                PointStyle pointStyle = new PointStyle();
                                newFeatureLayer.setStyle(pointStyle.getPointSize(), 0, pointStyle.getPointColor(), pointStyle.getPointColor());

                                mapView.getProjectTree().addShapeFileLayer(newLayerName, newLayerName + ".shp", true, true, new PointStyle());
                            } else if (finalInewlayerType == ogr.wkbLineString) {
                                LineStyle lineStyle = new LineStyle();
                                newFeatureLayer.setStyle(lineStyle.getLineWidth(), lineStyle.getLineWidth(), lineStyle.getLineColor(), lineStyle.getLineColor());
                                mapView.getProjectTree().addShapeFileLayer(newLayerName, newLayerName + ".shp", true, true, new LineStyle());
                            } else if (finalInewlayerType == ogr.wkbPolygon) {
                                PolygonStyle polygonStyle = new PolygonStyle();
                                newFeatureLayer.setStyle(polygonStyle.getLineWidth(), polygonStyle.getLineWidth(), polygonStyle.getLineColor(), polygonStyle.getFillColor());
                                mapView.getProjectTree().addShapeFileLayer(newLayerName, newLayerName + ".shp", true, true, new PolygonStyle());
                            }

                        } else {
                            ToastUtils.showShort(context, "创建失败,发生内部异常");
                        }


                    }
                });
                opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        opacityDialog.dismiss();
                    }
                });
                opacityDialog.show();
            }
        });

    }

    private void showFeatureAttDlg(final UCFeatureLayer layer, final Feature feature) {

        final List<FeatureAtt> list = new ArrayList<>();
        Map<String, Object> att = feature.map();

        int fldNum = layer.getFieldCount();
        for (int i = 0; i < fldNum; i++) {
            Field f = layer.getField(i);
            if (f.name().equals("geometry")) {
                continue;
            }

            Object value = feature.get(f.name());

            String strVal = "";

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


            list.add(new FeatureAtt(f.name(), strVal, layer, feature.geometry()));
        }


        final AlertDialog opacityDialog = new AlertDialog.Builder(context).create();
        final View digView = LayoutInflater.from(context).inflate(R.layout.layer_edit_feature_att_dlg, null);
        ListView listView = digView.findViewById(R.id.feature_att_list);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int position) {
                return list.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.widget_view_layeredit_att_item, null);
                TextView tvName = itemView.findViewById(R.id.fld_name);
                View camera = itemView.findViewById(R.id.camera);
                View video = itemView.findViewById(R.id.video);
                View voice = itemView.findViewById(R.id.voice);
                final EditText tvVal = itemView.findViewById(R.id.fld_val);
                if (list.get(position).isPhoto()) {
                    camera.setVisibility(View.VISIBLE);
                    camera.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            String photo = projectPath + File.separator + "media" + File.separator + System.currentTimeMillis() + ".jpg";
                            final File file = new File(photo);
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            Activity act = (Activity) context;
                            RxActivityResult.getInstance(context).from(act)
                                    .startActivityForResult(intent, 10001)
                                    .subscribe(new Action1<ActivityResult>() {
                                        @Override
                                        public void call(ActivityResult result) {
                                            if (result.getRequestCode() == 10001 && result.isOk()) {
                                                tvVal.setText(file.getName());
                                            }
                                        }
                                    });


                        }
                    });
                } else if (list.get(position).isVideo()) {
                    video.setVisibility(View.VISIBLE);
                    video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String photo = projectPath + File.separator + "media" + File.separator + System.currentTimeMillis() + ".mp4";

                            Intent intent = new Intent();
                            intent.setAction("android.media.action.VIDEO_CAPTURE");
                            intent.addCategory("android.intent.category.DEFAULT");
                            final File file = new File(photo);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                            Activity act = (Activity) context;
                            RxActivityResult.getInstance(context).from(act)
                                    .startActivityForResult(intent, 10002)
                                    .subscribe(new Action1<ActivityResult>() {
                                        @Override
                                        public void call(ActivityResult result) {
                                            if (result.getRequestCode() == 10002 && result.isOk()) {
                                                tvVal.setText(file.getName());
                                            }
                                        }
                                    });
                            act.startActivityForResult(intent, 10002);
                        }
                    });
                } else if (list.get(position).isVoice()) {
                    voice.setVisibility(View.VISIBLE);
                    voice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String path = projectPath + File.separator + "media" + File.separator + System.currentTimeMillis() + ".mp3";


                            final File file = new File(path);
                            if (!file.getParentFile().exists()) {
                                file.getParentFile().mkdirs();
                            }
                            new BluRecorder().take(context, path, new Supplier() {
                                @Override
                                public Object get() {
                                    tvVal.setText(file.getName());
                                    return null;
                                }
                            });
                        }
                    });

                }
                tvName.setText(list.get(position).name);

                if (!list.get(position).value.equals(null)) {
                    tvVal.setText(list.get(position).value);
                }

                Field f = layer.getField(list.get(position).name);
                if (f.type() == Byte.class)
                    tvVal.setInputType(InputType.TYPE_NULL);
                else if (f.type() == Short.class)
                    tvVal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else if (f.type() == Integer.class)
                    tvVal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else if (f.type() == Long.class)
                    tvVal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else if (f.type() == Float.class)
                    tvVal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else if (f.type() == Double.class)
                    tvVal.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                else if (f.type() == java.sql.Date.class) {
                    tvVal.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                } else if (f.type() == java.sql.Time.class) {
                    tvVal.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
                } else if (f.type() == String.class) {
                    tvVal.setInputType(InputType.TYPE_NULL);
                }


                tvVal.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        list.get(position).value = s.toString();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                return itemView;
            }
        });

        opacityDialog.setTitle("图元属性编辑");
        opacityDialog.setView(digView);
        //opacityDialog.getWindow().setBackgroundDrawableResource(R.drawable.apl_bg);
        opacityDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Hashtable<String, Object> newFeature = new Hashtable<String, Object>();
                newFeature.put("geometry", feature.geometry());
                try {

                    for (int i = 0; i < list.size(); ++i) {
                        Field f = layer.getField(list.get(i).name);
                        if(list.get(i).value != null && !"".equals(list.get(i).value)){
                            if (f.type() == Byte.class)
                                newFeature.put(list.get(i).name, Byte.parseByte(list.get(i).value));
                            else if (f.type() == Short.class)
                                newFeature.put(list.get(i).name, Short.parseShort(list.get(i).value));
                            else if (f.type() == Integer.class)
                                newFeature.put(list.get(i).name, Integer.parseInt(list.get(i).value));
                            else if (f.type() == BigDecimal.class)
                                newFeature.put(list.get(i).name, Integer.parseInt(list.get(i).value));

                            else if (f.type() == Long.class)
                                newFeature.put(list.get(i).name, Long.parseLong(list.get(i).value));
                            else if (f.type() == Float.class)
                                newFeature.put(list.get(i).name, Float.parseFloat(list.get(i).value));
                            else if (f.type() == Double.class)
                                newFeature.put(list.get(i).name, Double.parseDouble(list.get(i).value));
                            else if (f.type() == java.sql.Date.class) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                newFeature.put(list.get(i).name, format.parse(list.get(i).value));
                            } else if (f.type() == java.sql.Time.class) {
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                newFeature.put(list.get(i).name, new java.sql.Time(format.parse(list.get(i).value).getTime()));
                            } else if (f.type() == String.class)
                                newFeature.put(list.get(i).name, list.get(i).value);
                        }

                    }
                    Hashtable<String, Object> values1 = new Hashtable<String, Object>();
                    for (Field f : feature.schema())
                        if (feature.get(f.name()) != null)
                            values1.put(f.name(), feature.get(f.name()));
                    Feature ft = layer.updateFeature(feature, newFeature);
                    UndoRedo.getInstance().addUndo(EditOperation.UpdateFeature, layer, new BasicFeature(null, values1), ft);

                    mapView.refresh();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                opacityDialog.dismiss();
            }
        });
        opacityDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                opacityDialog.dismiss();
            }
        });
        opacityDialog.show();
        opacityDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

    }

    private void initLayerSpinner() {
        final List<UCLayerWapper> editLayers = new ArrayList<>();
        spinner = mWidgetView.findViewById(R.id.edit_layers);
        int layerCount = super.mapView.getLayerCount();
        for (int i = 0; i < layerCount; i++) {
            UCLayerWapper layerWapper = super.mapView.getLayerWapper(i);
            if (layerWapper.getLayerType() == UCLayerWapper.UCLAYER_TYPE_FEATURE && layerWapper.isCanEdit()) {
                editLayers.add(layerWapper);
            }
        }

        spinner.setAdapter(new LayerSpinnerAdapter(super.context, editLayers));
        final UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
        if (layerWapper != null) {
            setFeatureStatus(layerWapper.getGeomertyType());

        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UCLayerWapper layerWapper = (UCLayerWapper) spinner.getSelectedItem();
                if (layerWapper != null) {
                    setFeatureStatus(layerWapper.getGeomertyType());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean createShp(String shpPath, String name, int geomType, Map<String, Integer> flds) {

        ogr.RegisterAll();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");

        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName + " 驱动不可用！\n");
            return false;
        }
        //String shpPath=Environment.getExternalStorageDirectory().getPath() + "/geodata/utf8.shp";
        DataSource oDS = oDriver.CreateDataSource(shpPath, null);
        if (oDS == null) {
            System.out.println("创建矢量文件【" + shpPath + "】失败！\n");
            return false;
        }

        org.gdal.ogr.Layer oLayer = oDS.CreateLayer(name, null, geomType, null);
        if (oLayer == null) {
            System.out.println("图层创建失败！\n");
            return false;
        }
        for (Map.Entry<String, Integer> entry : flds.entrySet()) {
            FieldDefn oFieldID = new FieldDefn(entry.getKey(), entry.getValue());
            if (entry.getValue() == ogr.OFTString) {
                oFieldID.SetWidth(255);
            }

            int ret = oLayer.CreateField(oFieldID);
            if (ret < 0) {
                return false;
            }
        }

        FeatureDefn oDefn = oLayer.GetLayerDefn();

        org.gdal.ogr.Feature oFeatureTriangle = new org.gdal.ogr.Feature(oDefn);
        oFeatureTriangle.SetField("id", 0);

        org.gdal.ogr.Geometry geo = null;
        switch (geomType) {
            case ogr.wkbPoint:
                geo = org.gdal.ogr.Geometry.CreateFromWkt("POINT (0 0)");
                break;
            case ogr.wkbLineString:
                geo = org.gdal.ogr.Geometry.CreateFromWkt("LINESTRING (0 0,20 0,10 15,0 0)");
                break;
            case ogr.wkbPolygon:
                geo = org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");
                break;
        }

        oFeatureTriangle.SetGeometry(geo);
        int ret = oLayer.CreateFeature(oFeatureTriangle);
        oLayer.DeleteFeature(ret);
        try {
            oLayer.SyncToDisk();
            oDS.SyncToDisk();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("\n数据集创建完成！\n");

        return true;
    }


    private void setFeatureStatus(int geomertyType) {
        mWidgetView.findViewById(R.id.point_tool).setVisibility(View.GONE);
        mWidgetView.findViewById(R.id.line_tool).setVisibility(View.GONE);
        mWidgetView.findViewById(R.id.polygon_tool).setVisibility(View.GONE);
        if (geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POINT) {
            mWidgetView.findViewById(R.id.point_tool).setVisibility(View.VISIBLE);
        } else if (geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_LINE) {
            mWidgetView.findViewById(R.id.line_tool).setVisibility(View.VISIBLE);
        } else if (geomertyType == UCLayerWapper.FEATURE_GEOMERTY_TYPE_POLYGON) {
            mWidgetView.findViewById(R.id.polygon_tool).setVisibility(View.VISIBLE);
        }
    }


    /**
     * widget组件的初始化操作，包括设置view内容，逻辑等
     * 该方法在应用程序加载完成后执行
     */
    @Override
    public void create() {
        final LayoutInflater mLayoutInflater = LayoutInflater.from(super.context);
        //设置widget组件显示内容
        mWidgetView = mLayoutInflater.inflate(R.layout.widget_view_feature_edit, null);


    }

    /**
     * 组件面板关闭时，执行的操作
     * 面板关闭将会调用 "inactive" 方法
     */
    @Override
    public void inactive() {
        super.inactive();
        MapOperTool tool = new PanTool(mapView);
        mapView.startOperTool(tool);
    }

    class FldListItemData {
        public boolean isDefault;

        public String fldName;

        public int fldType;

        FldListItemData(String name, int fldType) {
            this.fldName = name;
            this.fldType = fldType;
            this.isDefault = false;
        }

        FldListItemData(String name, int fldType, boolean isDefault) {
            this.fldName = name;
            this.fldType = fldType;
            this.isDefault = isDefault;
        }
    }

    public class CustomCoinNameFilter implements InputFilter {

        private int maxLength;//最大长度，ASCII码算一个，其它算两个

        public CustomCoinNameFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (TextUtils.isEmpty(source)) {
                return null;
            }

            int inputCount = 0;
            int destCount = 0;
            inputCount = getCurLength(source);

            if (dest.length() != 0)
                destCount = getCurLength(dest);

            if (destCount >= maxLength)
                return "";
            else {

                int count = inputCount + destCount;
                if (dest.length() == 0) {
                    if (count <= maxLength)
                        return null;
                    else
                        return sub(source, maxLength);
                }

                if (count > maxLength) {
                    //int min = count - maxLength;
                    int maxSubLength = maxLength - destCount;
                    return sub(source, maxSubLength);
                }
            }
            return null;
        }

        private CharSequence sub(CharSequence sq, int subLength) {
            int needLength = 0;
            int length = 0;
            for (int i = 0; i < sq.length(); i++) {
                if (sq.charAt(i) < 128)
                    length += 1;
                else
                    length += 2;
                ++needLength;
                if (subLength <= length) {
                    return sq.subSequence(0, needLength);
                }
            }
            return sq;
        }

        private int getCurLength(CharSequence s) {
            int length = 0;
            if (s == null)
                return length;
            else {
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) < 128)
                        length += 1;
                    else
                        length += 2;
                }
            }
            return length;
        }
    }
}


