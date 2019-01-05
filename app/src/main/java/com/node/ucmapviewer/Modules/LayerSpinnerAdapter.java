package com.node.ucmapviewer.Modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.MapModule.Map.UCLayerWapper;
import com.node.ucmapviewer.R;

import java.util.List;

public class LayerSpinnerAdapter extends BaseAdapter {

    public class AdapterHolder{//列表绑定项
        public ImageView imageView;
        public TextView textView;//图层
    }

    private List<UCLayerWapper> layerList =null;
    private Context context;

    public LayerSpinnerAdapter(Context c, List<UCLayerWapper> list) {
        this.layerList = list;
        this.context = c;
    }

    /**
     * 刷新数据
     */
    public void refreshData(){
        notifyDataSetChanged();//刷新数据
    }

    @Override
    public int getCount() {
       return layerList.size();
    }

    @Override
    public Object getItem(int position) {
        return layerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        AdapterHolder holder = new AdapterHolder();
        convertView = LayoutInflater.from(context).inflate(R.layout.widget_view_query_attributequery_spinner_item, null);
        holder.textView = (TextView) convertView.findViewById(R.id.widget_view_query_attributequery_spinner_item_txtName);

        //仅获取当前显示的layer
        UCLayerWapper layer =layerList.get(position);

        holder.textView.setText(layer.getName());

        return convertView;
    }

}
