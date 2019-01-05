package com.node.ucmapviewer.FrameWork.Config;

import android.content.Context;

import com.node.ucmapviewer.FrameWork.Config.Entity.ConfigEntity;
import com.node.ucmapviewer.FrameWork.Config.Xml.XmlParser;

/**
 * 系统配置获取
 */
public class AppConfig {

    /**
     * 获取应用程序配置信息
     */
    public static ConfigEntity getConfig(Context context) {
        ConfigEntity config = null;
        try {
            config = XmlParser.getConfig(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }
}
