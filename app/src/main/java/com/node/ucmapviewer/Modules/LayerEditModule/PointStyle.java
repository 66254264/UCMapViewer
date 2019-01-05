package com.node.ucmapviewer.Modules.LayerEditModule;

/**
 * 点样式
 */
public class PointStyle{
    public PointStyle(){
        pointSize = 30;
        pointColor = "#ff0000ff";
    }
    int pointSize;

    public String getPointColor() {
        return pointColor;
    }

    public void setPointColor(String pointColor) {
        this.pointColor = pointColor;
    }

    String pointColor;

    public int getPointSize() {
        return pointSize;
    }

    public void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }
}
