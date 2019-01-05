package com.node.ucmapviewer.FrameWork.MapModule.Base;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.node.ucmapviewer.FrameWork.Config.Entity.ConfigEntity;
import com.node.ucmapviewer.FrameWork.Config.Entity.WidgetEntity;
import com.node.ucmapviewer.FrameWork.EventBus.BaseWidgetMsgEvent;
import com.node.ucmapviewer.FrameWork.MapModule.Map.MapView;
import com.node.ucmapviewer.Utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 应用程序组件基类

 */

public abstract class BaseModule {

    public int id = 0;
    public WidgetEntity entity;
    public Context context;
    public String name;
    public MapView mapView;
    public ConfigEntity viewerConfig;
    public String widgetConfig;

    public TextView txtWidgetName; //名称
    public ImageView imgWidgetIcon;//图标



    public String projectPath;//工程文件夹路径


    private ProgressDialog mProgressDlg;
    //private MapView.OnTouchListener mMapOnTouchListener;

    private View widgetContextView;//组件内容视图
    private RelativeLayout widgetExtentView;//组件扩展区域视图

    private boolean isActiveView=false;//当前是否显示

    private BaseModule baseWidget;

    public BaseModule(){
        baseWidget = this;
        EventBus.getDefault().register(this);//注册事件
    }

    /**
     * 设置扩展区域组件关键
     * @param widgetExtentView
     */
    public void setWidgetExtentView(RelativeLayout widgetExtentView) {
        this.widgetExtentView = widgetExtentView;
    }

    /**
     * 扩展区域显示
     */
    public void removeWidgetExtentView(){
        if (widgetExtentView!=null){
            widgetExtentView.removeAllViews();
        }
    }

    /**
     * 显示扩展区域信息
     * @param view
     */
    public void startWidgetExtentView(View view){
        if (widgetExtentView!=null){
            widgetExtentView.removeAllViews();
            widgetExtentView.addView(view);
        }
    }

    /**
     * 显示加载进度条
     * @param title 标题
     * @param message 消息内容
     */
    public void showLoading(String title, String message) {
        if(mProgressDlg == null)
            mProgressDlg = new ProgressDialog(context);

        mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDlg.setTitle(title);
        mProgressDlg.setMessage(message);
        if(!mProgressDlg.isShowing()) mProgressDlg.show();
    }

    /**
     * 关闭加载进度条
     */
    public void hideLoading()
    {
        if(mProgressDlg != null) mProgressDlg.dismiss();
    }

    /**
     * 显示消息
     * @param messsage 消息
     */
    public void showMessageBox(String messsage)
    {
        ToastUtils.showShort(context,messsage);
    }

    /**
     * widget组件的初始化操作，包括设置view内容，逻辑等
     * 该方法在应用程序加载完成后执行
     */
    public abstract void create();


    /**
     * 组件面板打开时，执行的操作
     * 当点击widget按钮是, WidgetManager将会调用这个方法，面板打开后的代码逻辑.
     * 面板关闭将会调用 "inactive" 方法
     */
    @SuppressLint("ResourceAsColor")
    public void active(){
        //当前面板活动，其他所有面板关闭
        EventBus.getDefault().post(new BaseWidgetMsgEvent(id+"-open"));
    }

    /**
     * 组件面板关闭时，执行的操作
     * 面板关闭将会调用 "inactive" 方法
     */
    public void inactive(){


    }

    /**
     * 获取当前widget是否处于显示状态
     * @return
     */
    public boolean isActiveView(){
        return isActiveView;
    }



    /**
     * 获取插件
     */
    public View getWidgetContextView()
    {
        return widgetContextView;
    }

    /**
     * 显示组件
     * @param v
     */
    public void showWidget(View v)
    {
        widgetContextView = v;
    }







    /**
     * 关闭组件
     */
    public void hideWidget() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN,priority = 100)//优先级100
    public void onMoonEvent(BaseWidgetMsgEvent baseWidgetMsgEvent){
        if(baseWidgetMsgEvent.getMessage().equals(id+"-open")){
           //判断当前页面是否活动，如果活动不执行任何操作
        }else{
           //关闭所有非活动状态
           Method method = null; // 父类对象调用子类方法(反射原理)
           try {
               method = baseWidget.getClass().getMethod("inactive");
               Object o = method.invoke(baseWidget);
           } catch (NoSuchMethodException e) {
               e.printStackTrace();
           } catch (IllegalAccessException e) {
               e.printStackTrace();
           } catch (InvocationTargetException e) {
               e.printStackTrace();
           }
       }
    }

    /**
     * 设置widget组件按钮view
     * @param textView
     * @param imageView
     */
    public void setWidgetBtnView(TextView textView, ImageView imageView) {
        this.txtWidgetName  = textView;
        this.imgWidgetIcon =  imageView;
    }

}
