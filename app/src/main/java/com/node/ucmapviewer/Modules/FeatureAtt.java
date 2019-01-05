package com.node.ucmapviewer.Modules;

import com.vividsolutions.jts.geom.Geometry;

import cn.creable.ucmap.openGIS.UCFeatureLayer;

public class FeatureAtt{
    public String name;
    public String value;
    public UCFeatureLayer layer;
    public Geometry geomerty;
    public FeatureAtt(String name,String value,UCFeatureLayer layer,Geometry geomerty){
        this.name = name;
        this.value = value;
        this.layer = layer;
        this.geomerty = geomerty;
    }

    public boolean isVoice(){
        return name.startsWith("S_");
    }
    public boolean isVideo(){
        return name.startsWith("V_");
    }
    public boolean isPhoto(){
        return name.startsWith("P_");
    }
}
