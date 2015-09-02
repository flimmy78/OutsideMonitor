package com.lon.outsidemonitor.core;

import java.util.Calendar;

public class DevInfo {
	
	private final static String[] DeviceName = new String[] { "�����·���ģ��",
		"����������ģ��", "ֱ��������ģ��", "�����źŻ����ģ��", "ֱ���źŻ����ģ��", "�ۺ��źŰ�����ɼ�ģ��",
		"�ۺ��źŰ��ѹ�ɼ�ģ��" };

	int moduleType = -1;
	Calendar producelDate=Calendar.getInstance();
	
	String seqNum;//���к�
	String moduleMode; //�ͺ�
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
