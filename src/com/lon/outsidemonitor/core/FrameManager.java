package com.lon.outsidemonitor.core;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.lon.outsidemonitor.core.IFrameFilter;
import com.lon.outsidemonitor.util.MySerialPort;

public class FrameManager {

	Thread rxThread; // 读串口数据
	LinkedList<FrameFilter> listFilter = new LinkedList<FrameFilter>();

	MySerialPort serialPort;

	public FrameManager(MySerialPort serialPort) {
		// TODO Auto-generated constructor stub
		this.serialPort = serialPort;
		rxThread = new Thread(new SerialRead(serialPort, 0));
		//rxThread.setPriority(Thread.MAX_PRIORITY);
		rxThread.start();

	}

	/*
	 * 添加数据帧过滤
	 */
	public IFrameFilter createFilter() {
		FrameFilter filter = new FrameFilter();

		synchronized (listFilter) {
			listFilter.add(filter);
		}
		return filter;
	}

	public void removeFilter(IFrameFilter filter) {
		if (FrameFilter.class.isInstance(filter)) {
			FrameFilter frameFilter = FrameFilter.class.cast(filter);
			synchronized (listFilter) {
				listFilter.remove(frameFilter);
			}
		}
	}

	private void addFrame(byte[] frame, int length) {
		synchronized (listFilter) {

			for (FrameFilter filter : listFilter) {
				filter.putFrame(frame, length);
			}

		}
	}

	public void close() {
		if (rxThread != null && rxThread.isAlive()) {
			rxThread.interrupt();
		}
	}

	class FrameFilter implements IFrameFilter {

		LinkedList<byte[]> listFrame = new LinkedList<byte[]>();
		ReentrantLock lock = new ReentrantLock();
		Condition condition = lock.newCondition();
		int[] cmdId;

		public FrameFilter(int[] cmdId) {
			// TODO Auto-generated constructor stub
			this.cmdId = cmdId;
		}

		public FrameFilter() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public byte[] getFrame(int timeout) {
			// TODO Auto-generated method stub
			byte[] frame = null;
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

		protected void putFrame(byte[] frame, int length) {

			// 先判断是否匹配
			int cmd = frame[5];

			if (cmdId != null) // 必须匹配命令
			{
				boolean match = false;
				for (int i = 0; i < cmdId.length; i++) {
					if (cmdId[i] == cmd) {
						match = true;
						break;
					}
				}
				if (match == false)
					return;
			}
			byte[] data = new byte[length];
			System.arraycopy(frame, 0, data, 0, length);

			lock.lock();
			try {
				if (listFrame.size() > 100) {
					listFrame.removeFirst();
				}
				listFrame.addLast(data);
				condition.signal();

			} finally {
				lock.unlock();
			}
		}

	}

	private class SerialRead implements Runnable {

		MySerialPort port = null;
		int portIndex = 0;

		private final int BufferSize = 1024 * 128;

		byte[] buffer = new byte[BufferSize * 2];
		byte[] bufferHalf = new byte[BufferSize];
		byte[] frameBuffer = new byte[BufferSize];
		boolean rollback = false;
		int dataLength = 0;
		int frameLength = 0;
		int leftLength = 0;
		int realLength = 0;

		public SerialRead(MySerialPort port, int portIndex) {
			this.port = port;
			this.portIndex = portIndex;
		}

		public short CalSUMCheck(byte[] data, int index, int cnt) {

			int sum = 0;
			for (int i = index; i < index + cnt; i++) {

				sum += (data[i] & 0xff);
			}

			return (short) (sum & 0xffff);

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				while (Thread.currentThread().isInterrupted() == false) {
					dataLength = 0;
					Thread.sleep(5);
					if (rollback) // 前一个数据帧回滚
					{
						for (int i = 1; i < frameLength; i++) {
							if (frameBuffer[i] == (byte) 0xaa
									&& ((i == frameLength - 1) || (frameBuffer[i + 1] == (byte) 0xaa))) {
								dataLength = frameLength - i;
								System.arraycopy(frameBuffer, i, buffer, 0,
										dataLength);
								break;
							}
						}
						frameLength = 0;
						rollback = false;
					}

					if (leftLength > 0) // 前一个BUffer剩下的Data
					{
						System.arraycopy(buffer, buffer.length / 2, buffer,
								dataLength, leftLength);
						dataLength += leftLength;
						leftLength = 0;
					}

					if (dataLength <= 0) // 串口数据
					{

						dataLength = port.read(bufferHalf, 5000);
						
						if (dataLength <= 0) {
							rollback = true;
							continue;
		
						} else {
							System.arraycopy(bufferHalf, 0, buffer, 0,
									dataLength);
						}
					}

					for (int i = 0; i < dataLength; i++) {
						frameBuffer[frameLength++] = buffer[i];
						leftLength = dataLength - i - 1;
						switch (frameLength) {
						case 1:
						case 2:
							if (frameBuffer[frameLength - 1] != (byte) 0xaa) // 帧头
							{
								rollback = true;
							}
							break;
						case 3:
							if (frameBuffer[2] != 1) // 版本协议
							{
								rollback = true;
							}
							break;
						case 4:
							break;
						case 5: {
							realLength = (frameBuffer[3] & 0xff)
									| ((frameBuffer[4] & 0xff) << 8);// 数据长度
							if (realLength + 9 > frameBuffer.length
									|| (realLength <= 0)) {
								rollback = true;
							}
						}
							break;
						default:
							if (frameLength == (realLength + 9)) // 收到完整数据
							{
								short calSum = CalSUMCheck(frameBuffer, 5,
										realLength);

								short realSum = (short) ((frameBuffer[realLength + 5] & 0xff) | ((frameBuffer[realLength + 6] & 0xff) << 8));
								if (calSum != realSum) {
									rollback = true;
									Log.e("CRC", "校验和");
								} else {

									addFrame(frameBuffer, frameLength);
									frameLength = 0;
								}

							}
							break;
						}
						if (rollback || frameLength >= frameBuffer.length) {
							System.arraycopy(buffer, i + 1, buffer,
									buffer.length / 2, leftLength);

							break;
						}
					}

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

}
