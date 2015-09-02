package com.lon.outsidemonitor.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android_serialport_api.SerialPort;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.lon.outsidemonitor.util.MySerialPort;

public class SerialPortManager {

//	static final String[] portNames = new String[] { "/dev/s3c2410_serial1",
//			"/dev/s3c2410_serial2", "/dev/s3c2410_serial3" };

	static final String[] portNames = new String[] { "/dev/s3c2410_serial2",
		"/dev/s3c2410_serial1",  };
	private final int Baudrate = 576000;

	private static SerialPortManager sigletonManager;

	private UsbManager usbManager;

	private List<MySerialPort> usbList = new ArrayList<MySerialPort>();

	private SerialPortManager(UsbManager usbManager) {
		this.usbManager = usbManager;
	}

	public static void createInstance(UsbManager usbManager) {
		if (sigletonManager == null) {
			sigletonManager = new SerialPortManager(usbManager);
		}
	}

	
	
	public static SerialPortManager getInstance() {

		return sigletonManager;
	}



	
	public MySerialPort getPort(int num)
	{
		if(num<usbList.size())
		{
			return usbList.get(num);
		}
		return null;
	}
	 void reset() {

		for (MySerialPort port : usbList) {
			if (port != null) {
				port.close();
			}
		}
		usbList.clear();

	

		// ±¾µØ´®¿Ú
		File serial = new File(portNames[0]);
		if (serial.exists()) {
			for (int i = 0; i < portNames.length; i++) {
				try {
					SerialPort sp = new SerialPort(new File(portNames[i]),
							Baudrate, 0);
					MySerialPort myport = new MySerialPort(sp);
					usbList.add(myport);

				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		final List<UsbSerialDriver> drivers = UsbSerialProber
				.getDefaultProber().findAllDrivers(usbManager);

		for (final UsbSerialDriver driver : drivers) {
			final List<UsbSerialPort> ports = driver.getPorts();

			for (UsbSerialPort port : ports) {
				UsbDeviceConnection connection = usbManager.openDevice(port
						.getDriver().getDevice());
				if (connection == null) {
					Log.e("OutsideMonitor", "usbport err!");
					continue;
				} else {
					Log.e("OutsideMonitor", "usbport OK!");
				}
				try {

					port.open(connection);
					port.setParameters(Baudrate, 8, UsbSerialPort.STOPBITS_1,
							UsbSerialPort.PARITY_NONE);
					MySerialPort myport = new MySerialPort(port);
					usbList.add(myport);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						port.close();
					} catch (IOException e2) {
						// Ignore.
					}
					e.printStackTrace();
				}
			}
		}
	}

}
