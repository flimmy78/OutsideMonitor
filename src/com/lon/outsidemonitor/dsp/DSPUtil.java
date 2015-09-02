package com.lon.outsidemonitor.dsp;

import java.util.ArrayList;
import java.util.Arrays;

public class DSPUtil {

	static final float[] A = new float[] { 1, -2.686157396548f,
			2.419655110966f, -0.7301653453057f }; // A[0]始终为1
	static final float[] B = new float[] { 0.0004165461390757f,
			0.001249638417227f, 0.001249638417227f, 0.0004165461390757f };

	public float calAmplByFreq(float[] data, int fftNum, int freq) {

		if (freq == 0)
			return data[0] / fftNum;

		int startIndex = freq;
		int endIndex = freq;

		ArrayList<Integer> listIndex = new ArrayList<Integer>();

		if (freq < 450) {
			startIndex = freq - 2 > 0 ? freq - 2 : 0;
			endIndex = freq + 2;
			listIndex.add(startIndex);
			listIndex.add(endIndex);
			// 偶次谐波
			startIndex = freq * 2 - 2 > 0 ? freq * 2 - 2 : 0;
			endIndex = freq * 2 + 2;
			listIndex.add(startIndex);
			listIndex.add(endIndex);
			// 奇次谐波
			startIndex = freq * 3 - 2 > 0 ? freq * 3 - 2 : 0;
			endIndex = freq * 3 + 2;
			listIndex.add(startIndex);
			listIndex.add(endIndex);
		} else if (freq < 1000) {
			startIndex = freq - 150 > 0 ? freq - 150 : 0;
			endIndex = freq + 150;
			listIndex.add(startIndex);
			listIndex.add(endIndex);
		} else if (freq < 3000) {
			startIndex = freq - 100 > 0 ? freq - 100 : 0;
			endIndex = freq + 100;
			listIndex.add(startIndex);
			listIndex.add(endIndex);
		}

		float ampl = 0;

		for (int i = 0; i < listIndex.size() / 2; i++) {
			startIndex = listIndex.get(i * 2);
			endIndex = listIndex.get(i * 2 + 1);

			for (int j = startIndex; j <= endIndex; j++) {
				ampl += (data[j * 2] * data[j * 2] + data[j * 2 + 1]
						* data[j * 2 + 1]);
			}
		}

		ampl = (float) (Math.sqrt(ampl) * 2 / fftNum / Math.sqrt(2f));
		return ampl;
	}

	/*
	 * @param ampl 信号幅度
	 * 
	 * @param peaks 峰值
	 * 
	 * @index 峰值的索引
	 */
	public void findPeaks(float[] ampl, int from, int to, float[] peaks,
			int[] index) {
		// int length = ampl.length;
		int peakCnt = Math.min(peaks.length, index.length);

		for (int i = 0; i < index.length; i++)// -1表示没发现极值
		{
			index[i] = -1;
		}
		float peakVal = 0;
		int findPeakCnt = 0;
		for (int i = from + 1; i < to; i++) {
			if (ampl[i] > ampl[i - 1]) {
				peakVal = ampl[i];
				findPeakCnt = 1;
			} else if (ampl[i] < ampl[i - 1]) {
				if (findPeakCnt > 0) // 找到极值
				{
					for (int j = 0; j < peakCnt; j++) {
						if (index[j] < 0) {
							peaks[j] = peakVal;
							index[j] = i - (findPeakCnt + 1) / 2;
							break;
						} else if (peakVal > peaks[j]) { // 找到插入的位置
							for (int k = peakCnt - 2; k >= j; k--) // 移动
							{
								peaks[k + 1] = peaks[k];
								index[k + 1] = index[k];
							}
							peaks[j] = peakVal;
							index[j] = i - (findPeakCnt + 1) / 2;
							break;
						}
					}
				}
				findPeakCnt = 0;
			} else if (findPeakCnt > 0) { // 相等
				findPeakCnt++;
			}
		}

	}

	public void findComplexPeaks(float[] signal, int signalLength,
			float[] peaks, int[] index) {

		int length = signalLength / 2;
		float[] ampl = new float[length];
		for (int i = 0; i < length; i++) {
			ampl[i] = signal[2 * i] * signal[2 * i] + signal[2 * i + 1]
					* signal[2 * i + 1];

		}

		findPeaks(ampl, 0, ampl.length, peaks, index);

	}

	public void findComplexPeaks(float[] signal, float[] peaks, int[] index) {
		findComplexPeaks(signal, signal.length / 2, peaks, index);
	}

	public void findComplexPeaks(float[] signal, float[] ampl, int from,
			int to, float[] peaks, int[] index) {
		for (int i = from; i < to; i++) {
			ampl[i - from] = signal[2 * i] * signal[2 * i] + signal[2 * i + 1]
					* signal[2 * i + 1];

		}
		findPeaks(ampl, 0, to - from, peaks, index);

	}

	public void shiftSignal(float[] signal, float freq, int sampleRate) {
		int length = signal.length / 2;

		for (int i = 0; i < length; i++) {
			double alpha = 2 * Math.PI * freq * i / sampleRate;
			double real = Math.cos(alpha);
			double img = Math.sin(alpha);

			double rReal = signal[2 * i] * real + signal[2 * i + 1] * img;
			double rImg = signal[2 * i + 1] * real - signal[2 * i] * img;

			signal[2 * i] = (float) rReal;
			signal[2 * i + 1] = (float) rImg;
		}
	}

	public void complexLowFilter(float[] orignal, float[] filterData) {

		if (orignal == null || orignal.length <= 1)
			return;
		if (filterData.length < orignal.length)
			return;
		int length = orignal.length / 2;

		int order = A.length - 1;
		int paddingLenth = length + order; // 补零
		float[] xData = new float[paddingLenth * 2];
		float[] yData = new float[paddingLenth * 2];

		Arrays.fill(yData, 0);
		Arrays.fill(xData, 0, order * 2, 0);

		System.arraycopy(orignal, 0, xData, order * 2, orignal.length);

		for (int i = order; i < paddingLenth; i++) {
			for (int j = 0; j <= order; j++) {
				yData[i * 2] += B[j] * xData[(i - j) * 2];
				yData[i * 2 + 1] += B[j] * xData[(i - j) * 2 + 1];
				if (j > 0) {
					yData[i * 2] -= A[j] * yData[(i - j) * 2];
					yData[i * 2 + 1] -= A[j] * yData[(i - j) * 2 + 1];
				}
			}
		}

		System.arraycopy(yData, 2 * order, filterData, 0, filterData.length);

	}

	public void underSample(float[] signal, int sampleInternal) {
		int length = signal.length / 2;
		int cnt = length / sampleInternal;
		for (int i = 1; i < cnt; i++) {
			signal[i * 2] = signal[i * 2 * sampleInternal];
			signal[i * 2 + 1] = signal[i * 2 * sampleInternal + 1];
		}

		for (int i = 2 * cnt; i < signal.length; i++) {
			signal[i] = 0;
		}
	}
}
