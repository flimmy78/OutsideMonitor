package com.lon.outsidemonitor.signal;



public interface ISignal {

public SignalType getSignalType();
	
	/*
	 * 获取交流幅度
	 */
	public float getACAmpl();
	/*
	 * 获取直流幅度
	 */
	public float getDCAmpl();
	
	public float getFreq();
	
	public String getSignalInfo();
	
	public void copyTo(ISignal dest);
	
	public float[] getRawData();
	public float[] getSpectrumData();
	
	public SignalAmpl getAmpl();
	
	public String getUnit();
	
	public long getTime();
	
	public void setParam(boolean enableDC,String unit);
}
