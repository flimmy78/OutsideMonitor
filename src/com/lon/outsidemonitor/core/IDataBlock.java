package com.lon.outsidemonitor.core;

public interface IDataBlock {

	float[] getBlock(int timeout);

    int getSampleRate();
}
