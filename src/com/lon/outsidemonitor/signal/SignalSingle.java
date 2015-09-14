package com.lon.outsidemonitor.signal;

import java.text.DecimalFormat;


public class SignalSingle implements ISignal{

	private float acAmpl=0;
	private float dcAmpl=0;
	private float freq=0;
	long time=0;
	
	private float[] rawData;
	private float[] spectrumData;
	
	
	boolean showDC=true;
	
	private SignalAmpl signalAmpl;
	private String unit="";
	
	public SignalSingle() {
		// TODO Auto-generated constructor stub
	}
	public SignalSingle(float freq,float acAmpl,float dcAmpl) {
		// TODO Auto-generated constructor stub
		this.freq=freq;
		this.acAmpl=acAmpl;
		this.dcAmpl=dcAmpl;
	}
	
	
	public SignalSingle(float freq,SignalAmpl ampl,String unit)
	{
		this.freq=freq;
		this.signalAmpl=ampl;
		this.unit=unit;
	}
	
	public void setACAmpl(float ampl)
	{
		this.acAmpl=ampl;
	}
	
	public void setDCAmpl(float ampl)
	{
		this.dcAmpl=ampl;
	}
	
	public float getFreq()
	{
		return freq;
	}
	public long getTime()
	{
		return this.time;
	}
	public float getFrequcy()
	{
		return freq;
	}
	public void putRawData(float[] data)
	{
		rawData=data;
	}
	
	public void putSpectrumData(float[] data)
	{
		spectrumData=data;
	}
	
	public void setParam(boolean enable,String unit)
	{
		this.showDC=enable;
		this.unit=unit;
	}
	
	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalSingle;
	}

	@Override
	public float getACAmpl() {
		// TODO Auto-generated method stub
		return acAmpl;
	}

	@Override
	public float getDCAmpl() {
		// TODO Auto-generated method stub
		return dcAmpl;
	}
	@Override
	public String getSignalInfo() {
		// TODO Auto-generated method stub
		
		
		StringBuilder sb=new StringBuilder();
		sb.append("  [µ¥ÆµÐÅºÅ]");
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");

		
		sb.append(" AC("+df.format(acAmpl)+unit+ ")");
		if(showDC)
		{
			sb.append(" DC("+df.format(dcAmpl)+unit+ ")");
		}
		df.applyPattern("0.00");
		sb.append(" ÆµÂÊ("+df.format(freq)+ ")");
		
		return sb.toString();
	}
	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		
		SignalSingle destSignal=(SignalSingle)dest;
		destSignal.acAmpl=acAmpl;
		destSignal.freq=freq;
		destSignal.dcAmpl=dcAmpl;
		destSignal.signalAmpl=signalAmpl;
		destSignal.rawData=rawData;
		
		destSignal.spectrumData=spectrumData;
		//destSignal.unit=unit;
		
	}
	@Override
	public float[] getRawData() {
		// TODO Auto-generated method stub
		return rawData;
	}
	
	public float[] getSpectrumData()
	{
		return spectrumData;
	}
	
	public SignalAmpl getAmpl()
	{
		return signalAmpl;
	}
	
	public String getUnit()
	{
		return unit;
	}
}
