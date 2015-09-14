package com.lon.outsidemonitor.signal;

import java.text.DecimalFormat;

public class SignalFSK implements ISignal {

	float acAmpl = 0;
	float dcAmpl = 0;
	float freqCarrier = 0; // ��Ƶ
	float freqLower = 0; // ��Ƶ
	long time=0;
	float[] rawData = null;
	float[] spectrumData = null;
	SignalAmpl signalAmpl = null;
	private String unit = "";
	boolean enableDC = false;
	
	public SignalFSK() {
		// TODO Auto-generated constructor stub
	}

	public SignalFSK(float acAmpl, float carrier, float lower) {
		// TODO Auto-generated constructor stub
		this.acAmpl = acAmpl;
		this.freqCarrier = carrier;
		this.freqLower = lower;
	}

	public SignalFSK(SignalAmpl ampl, float carrier, float lower, String unit) {
		// TODO Auto-generated constructor stub
		this.signalAmpl = ampl;
		this.freqCarrier = carrier;
		this.freqLower = lower;
		// this.unit=unit;
	}

	public float getFreq()
	{
		return freqCarrier;
	}
	
	public long getTime()
	{
		return this.time;
	}
	public void setACAmpl(float ampl) {
		this.acAmpl = ampl;
	}

	public void setDCAmpl(float ampl) {
		this.dcAmpl = ampl;
	}

	public void putRawData(float[] data) {
		this.rawData = data;
	}

	public void putSpectrumData(float[] data) {
		this.spectrumData = data;
	}

	@Override
	public SignalType getSignalType() {
		// TODO Auto-generated method stub
		return SignalType.SignalFSK;
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
		StringBuilder sb = new StringBuilder();
		if (freqCarrier > 1700 - 50 && freqCarrier < 2600 + 50) {
			sb.append("  [ZPW2000]");
		} else if (freqCarrier > 550 - 50 && freqCarrier < 850 + 50) {
			sb.append("  [��Ƶ�ź�]");
		}
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("0.000");

		sb.append("  AC(" + df.format(acAmpl) + unit + ")");
		if (enableDC) {
			sb.append("  DC(" + df.format(dcAmpl) + unit + ")");
		}
		df.applyPattern("0.00");
		sb.append("  ��Ƶ(" + df.format(freqCarrier) + ")");
		sb.append("  ��Ƶ(" + df.format(freqLower) + ")");
		return sb.toString();
	}

	@Override
	public void copyTo(ISignal dest) {
		// TODO Auto-generated method stub

		SignalFSK fsk = (SignalFSK) dest;
		fsk.acAmpl = acAmpl;
		fsk.dcAmpl = dcAmpl;
		fsk.freqCarrier = freqCarrier;
		fsk.freqLower = freqLower;
		fsk.rawData = rawData;
		fsk.spectrumData = spectrumData;
		fsk.signalAmpl = signalAmpl;
		// fsk.unit=unit;
	}

	@Override
	public float[] getRawData() {
		// TODO Auto-generated method stub
		return rawData;
	}

	public float[] getSpectrumData() {
		return spectrumData;
	}

	public SignalAmpl getAmpl() {
		return signalAmpl;
	}

	public String getUnit() {
		return unit;
	}

	@Override
	public void setParam(boolean enableDC, String unit) {
		// TODO Auto-generated method stub
		this.unit = unit;
		this.enableDC = enableDC;
	}
}