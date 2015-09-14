package com.lon.outsidemonitor.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DataBlock implements IDataBlock {

	LinkedList<float[]> listFrame = new LinkedList<float[]>();
	Queue<Short> queue = new LinkedList<Short>();

	
	float[] ringBuffer;
	int ringBegin=0;
	int ringLength=0;
	ReentrantLock lock = new ReentrantLock();
	Condition condition = lock.newCondition();

	int window = 8000;
	int slide = 8000;

	int slideNum = 0;

	int prevSampleIndex = -1;

	public DataBlock(int window, int slide) {

		this.window = window;
		this.slide = slide;
		this.ringBuffer=new float[window];
	}

	public void putSampleData(float[] data, int offset, int length) {
		
	

		int endIndex;
		while (slideNum > 0) {
			if (ringLength<= 0)
				break;
			ringBegin=(ringBegin+1)%this.window;
			ringLength--;
			slideNum--;
		}
		for (int i = 0; i < length; i++) {
			if (slideNum > 0) {
				slideNum--;

			} else {
				
				endIndex=(ringBegin+ringLength)%this.window;
				ringBuffer[endIndex]=data[offset + i];
				
				ringLength++;
				
				if (ringLength == this.window) {

					float[] dataSample = new float[this.window];

					for(int j=0;j<ringLength;j++)
					{
						endIndex=(ringBegin+j)%this.window;
						dataSample[j]=ringBuffer[endIndex];
					}

					slideNum = this.slide;

					lock.lock();
					try {
						if (listFrame.size() > 100) {
							listFrame.removeFirst();
						}
						listFrame.addLast(dataSample);
						condition.signal();
					} finally {
						lock.unlock();
					}
				}
			}
		}
	}

	public void putSampleData(float[] data, int offset, int length,
			int sampleIndex) {

		if ((prevSampleIndex >= 0) && (prevSampleIndex + 1) != sampleIndex) {

			//²¹È«Êý¾Ý
			putSampleData(data,offset,length);
		}
		
		putSampleData(data,offset,length);
		prevSampleIndex=sampleIndex;
	}

	@Override
	public float[] getBlock(int timeout) {
		// TODO Auto-generated method stub
		float[] frame = null;
		lock.lock();
		try {

			if (listFrame.size() > 0) {
				frame = listFrame.removeFirst();
			}
			if (frame == null && timeout != 0) {
				try {
					if (timeout > 0) {

						condition.await(timeout, TimeUnit.MILLISECONDS);

					} else {
						condition.await();
					}
				} catch (InterruptedException e) {
					// TODO: handle exception
				}

			}
			if (listFrame.size() > 0) {
				frame = listFrame.removeFirst();
			}
		} finally {
			lock.unlock();
		}
		return frame;
	}

	@Override
	public int getSampleRate() {
		// TODO Auto-generated method stub
		return 0;
	}

}
