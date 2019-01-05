package com.node.ucmapviewer.FrameWork.MapModule.Map;

import android.graphics.Bitmap;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.node.ucmapviewer.Modules.LayerEditModule.MapOperTool;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.util.Vector;

import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCVectorLayer;

/**
 * 这个工具啥都不做，就是为了清空工具得状态
 */
public class PanTool implements OnGestureListener,MapOperTool {

	private MapView mMapView;


	public PanTool(MapView mapView)
	{
		this.mMapView=mapView;

	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		stop();

	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {

		return true;
	}


@Override
	public void start()
	{


	}
	@Override
	public void stop()
	{
	}
}
