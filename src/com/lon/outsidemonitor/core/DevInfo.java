package com.lon.outsidemonitor.core;

import java.util.Calendar;

public class DevInfo {
	
	private final static String[] DeviceName = new String[] { "轨道电路监测模块",
		"交流道岔监测模块", "直流道岔监测模块", "交流信号机监测模块", "直流信号机监测模块", "综合信号板电流采集模块",
		"综合信号板电压采集模块" };

	int moduleType = -1;
	Calendar producelDate=Calendar.getInstance();
	
	String seqNum;//序列号
	String moduleMode; //型号
	public DevInfo(byte[] frame) {

		
		moduleType=frame[6]-1;
		
		producelDate.set(frame[23]+2000, frame[24], frame[25]);
		
		
		  int seqLen = frame[26];
		  seqNum=new String(frame,27,seqLen);

		  byte typeLen = frame[27 + seqLen];
		  moduleMode =new String(frame,28+seqLen,typeLen);
		  
          
		
	}
	
	
	


	public boolean isRemovable()
	{
		if(moduleType!=6) return true;
		return false;
	}
	
	public String getName()
	{
		return moduleMode+":"+seqNum;
	}
	
	public int getType()
	{
		return moduleType;
	}
	
}
