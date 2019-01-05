package com.node.ucmapviewer.Modules.LayerEditModule;

/**
 * 多边形样式
 */
public class PolygonStyle{


    public PolygonStyle(){
        lineWidth = 2;
        lineColor = "#ff000000";
        fillColor = "#cc00ff00";
    }
    int lineWidth;
    String lineColor;
    String fillColor;

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }
}
