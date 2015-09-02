package com.lon.outsidemonitor.signal;

import java.util.ArrayList;
import java.util.List;


public class SignalAmpl {

List<SignalAmplPoint> listAmpl=new ArrayList<SignalAmplPoint>();
	

	
	public SignalAmpl()
	{
		
	}
	
	public SignalAmpl(float ampl,long millisTime)
	{
		listAmpl.add(new SignalAmplPoint(ampl, millisTime));
	}
	
	public void addAmpl(float ampl,long millisTime)
	{
		listAmpl.add(new SignalAmplPoint(ampl, millisTime));
	}
	
	public int getCount()
	{
		return listAmpl.size();
	}
	
	public SignalAmplPoint getAmplPoint(int index) {
		
		return listAmpl.get(index);
	}
	
}
