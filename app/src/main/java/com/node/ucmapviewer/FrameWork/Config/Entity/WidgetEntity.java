////////////////////////////////////////////////////////////////////////////////
//
//Copyright (c) 2011-2012 Esri
//
//All rights reserved under the copyright laws of the United States.
//You may freely redistribute and use this software, with or
//without modification, provided you include the original copyright
//and use restrictions.  See use restrictions in the file:
//<install location>/License.txt
//
////////////////////////////////////////////////////////////////////////////////

package com.node.ucmapviewer.FrameWork.Config.Entity;

/**
 * Widget组件信息
 */
public class WidgetEntity 
{
	private int mId = 0;
	private String mClassname = "";
	private String mLabel = "";
	private String mGroup = "";
	private String mIcon = "";
	private String mSelectIcon = "";
	private String mConfig = "";
	private boolean isShowing = false;

	public void setGroup(String group){
		this.mGroup = group;
	}
	public String getGroup(){
		return mGroup;
	}

	public boolean getIsShowing() {
		return isShowing;
	}
	public void setStatus(boolean isShowing) {
		this.isShowing =  isShowing;
	}
	public void setId(int id)
	{
		mId = id;
	}
	public int getId()
	{
		return mId;
	}
	public void setClassname(String name)
	{
		mClassname = name;
	}
	public String getClassname()
	{
		return mClassname;
	}
	public void setConfig(String config)
	{
		mConfig = config;
	}
	public String getConfig()
	{
		return mConfig;
	}

	public void setSelectIconName(String mSelectIcon) {
		this.mSelectIcon = mSelectIcon;
	}

	public String getSelectIcon() {
		return mSelectIcon;
	}

	public void setIconName(String icon)
	{
		mIcon = icon;
	}
	public String getIconName()
	{
		return mIcon;
	}

	public void setLabel(String label)
	{
		mLabel = label;
	}
	public String getLabel()
	{
		return mLabel;
	}
}
