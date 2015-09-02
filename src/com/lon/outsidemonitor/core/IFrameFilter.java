package com.lon.outsidemonitor.core;

public interface IFrameFilter {

	byte[] getFrame(int timeout);
}
