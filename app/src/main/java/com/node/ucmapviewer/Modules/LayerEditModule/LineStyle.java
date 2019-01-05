package com.node.ucmapviewer.Modules.LayerEditModule;

/**
 * 线样式
 */
public class LineStyle{

    public LineStyle(){
        lineWidth = 3;
        lineColor = "#ff0000ff";
    }
    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    int lineWidth;

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    String lineColor;

}
