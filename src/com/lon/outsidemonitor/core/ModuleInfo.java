package com.lon.outsidemonitor.core;

import com.lon.outsidemonitor.R;
import com.lon.outsidemonitor.signal.ISignal;

public class ModuleInfo {

	int channelIndex = -1;
	int index = -1;
	SignalModule module;

	public ModuleInfo(SignalModule module, int cIndex, int aIndex) {
		this.module = module;
		this.channelIndex = cIndex;
		this.index = aIndex;
	}

	public boolean isGroup() {
		return channelIndex < 0 ? true : false;
	}

	public String getName() {
		if (channelIndex >= 0) {
			return "通道:" + channelIndex;
		}
		DevInfo info = module.getDevInfo();
		if (info != null) {
			return info.getName();
		}
		return "";
	}

	public int getImageId() {
		return R.drawable.sine;
	}

	public ISignal getSignal() {
		if (channelIndex < 0)
			return null;
		return module.getChannel(channelIndex).getSignal();
	}

	public String getDescription() {
		StringBuilder desc = new StringBuilder(this.getName());

		if (this.isGroup() == false) {
			SignalChannel channel = module.getChannel(channelIndex);
			if (channel != null) {
				WorkMode wkMode=channel.getWorkMode();
				if(wkMode==null)
				{
					desc.append(" [档位:自动]");
				}
				else
				{
					desc.append(" [档位:"+channel.getWorkMode().getGear()+"]");
				}
				
				desc.append(" [滤波方式:"+channel.getFilter().toString()+"]");
				ISignal signal = channel.getSignal();
				if (signal != null) {
					desc.append(signal.getSignalInfo());
				}

			}

		}

		return desc.toString();
	}
}
