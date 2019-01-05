package com.node.ucmapviewer.Modules;

import java.util.ArrayList;
import java.util.List;

public class  LayerAtt{
    public String layerName;

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public List<FeatureAtt> getFlds() {
        return flds;
    }

    public void setFlds(List<FeatureAtt> flds) {
        this.flds = flds;
    }

    public List<FeatureAtt> flds = new ArrayList<>();
}
