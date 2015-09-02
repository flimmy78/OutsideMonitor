package com.lon.outsidemonitor.util;

import java.io.IOException;

import android_serialport_api.SerialPort;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.lon.outsidemonitor.core.FrameManager;

public class MySerialPort {

	UsbSerialPort usbPort = null;
	SerialPort filePort = null;

	FrameManager frameManager = null;

	public MySerialPort(UsbSerialPort port) {
		usbPort = port;
		this.frameManager = new FrameManager(this);
	}

	public MySerialPort(SerialPort sp) {
		this.filePort = sp;
		this.frameManager = new FrameManager(this);
	}

	public int read(byte[] data, int timeoutMillis) {

		try {
			if (usbPort != null) {
				return usbPort.read(data, timeoutMillis);
			} else if (filePort != null) {
				return filePort.getInputStream().read(data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public int write(byte[] data, int timeoutMillis) {
		try {
			if (usbPort != null) {
				return usbPort.write(data, timeoutMillis);
			} else if (filePort != null) {
				filePort.getOutputStream().write(data);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return 0;
	}

	public void close() {

		try {
			if (usbPort != null) {
				usbPort.close();
			} else if (filePort != null) {
				filePort.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public FrameManager getFrameManager() {
		return frameManager;
	}

}
