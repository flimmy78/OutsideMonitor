package com.lon.outsidemonitor.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.lon.outsidemonitor.core.DataBlock;
import com.lon.outsidemonitor.core.IDataBlock;
import com.lon.outsidemonitor.dsp.DSPUtil;
import com.lon.outsidemonitor.dsp.FFTPlan;
import com.lon.outsidemonitor.dsp.Filter;
import com.lon.outsidemonitor.dsp.SignalUtil;
import com.lon.outsidemonitor.signal.ISignal;
import com.lon.outsidemonitor.signal.ISignalChangedListener;
import com.lon.outsidemonitor.signal.SignalAmpl;
import com.lon.outsidemonitor.signal.SignalFSK;
import com.lon.outsidemonitor.signal.SignalSingle;
import com.lon.outsidemonitor.signal.SignalType;
import com.lon.outsidemonitor.signal.SignalUnknown;
import com.lon.outsidemonitor.util.CRC16;

public class SignalChannel {

	SignalModule module = null;
	byte channelIndex = 0;

	byte state;
	int sampleRate;
	Calendar calDate = Calendar.getInstance();
	String persion = "";
	ArrayList<WorkMode> listGrear = new ArrayList<WorkMode>();
	int gearNow = -1;
	int gearSet = 0;
	Thread threadDSP;

	Filter signalFilter=new Filter();
	
	int displayMode = -1; // -1:计算所有信息，显示模式 0：原始波形 1：信号幅度 2:信号频谱

	private ArrayList<DataBlock> listDataBlocks = new ArrayList<DataBlock>(); // 数据块

	private List<ISignalChangedListener> signalChangedListeners = new ArrayList<ISignalChangedListener>();

	private List<IWorkModeChangedListener> workModeChangedListeners = new ArrayList<IWorkModeChangedListener>();

	ArrayList<ISignal> listSignals = new ArrayList<ISignal>();
	ISignal currentSignal;

	final static boolean[][] DCEnable = new boolean[][] {
			new boolean[] { false, false, false },
			new boolean[] { false, false, true },
			new boolean[] { true, true, true },
			new boolean[] { false, false, false },
			new boolean[] { true, true, true },
			new boolean[] { false, false, true },
			new boolean[] { true, true, true }, };

	final static String[][] UnitList = new String[][] {
			new String[] { "A", "", "V" }, new String[] { "A", "V", "V" },
			new String[] { "A", "V", "V" }, new String[] { "A", "", "V" },
			new String[] { "A", "", "V" }, new String[] { "A", "A", "A" },
			new String[] { "V", "V", "V" }, };

	public SignalChannel(SignalModule module, int index) {
		this.module = module;
		this.channelIndex = (byte) index;

		listSignals.add(new SignalSingle()); // 单频信号
		listSignals.add(new SignalFSK());// 移频和UM71信号
		listSignals.add(new SignalUnknown());// 未知信号
		// listSignals.add(new SignalNULL());// 小信号
	}

	public Filter getFilter()
	{
		return signalFilter;
	}
	public void setFilter(int index)
	{
		this.signalFilter.setFilterIndex(index);
	}
	public void setDisplayMode(int mode) {

		int num = ModuleManager.getInstance().getModuleNum();
		for (int i = 0; i < num; i++) {
			SignalModule module = ModuleManager.getInstance().getModule(i);

			for (int j = 0; j < 3; j++) {
				SignalChannel channel = module.getChannel(j);
				if (channel == this) {
					channel.displayMode = mode;
				} else if (mode >= 0) {
					channel.displayMode = -2;
				} else {
					channel.displayMode = -1;
				}
			}

		}
	}

	public void updateChannel() {
		DevInfo info = module.getDevInfo();
		if (info == null)
			return;

		int type = info.getType();
		if (type >= 0 && type <= 6) {
			for (ISignal signal : listSignals) {
				signal.setParam(DCEnable[type][channelIndex],
						UnitList[type][channelIndex]);
			}
		}

	}

	int frameNum = -1;

	public void putFrame(byte[] frame) {
		frameNum++;
		if ((listGrear.size() <= 0) && (frameNum == 0)) {
			requestParam(); // 查询参数
		}
		if (gearNow < 0 && (frameNum == 1)) {
			requestGear(); // 查询档位
		}
		if ((gearNow != gearSet) && (frameNum == 2)) {
			setGear(gearSet);
		}
		if ((gearNow != gearSet) && (frameNum == 3)) {
			requestGear(); // 查询档位
		}
		if (frameNum >= 4) {
			frameNum = -1;
		}
		byte cmd = frame[5];
		switch (cmd) {
		case (byte) 0x13:
			processParam(frame);
			break;
		case (byte) 0x14:
			processGear(frame);
			break;
		case (byte) 0x15:
			processAD(frame);
			break;
		}
	}

	public IDataBlock addDataBlock(int window, int slice) {

		DataBlock block = null;
		synchronized (listDataBlocks) {

			block = new DataBlock(window, slice);

			listDataBlocks.add(block);
		}

		return block;
	}

	public void removeDataBlock(IDataBlock block) {

		if (DataBlock.class.isInstance(block)) {
			DataBlock iBlock = DataBlock.class.cast(block);
			synchronized (listDataBlocks) {
				listDataBlocks.remove(iBlock);
			}
		}
	}

	private void requestParam() {

		byte[] frame = new byte[] { (byte) 0x93, channelIndex };
		module.sendFrame(frame);

	}

	private void requestGear() {
		byte[] frame = new byte[] { (byte) 0x94, channelIndex };
		module.sendFrame(frame);
	}

	public void setGear(int gear) {
		if (gear != gearNow) {
			byte[] frame = new byte[] { (byte) 0x84, channelIndex, (byte) gear };
			module.sendFrame(frame);
			gearSet = gear;
		}
	}

	private void processParam(byte[] data) {

		DevInfo info = module.getDevInfo();
		if (info == null)
			return;

		int offset = 7;
		int crcPos = info.isRemovable() ? 124 - 2 : 128 - 2;
		int crc = CRC16.computeCRC16(data, offset, crcPos);
		int crcCal = (data[offset + crcPos] & 0xff)
				+ ((data[offset + crcPos + 1] & 0xff) << 8);

		if (crcCal != crc)
			return;

		this.state = (byte) ((data[1 + offset] & 0xff) >> 7);
		this.sampleRate = data[offset] + ((data[1 + offset] & 0x1f) << 8);

		// 时间
		int year = data[3 + offset] + 2000;
		int month = data[4 + offset];
		int day = data[5 + offset];

		this.calDate.set(year, month, day);
		// 名字;
		int num = data[6 + offset];
		int maxNameLen = info.isRemovable() ? 8 : 7;
		if (num > 0) {
			num = num > maxNameLen ? maxNameLen : num;

			this.persion = new String(data, 7 + offset, num);
		} else {

			this.persion = "";
		}

		listGrear.clear();
		if (info.isRemovable()) {

			WorkMode wkMode = new WorkMode(data, offset + 15);
			listGrear.add(wkMode);

		} else {

			for (int i = 0; i < 4; i++) {
				WorkMode wkMode = new WorkMode(i, data, offset + 14 + i * 28,
						info.moduleType);
				listGrear.add(wkMode);
			}
		}

	}

	public float calRealVal(float adVal, int freq) {
		
		if (gearNow < 0 || gearNow >= 4)
			return adVal;

		if (listGrear.size() <= gearNow)
			return adVal;
		
		float r = listGrear.get(gearNow).calRealVal(adVal, freq);
		return r;

	}

	private void processAD(byte[] frame) {

		int adLen = ((frame[3] & 0xff) | ((frame[4] & 0xff) << 8)) - 6;// 数据长度

		float[] adData = new float[adLen / 2];

		for (int j = 0; j < adLen / 2; j++) {
			short adVal = (short) ((frame[j * 2 + 11] & 0xff) | ((frame[j * 2 + 12] & 0x0f) << 8));
			float adFilter=signalFilter.doFilter(adVal);
			adData[j] = adFilter;
		}
		synchronized (listDataBlocks) {

			for (DataBlock block : listDataBlocks) {
				block.putSampleData(adData, 0, adData.length);
			}
		}

	}

	private void processGear(byte[] frame) {

		this.gearNow = frame[7];

		for (WorkMode mode : listGrear) {
			if (mode.getGear() == frame[7]) {
				this.WorkModeChanged(mode);
				break;
			}
		}

	}

	private void WorkModeChanged(WorkMode mode) {
		synchronized (workModeChangedListeners) {
			for (IWorkModeChangedListener listener : workModeChangedListeners) {
				listener.onWorkModeChanged(mode);
			}
		}
	}

	public ISignal getSignal() {
		return currentSignal;
	}

	
	public WorkMode getWorkMode() {
		WorkMode modeGet = null;
		if (gearNow >= 0 && gearNow <= 3) {
			for (WorkMode mode : listGrear) {
				if (mode.getGear() == gearNow) {
					modeGet = mode;
					break;
				}
			}
		}
		return modeGet;
	}

	public List<WorkMode> getModeList() {
		return listGrear;
	}

	private void setSignal(ISignal signal) {
		if (signal == null) {
			currentSignal = null;
			synchronized (signalChangedListeners) {
				for (ISignalChangedListener listener : signalChangedListeners) {
					listener.onSignalChanged(null);
				}
			}
			return;
		}
		SignalType signalType = signal.getSignalType();
		for (ISignal s : listSignals) {
			if (s.getSignalType() == signalType) {
				signal.copyTo(s);

				currentSignal = s;
				synchronized (signalChangedListeners) {
					for (ISignalChangedListener listener : signalChangedListeners) {
						listener.onSignalChanged(currentSignal);
					}
				}
				break;
			}
		}
	}

	public void addSignalChangedListener(ISignalChangedListener listener) {
		synchronized (signalChangedListeners) {
			signalChangedListeners.add(listener);
		}

	}

	public void removeSignalChangedListener(ISignalChangedListener listener) {
		synchronized (signalChangedListeners) {
			signalChangedListeners.remove(listener);
		}
	}

	public void addWorkModeChangedListener(IWorkModeChangedListener listener) {
		synchronized (workModeChangedListeners) {
			workModeChangedListeners.add(listener);
		}
	}

	public void remodeWorkModeChangedListner(IWorkModeChangedListener listener) {
		synchronized (workModeChangedListeners) {
			workModeChangedListeners.remove(listener);
		}
	}

	public void run() {
		if (threadDSP == null || threadDSP.isInterrupted()) {
			threadDSP = new Thread(new SignalDSP());
			threadDSP.start();
		}
	}

	public void stop() {
		if (threadDSP != null && !threadDSP.isInterrupted()) {
			threadDSP.interrupt();
		}
	}

	/*
	 * 采集信号的数字处理
	 */
	class SignalDSP implements Runnable {

		int sampleRate = 8000; // 固定为8000sps
		FFTPlan fftPlan = new FFTPlan(sampleRate);
		SignalUtil amplTool = new SignalUtil();
		DSPUtil util = new DSPUtil();
		float[] data1 = new float[sampleRate * 2];
		float[] data2 = new float[sampleRate * 2];

		float[] dataSpectrum = new float[sampleRate];
		float[] peakVal = new float[5];
		int[] peakIndex = new int[5];

		float[] amplDense = new float[25];

		float[] dcacAmpl = new float[2];
		float[] amplTemp = new float[16 * 1024];

		int ignoreLow = 5;

		int[] um71List = new int[] { 1700, 2000, 2300, 2600 };
		int[] ypList = new int[] { 550, 650, 750, 850 };

		@Override
		public void run() {
			// TODO Auto-generated method stub

			IDataBlock dataBlock = addDataBlock(8000, 4000); // 固定为1S个采样点

			try {
				while (!Thread.currentThread().isInterrupted()) {

					float[] sampleData = dataBlock.getBlock(-1);
					if (sampleData == null)
						continue;
					long millisTime = System.currentTimeMillis(); // 获取系统时间
					if (gearNow < 0 || gearNow >= 4)
						continue; // 档位不确定
					if (SignalChannel.this.displayMode == -2)
						continue;

					SignalAmpl signalAmplA = new SignalAmpl();

					for (int i = 0; i < sampleRate; i++) {
						data1[i] = sampleData[i];
					}
					if (SignalChannel.this.displayMode == 0) {
						SignalUnknown signal = new SignalUnknown(50,signalAmplA,
								"A");
						signal.setDCAmpl(0);
						signal.setACAmpl(0);
						signal.putRawData(sampleData);
						signal.putSpectrumData(dataSpectrum);
						SignalChannel.this.setSignal(signal);
						continue;
					}

					fftPlan.realForward(data1, 0);

					for (int i = 0; i < sampleRate / 2; i++) {
						if (i < ignoreLow) {
							dataSpectrum[i] = 0;
						} else {
							dataSpectrum[i] = (float) Math.sqrt(data1[i * 2]
									* data1[i * 2] + data1[i * 2 + 1]
									* data1[i * 2 + 1]);
						}
					}

					
					util.findPeaks(dataSpectrum, ignoreLow, sampleRate / 2,
							peakVal, peakIndex); // 忽略直流信息

					if (peakIndex[0] < 0)
						continue; // 不存在极点

				
					boolean isSingle = false;
					if (peakIndex[1] > 0) {
						float rate = (float) Math.abs((peakVal[0] - peakVal[1])
								/ peakVal[0]);
						if (rate > 0.85f) {
							isSingle = true;
						}
					}

					dcacAmpl[0] = util.calAmplByFreq(data1, sampleRate, 0); // 直流
					dcacAmpl[1] = util.calAmplByFreq(data1, sampleRate,
							peakIndex[0]); // 交流

					// 计算校准过的数值
					float dcAmpl = SignalChannel.this
							.calRealVal(dcacAmpl[0], 0);
					float acAmpl = SignalChannel.this.calRealVal(dcacAmpl[1],
							peakIndex[0]);

					if (isSingle) {
						signalAmplA.addAmpl(acAmpl, millisTime);
						SignalSingle signal = new SignalSingle(peakIndex[0],
								signalAmplA, "A");
						signal.setDCAmpl(dcAmpl);
						signal.setACAmpl(acAmpl);
						signal.putRawData(sampleData);
						signal.putSpectrumData(dataSpectrum);
						SignalChannel.this.setSignal(signal);
						continue;
					}

					if (peakIndex[peakIndex.length - 1] < 0) // 未知信号
					{
						signalAmplA.addAmpl(acAmpl, millisTime);
						SignalUnknown signal = new SignalUnknown(peakIndex[0],signalAmplA,
								"A");
						signal.setDCAmpl(dcAmpl);
						signal.setACAmpl(acAmpl);
						signal.putRawData(sampleData);
						signal.putSpectrumData(dataSpectrum);
						SignalChannel.this.setSignal(signal);
						continue; // 不是移频 和UM71信号
					}

					boolean matchYP = false;
					boolean matchUM71 = false;
					float freqShift = 0;
					int underSampleCount = 1;

					for (int i = 0; i < um71List.length; i++) {
						matchUM71 = true;
						for (int j = 0; j < 3; j++) {
							if ((peakIndex[j] < um71List[i] - 40)
									|| (peakIndex[j] > um71List[i] + 40)) {
								matchUM71 = false;
								break;
							}
						}
						if (matchUM71) {
							float diffFreq = Math.abs(peakIndex[0]
									- peakIndex[1]);
							if (diffFreq < 9.5 || diffFreq > 31)
								matchUM71 = false;
							if (matchUM71)
								break;
						}

					}
					for (int i = 0; i < ypList.length; i++) {
						matchYP = true;
						for (int j = 0; j < 3; j++) {
							if ((peakIndex[j] < ypList[i] - 100)
									|| (peakIndex[j] > ypList[i] + 100)) {
								matchYP = false;
								break;
							}
						}
						if (matchYP)
							break;
					}

					if (matchUM71 == false && matchYP == false) // 未知信号
					{
						signalAmplA.addAmpl(acAmpl, millisTime);
						SignalUnknown signal = new SignalUnknown(peakIndex[0],signalAmplA,
								"A");
						signal.setDCAmpl(dcAmpl);
						signal.setACAmpl(acAmpl);
						signal.putRawData(sampleData);
						signal.putSpectrumData(dataSpectrum);
						SignalChannel.this.setSignal(signal);
						continue;
					}

					if (matchYP) {
						float tmpShiftDiff = Float.MAX_VALUE;

						for (int i = 0; i < peakIndex.length - 2; i++) {
							for (int j = i + 1; j < peakIndex.length - 1; j++) {
								float tmpShift = (peakIndex[i] + peakIndex[j]) / 2f;
								if (Math.abs(tmpShift - 550) < tmpShiftDiff) {
									freqShift = tmpShift;
									tmpShiftDiff = Math.abs(tmpShift - 550);
								}
								if (Math.abs(tmpShift - 650) < tmpShiftDiff) {
									freqShift = tmpShift;
									tmpShiftDiff = Math.abs(tmpShift - 650);
								}
								if (Math.abs(tmpShift - 750) < tmpShiftDiff) {
									freqShift = tmpShift;
									tmpShiftDiff = Math.abs(tmpShift - 750);
								}
								if (Math.abs(tmpShift - 850) < tmpShiftDiff) {
									freqShift = tmpShift;
									tmpShiftDiff = Math.abs(tmpShift - 850);
								}

							}
						}
						util.findPeaks(dataSpectrum, ignoreLow,
								(int) freqShift, peakVal, peakIndex); // 忽略直流信息

						dcacAmpl[1] = util.calAmplByFreq(data1, sampleRate,
								(int) freqShift);

						underSampleCount = 30;

					}
					if (matchUM71) {
						freqShift = peakIndex[0] - 40;
						underSampleCount = 40;
						dcacAmpl[1] = util.calAmplByFreq(data1, sampleRate,
								peakIndex[0]);
					}

					for (int i = 0; i < sampleData.length; i++) {
						data2[i * 2] = sampleData[i];
						data2[i * 2 + 1] = 0;
					}

					// 频谱搬移
					util.shiftSignal(data2, freqShift, sampleRate);
					// Thread.sleep(1);
					// 滤波
					util.complexLowFilter(data2, data1);
					// Thread.sleep(1);
					// 欠采样
					util.underSample(data1, underSampleCount);
					// Thread.sleep(1);
					// 频谱分析
					fftPlan.complexForward(data1, 0);
					// Thread.sleep(1);
					float freqCarrier = 0;
					float freqLow = 0;

					if (matchYP) {
						int signalLength = data1.length / 2;
						util.findComplexPeaks(data1, amplTemp, 0,
								signalLength / 2, peakVal, peakIndex);

						freqLow = Math.abs(peakIndex[0] - peakIndex[1]) * 1f
								/ underSampleCount;

						freqCarrier = freqShift;//
						acAmpl = SignalChannel.this.calRealVal(dcacAmpl[1],
								(int) freqCarrier);
					}
					if (matchUM71) {
						util.findComplexPeaks(data1, peakVal, peakIndex);
						freqCarrier = freqShift + peakIndex[0] * 1f
								/ underSampleCount;
						freqLow = Math.abs(peakIndex[1] - peakIndex[2]) / 2f
								/ underSampleCount;

						acAmpl = SignalChannel.this.calRealVal(dcacAmpl[1],
								(int) freqCarrier);
					}
					signalAmplA.addAmpl(acAmpl, millisTime);
					SignalFSK signalFSK = new SignalFSK(signalAmplA,
							freqCarrier, freqLow, "A");
					signalFSK.setDCAmpl(dcAmpl);
					signalFSK.setACAmpl(acAmpl);
					signalFSK.putRawData(sampleData);
					signalFSK.putSpectrumData(dataSpectrum);
					SignalChannel.this.setSignal(signalFSK);

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				removeDataBlock(dataBlock);
			}

		}

	}

}
