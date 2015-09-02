package com.lon.outsidemonitor.signal;

import java.text.DecimalFormat;



public class SignalUnknown implements ISignal {

	private float acAmpl=0;
	private float dcAmpl=0;
	long time=0;
	private short[] rawData;
	private float[] spectrumData;
	boolean showDC=true;
	private SignalAmpl signalAmpl;
	private float freq=0;
	private String unit="";
	
	public SignalUnknown(){}
	
	public SignalUnknown(float ampl)
	{
		this.acAmpl=ampl;
	}
	
	public SignalUnknown(float freq, SignalAmpl signalAmpl,String unit)
	{
		this.freq=freq;
		this.signalAmpl=signalAmpl;
		this.unit=unit;
	}
	
	public void setParam(boolean enable,String unit)
	{
		this.showDC=enable;
		this.unit=unit;
	}
	
	public void setDCAmpl(float ampl)
	{
		this.dcAmpl=ampl;
	}
	
	
	public void setACAmpl(float ampl)
	{
		this.acAmpl=ampl;
	}
	public float getFreq()
	{
		return freq;
	}
	public long getTime()
	{
		return this.time;
	}
	public void putRawData(short[] data)
	{
		this.rawData=data;
	}
	
	public void putSpectrumData(float[] data)
	{
		this.spectrumData=data;
	}
	
	
	
	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalUnknown;
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
		sb.append("  [Î´ÖªÐÅºÅ]");
		DecimalFormat df=new DecimalFormat();
		df.applyPattern("0.000");
		
		sb.append("  AC("+df.format(acAmpl)+unit+ ")");
		if(showDC)
		{
			sb.append("  DC("+df.format(dcAmpl)+unit+ ")");
		}
		
		return sb.toString();
	}

	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub
		SignalUnknown unkown=(SignalUnknown)dest;
		unkown.acAmpl=acAmpl;
		unkown.dcAmpl=dcAmpl;
		unkown.rawData=rawData;
		unkown.spectrumData=spectrumData;
		unkown.signalAmpl=signalAmpl;
		unkown.freq=this.freq;
	}

	@Override
	public short[] getRawData() {
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
