package com.lon.outsidemonitor.core;

public interface IDataBlock {

	short[] getBlock(int timeout);

    int getSampleRate();
}
