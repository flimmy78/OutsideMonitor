package com.lon.outsidemonitor.core;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import android.util.Log;

import com.lon.outsidemonitor.util.HalfFloat;

public class WorkMode {

	int gear = -1;;
	int adMin = 0;
	int adMax = 10;

	String unit="V";
	Date calDate;

	int moduleType=0;
	
	final int[] FrequencyArray = { 0, 25, 50, 100, 200, 550, 650, 750, 850,
			1700, 2000, 2300, 2600 };

	HashMap<Integer, float[]> calMap = new HashMap<Integer, float[]>();

	public WorkMode(int gear, byte[] data, int offset,int type) {
		this.moduleType=type;
		this.gear = gear;

		int calOffset = 0;

		float[] CoeffK = new float[1];
		float[] CoeffB = new float[1];
		for (int i = 0; i < FrequencyArray.length; i++) {
			if (i == 0) {
				int k = (data[offset] & 0xff)
						+ ((data[offset + 1] & 0xff) << 8);
				HalfFloat.Halfp2Singles(CoeffK, new int[] { k }, 1);
				int b = (data[offset + 2] & 0xff)
						+ ((data[offset + 3] & 0xff) << 8);
				HalfFloat.Halfp2Singles(CoeffB, new int[] { b }, 1);
				calMap.put(FrequencyArray[i], new float[] { CoeffK[0],
						CoeffB[0] });
				calOffset += 4;
			} else {
				int k = (data[offset + calOffset] & 0xff)
						+ ((data[offset + 1 + calOffset] & 0xff) << 8);
				HalfFloat.Halfp2Singles(CoeffK, new int[] { k }, 1);
				calMap.put(FrequencyArray[i], new float[] { CoeffK[0], 0f });
				calOffset += 2;
			}
		}
		switch(type)
		{
		case 5:
			this.adMin=0;
			this.adMax=5;
			break;
		case 6:
			switch(gear)
			{
			case 0:
				this.adMin=0;
				this.adMax=500;
				break;
			case 1:
				this.adMin=0;
				this.adMax=200;
				break;
			case 2:
				this.adMin=0;
				this.adMax=40;
				break;
			case 3:
				this.adMin=0;
				this.adMax=10;
				break;
			
			}
			break;
		}

	}

	public WorkMode(byte[] data, int offset) {

		this.gear = 0;
		float[] CoeffK = new float[1];
		float[] CoeffB = new float[1];

		for (int i = 0; i < 21; i++) {

			byte fg = data[offset + i * 5];
			if (fg == (byte) 0xff)
				continue;

			byte freqindex = (byte) ((fg & 0xff) >> 4);
			int k = (data[offset + i * 5 + 1] & 0xff)
					+ ((data[offset + i * 5 + 2] & 0xff) << 8);
			HalfFloat.Halfp2Singles(CoeffK, new int[] { k }, 1);
			int b = (data[offset + i * 5 + 3] & 0xff)
					+ ((data[offset + i * 5 + 4] & 0xff) << 8);
			HalfFloat.Halfp2Singles(CoeffB, new int[] { b }, 1);

			calMap.put(FrequencyArray[freqindex], new float[] { CoeffK[0], CoeffB[0] });

		}
		
		
		
		
	}

	public int getGear() {
		return gear;
	}
	
	public String getGearInfo()
	{
		return "µµÎ» "+gear+" ["+this.adMin+"--"+this.adMax+"]"+unit;
	}
	
	public float calRealVal(float adVal,int freq)
	{
		
		Set<Integer> keys= calMap.keySet();
		
		int absMax=Integer.MAX_VALUE;
		int minKey=0;
		
		for(Integer key : keys)
		{
			if(Math.abs(key-freq)<absMax)
			{
				minKey=key;
				absMax=Math.abs(key-freq);
			}
		}
		
		float[] coeff=calMap.get(minKey);
		
		
		return adVal*coeff[0]+coeff[1];
		
	}
	
	public float getUpper()
	{
		return this.adMax;
	}
	
	public float getLower()
	{
		return this.adMin;
	}

}
