package com.lon.outsidemonitor.core;

import com.lon.outsidemonitor.R;
import com.lon.outsidemonitor.signal.ISignal;



public class ModuleInfo {

	int channelIndex=-1;
	int index=-1;
	SignalModule module;
	
	public ModuleInfo(SignalModule module,int cIndex,int aIndex)
	{
		this.module=module;
		this.channelIndex=cIndex;
		this.index=aIndex;
	}
	
	public boolean isGroup()
	{
		return channelIndex<0?true:false;
	}
	
	public String getName()
	{
		if(channelIndex>=0)
		{
			return "Í¨µÀ:"+channelIndex;
		}
		DevInfo info=module.getDevInfo();
		if(info!=null)
		{
			return info.getName();
		}
		return  "";
	}
	
	public int getImageId()
	{
		 return R.drawable.sine;
	}
	
	public ISignal getSignal()
	{
		if(channelIndex<0) return null;
		return module.getChannel(channelIndex).getSignal();
	}
}
