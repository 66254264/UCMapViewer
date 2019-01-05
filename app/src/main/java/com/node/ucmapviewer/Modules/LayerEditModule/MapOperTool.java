package com.node.ucmapviewer.Modules.LayerEditModule;

/**
 * 地图操作工具,地图视图当前只能处于某一种操作状态
 */
public interface MapOperTool {
    /**
     * 开启
     */
    void start();

    /**
     * 停止
     */
    void stop();
}
