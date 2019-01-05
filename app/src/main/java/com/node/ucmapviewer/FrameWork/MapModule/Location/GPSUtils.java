package com.node.ucmapviewer.FrameWork.MapModule.Location;

import android.location.Criteria;
import android.location.LocationManager;

public class GPSUtils {

    /**
     * 获取最好的位置提供服务
     * @param manager
     * @return
     */
    public static String getBeastProvider(LocationManager manager) {
        //设置查询条件
        Criteria criteria = new Criteria();
        //定位精准度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //对海拔是否敏感
        criteria.setAltitudeRequired(false);
        //对手机耗电性能要求（获取频率）
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        //对速度变化是否敏感
        criteria.setSpeedRequired(true);
        //是否运行产生开销（费用）
        criteria.setCostAllowed(true);
        //如果置为ture只会返回当前打开的gps设备
        //如果置为false如果设备关闭也会返回

        return manager.getBestProvider(criteria, true);
    }
}
