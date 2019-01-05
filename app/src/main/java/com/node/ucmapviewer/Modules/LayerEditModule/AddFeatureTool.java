package com.node.ucmapviewer.Modules.LayerEditModule;

import android.graphics.Bitmap;
import android.view.MotionEvent;

import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.gdal.ogr.ogr;
import org.jeo.data.Cursor;
import org.jeo.vector.Feature;

import java.util.Hashtable;
import java.util.Vector;

import cn.creable.ucmap.openGIS.Arithmetic;
import cn.creable.ucmap.openGIS.GeometryType;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCMapViewListener;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCScreenLayer;
import cn.creable.ucmap.openGIS.UCScreenLayerListener;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class AddFeatureTool implements MapOperTool {
    private MapView mapView;
    private UCFeatureLayer layer;
    private UCScreenLayer slayer;
    private UCMarkerLayer mlayer;
    private UCVectorLayer vlayer;
    private Bitmap pointImage;
    private Bitmap crossImage;
    private LineString elastic;

    private GeometryFactory gf=new GeometryFactory();
    private Vector<Coordinate> coords;
    private Feature feature;

    private UCFeatureLayer[] snapLayers;
    private Point snapResult;
    private double snapDistance;

    private LineString snapLine;
    private int snapIndex;
    private LineString snapLinePrev;
    private int snapIndexPrev;
    private boolean snapAutoMode;

    private UCMapViewListener listener=new UCMapViewListener() {

        @Override
        public void onMapViewEvent() {
            if (elastic!=null)
            {
                Coordinate[] cds=null;
                Coordinate[] or=elastic.getCoordinates();
                if (or.length==2)
                {
                    Point pt=mapView.toMapPoint(mapView.getWidth()/2, mapView.getHeight()/2);
                    cds=new Coordinate[2];
                    cds[0]=or[0];
                    cds[1]=new Coordinate(pt.getX(),pt.getY());
                }
                else if (or.length==3)
                {
                    Point pt=mapView.toMapPoint(mapView.getWidth()/2, mapView.getHeight()/2);
                    cds=new Coordinate[3];
                    cds[0]=or[0];
                    cds[1]=new Coordinate(pt.getX(),pt.getY());
                    cds[2]=or[2];
                }
                LineString line=gf.createLineString(cds);
                vlayer.updateGeometry(elastic, line);
                elastic=line;
            }
        }

    };

    public AddFeatureTool(MapView mapView, UCFeatureLayer layer, Bitmap pointImage, Bitmap crossImage)
    {
        this.mapView=mapView;
        this.layer=layer;
        this.pointImage=pointImage;
        this.crossImage=crossImage;
    }

    public void openSnap(UCFeatureLayer[] snapLayers,double snapDistance,boolean autoMode)
    {
        this.snapLayers=snapLayers;
        this.snapDistance=snapDistance;
        this.snapAutoMode=autoMode;
    }

    public double snap(UCFeatureLayer layer,Point point,double distance)
    {
        double z=0.01;
        Envelope env=new Envelope(point.getX()-z,point.getX()+z,point.getY()-z,point.getY()+z);
        env=layer.transformEnvelopeClone(env, layer.getCRS());
        Cursor<Feature> cursor=layer.searchFeature(null, 0, 0, env.getMinX(),env.getMaxX(),env.getMinY(),env.getMaxY());
        try {
            double dis=distance;
            String type;
            snapIndexPrev=snapIndex;
            snapLinePrev=snapLine;
            for (Feature f : cursor)
            {
                Geometry geo=layer.transformGeometryClone(f.geometry(), layer.getOutputCRS());
                type=geo.getGeometryType();
                if (type==GeometryType.Point)
                {
                    double cur=Arithmetic.Distance(point, (com.vividsolutions.jts.geom.Point)geo);
                    if (cur<dis)
                    {
                        dis=cur;
                        snapResult=(Point)geo;
                    }
                }
                else if (type==GeometryType.LineString)
                {
                    LineString line=(LineString)geo;
                    int number=line.getNumPoints();
                    for (int j=0;j<number;j++) {
                        double cur=Arithmetic.Distance(point, line.getPointN(j));
                        if (cur<dis)
                        {
                            dis=cur;
                            snapResult=line.getPointN(j);
                            snapLine=line;
                            snapIndex=j;
                        }
                    }
                }
                else if (type==GeometryType.Polygon)
                {
                    Polygon pg=(Polygon)geo;
                    LineString line=pg.getExteriorRing();
                    int number=line.getNumPoints();
                    for (int j=0;j<number;j++) {
                        double cur=Arithmetic.Distance(point, line.getPointN(j));
                        if (cur<dis)
                        {
                            dis=cur;
                            snapResult=line.getPointN(j);
                            snapLine=line;
                            snapIndex=j;
                        }
                    }
                    int numberRing=pg.getNumInteriorRing();
                    for (int n=0;n<numberRing;n++) {
                        line=pg.getInteriorRingN(n);
                        number=line.getNumPoints();
                        for (int j=0;j<number;j++) {
                            double cur=Arithmetic.Distance(point, line.getPointN(j));
                            if (cur<dis)
                            {
                                dis=cur;
                                snapResult=line.getPointN(j);
                                snapLine=line;
                                snapIndex=j;
                            }
                        }
                    }
                }
            }
            cursor.close();
            return dis;
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }
    }

    private void onClick(Point pt)
    {
        if (layer.getGeometryType()==ogr.wkbPoint || layer.getGeometryType()==ogr.wkbMultiPoint)
        {
            Hashtable<String,Object> values=new Hashtable<String,Object>();
            layer.transformGeometry(pt, layer.getCRS());
            values.put("geometry", pt);
            Feature ft=layer.addFeature(values);
            UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, ft);
        }
        else if (layer.getGeometryType()==ogr.wkbLineString || layer.getGeometryType()==ogr.wkbMultiLineString)
        {
            mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");

            if (this.elastic!=null)
                vlayer.remove(this.elastic);
            Coordinate[] sss=new Coordinate[2];
            sss[0]=new Coordinate(pt.getX(),pt.getY());
            sss[1]=new Coordinate(pt.getX(),pt.getY());
            this.elastic=gf.createLineString(sss);
            vlayer.addLine(this.elastic, 2, 0xFFFF0000);

            mapView.bind(listener);

            if (coords==null)
            {
                coords=new Vector<Coordinate>();
                coords.add(new Coordinate(pt.getX(),pt.getY()));
            }
            else
            {
                coords.add(new Coordinate(pt.getX(),pt.getY()));
                if (feature==null) {
                    Coordinate[] cds=new Coordinate[coords.size()];
                    coords.copyInto(cds);
                    Hashtable<String,Object> values=new Hashtable<String,Object>();
                    LineString line=gf.createLineString(cds);
                    layer.transformGeometry(line, layer.getCRS());
                    values.put("geometry",line);
                    feature=layer.addFeature(values);
                }
                else
                {
                    Coordinate[] cds=new Coordinate[coords.size()];
                    coords.copyInto(cds);
                    Hashtable<String,Object> values=new Hashtable<String,Object>();
                    LineString line=gf.createLineString(cds);
                    layer.transformGeometry(line, layer.getCRS());
                    values.put("geometry",line);
                    layer.updateFeature(feature, values);
                }
            }
        }
        else if (layer.getGeometryType()==ogr.wkbPolygon || layer.getGeometryType()==ogr.wkbMultiPolygon)
        {
            mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
            if (coords==null)
                coords=new Vector<Coordinate>();
            coords.add(new Coordinate(pt.getX(),pt.getY()));

            if (this.elastic!=null)
                vlayer.remove(this.elastic);
            Coordinate[] sss=new Coordinate[3];
            sss[0]=new Coordinate(pt.getX(),pt.getY());
            sss[1]=new Coordinate(pt.getX(),pt.getY());
            sss[2]=coords.elementAt(0);
            this.elastic=gf.createLineString(sss);
           vlayer.addLine(this.elastic, 2, 0xFFFF0000);
            //this.elastic=gf.createPolygon(sss);
            //vlayer.addPolygon(this.elastic, 2, 0xFFFF0000,0xFF0000FF,0.85f);

            mapView.bind(listener);

            if (coords.size()>2)
            {
                if (feature==null) {
                    Coordinate[] cds=new Coordinate[coords.size()+1];
                    //coords.copyInto(cds);
                    for (int k=0;k<coords.size();++k)
                    {
                        cds[k]=new Coordinate(coords.elementAt(k).x,coords.elementAt(k).y);
                    }
                    cds[coords.size()]=new Coordinate(cds[0].x,cds[0].y);//cds[0];
                    Hashtable<String,Object> values=new Hashtable<String,Object>();
                    Polygon pg=gf.createPolygon(gf.createLinearRing(cds));
                    layer.transformGeometry(pg, layer.getCRS());
                    values.put("geometry",pg);
                    feature=layer.addFeature(values);
                }
                else
                {
                    Coordinate[] cds=new Coordinate[coords.size()+1];
                    //coords.copyInto(cds);
                    for (int k=0;k<coords.size();++k)
                    {
                        cds[k]=new Coordinate(coords.elementAt(k).x,coords.elementAt(k).y);
                    }
                    cds[coords.size()]=new Coordinate(cds[0].x,cds[0].y);//cds[0];
                    Hashtable<String,Object> values=new Hashtable<String,Object>();
                    Polygon pg=gf.createPolygon(gf.createLinearRing(cds));
                    layer.transformGeometry(pg, layer.getCRS());
                    values.put("geometry",pg);
                    layer.updateFeature(feature, values);
                }
            }
        }
    }

    private double distance(LineString line,int start,int end)
    {
        if (start<end)
        {
            double dis=0;
            for (int i=start;i<end;++i)
                dis+=Arithmetic.Distance(line.getPointN(i), line.getPointN(i+1));
            return dis;
        }
        else
        {
            if (line.isClosed()==false)
                return Double.MAX_VALUE;
            double dis=0;
            for (int i=start;i<(line.getNumPoints()-1);++i)
                dis+=Arithmetic.Distance(line.getPointN(i), line.getPointN(i+1));
            for (int i=0;i<end;++i)
                dis+=Arithmetic.Distance(line.getPointN(i), line.getPointN(i+1));
            return dis;
        }
    }

    @Override
    public void start() {
        vlayer=mapView.addVectorLayer();
        mlayer=mapView.addMarkerLayer(null);

        //String dir=Environment.getExternalStorageDirectory().getPath();
        slayer=mapView.addScreenLayer(crossImage,0,0, new UCScreenLayerListener() {

            @Override
            public boolean onItemSingleTapUp(UCScreenLayer lyr) {
                Point pt=mapView.toMapPoint(mapView.getWidth()/2, mapView.getHeight()/2);
                if (snapLayers!=null)
                {
                    snapResult=null;
                    double dis=Double.MAX_VALUE;
                    for (UCFeatureLayer flayer : snapLayers)
                    {
                        dis=snap(flayer,pt,dis);
                    }
                    if (snapResult!=null)
                    {
                        Point point=mapView.fromMapPoint(snapResult.getX(), snapResult.getY());
                        Point center=gf.createPoint(new Coordinate(mapView.getWidth()/2, mapView.getHeight()/2));
                        double distance=Arithmetic.Distance(center, point);
                        if (distance<snapDistance)
                        {//如果小于设定的最小距离，则snap到result上
                            if (snapAutoMode && snapLinePrev!=null && snapIndexPrev!=snapIndex && snapLinePrev.equals(snapLine))
                            {
                                if (snapIndexPrev<snapIndex)
                                {
                                    if (((snapIndex-snapIndexPrev)*2)<snapLine.getNumPoints())
                                    {
                                        for (int k=snapIndexPrev+1;k<=snapIndex;++k)
                                            onClick(snapLine.getPointN(k));
                                    }
                                    else if (snapLine.isClosed())
                                    {
                                        for (int k=snapIndexPrev;k>=0;--k)
                                            onClick(snapLine.getPointN(k));
                                        for (int k=snapLine.getNumPoints()-2;k>=snapIndex;--k)
                                            onClick(snapLine.getPointN(k));
                                    }
                                    else
                                    {
                                        onClick(snapResult);
                                    }
                                }
                                else
                                {
                                    if (((snapIndexPrev-snapIndex)*2)<snapLine.getNumPoints())
                                    {
                                        for (int k=snapIndexPrev-1;k>=snapIndex;--k)
                                            onClick(snapLine.getPointN(k));
                                    }
                                    else if (snapLine.isClosed())
                                    {
                                        for (int k=snapIndexPrev;k<snapLine.getNumPoints();++k)
                                            onClick(snapLine.getPointN(k));
                                        for (int k=1;k<=snapIndex;++k)
                                            onClick(snapLine.getPointN(k));
                                    }
                                    else
                                    {
                                        onClick(snapResult);
                                    }
                                }
                            }
                            else
                            {
                                onClick(snapResult);
                            }
                        }
                        else
                        {
                            snapResult=null;
                            snapLinePrev=null;
                            snapLine=null;
                            onClick(pt);
                        }
                    }
                    else
                    {
                        onClick(pt);
                    }
                }
                else
                {
                    onClick(pt);
                }
                UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, feature);
                mapView.refresh();
                return true;
            }

            @Override
            public boolean onItemLongPress(UCScreenLayer lyr) {
                mlayer.removeAllItems();
                coords.clear();
                coords=null;
                feature=null;
                mapView.unbind(listener);
                if (elastic!=null) vlayer.remove(elastic);
                elastic=null;
                mapView.refresh();
                return true;
            }

        });

        mapView.refresh();
    }

    @Override
    public void stop() {
        mapView.unbind(listener);
        if (slayer!=null) mapView.deleteLayer(slayer);
        if (mlayer!=null) mapView.deleteLayer(mlayer);
        if (vlayer!=null) mapView.deleteLayer(vlayer);
        mapView.setListener(null, null);

        //mapView=null;
        mlayer=null;
        snapLayers=null;
    }
}
