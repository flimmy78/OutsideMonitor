package com.lon.outsidemonitor.core;

import java.util.ArrayList;
import java.util.Calendar;

import com.lon.outsidemonitor.util.MySerialPort;

public class SignalModule {


	MySerialPort port = null;
	int moduleNum = -1;

	private Thread threadRcv; // 查询参数的读取
	private ChannelCollection channels; // 信号通道

	DevInfo moduleInfo = null;


	
	public SignalModule(int num) {
		this.port = SerialPortManager.getInstance().getPort(num);
		this.moduleNum = num;
		channels = new ChannelCollection(this, 3);
	}
	
	public boolean isValid()
	{
		return (this.port==null) ? false:true;
	}

	public int getModuleNum() {
		return moduleNum;
	}

	public SignalChannel getChannel(int index) {

		return channels.getChannel(index);
	}

	private void requestDevInfo() {
		byte[] frame = new byte[] { (byte) 0x91, };
		this.sendFrame(frame);
	}

	public DevInfo getDevInfo() {
		return moduleInfo;
	}

	class FrameRcv implements Runnable {

		int frameNum=-1;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (port == null)
				return;
			FrameManager frameManager = port.getFrameManager();
			IFrameFilter filter = frameManager.createFilter();
			try {
				while (Thread.currentThread().isInterrupted() == false) {
					byte[] frame = filter.getFrame(-1);

					if (frame == null)
						continue;
					frameNum++;
					if(frameNum>=10)
					{
						frameNum=0;
					}
					if ((moduleInfo == null) && (frameNum==0)) {
						requestDevInfo();
					}
					if (frame[5] == 0x11) // 设备信息
					{
						moduleInfo = new DevInfo(frame);
						channels.updateChannel();
					}
					channels.processFrame(frame);

				}

			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				frameManager.removeFilter(filter);
			}

		}

	}

	public void run() {
		if (threadRcv == null || threadRcv.isInterrupted()) {
			threadRcv = new Thread(new FrameRcv());
			threadRcv.start();
		}
		// 开启通道
		channels.run();
	}

	public void stop() {
		if (threadRcv != null && threadRcv.isInterrupted() == false) {
			threadRcv.interrupt();
		}
		// 停止通道
		channels.stop();
	}

	public void sendFrame(byte[] frame) {
		if (port == null)
			return;
		int length = 9 + frame.length;
		byte[] totalFrame = new byte[length];
		// 帧头
		totalFrame[0] = (byte) 0xaa;
		totalFrame[1] = (byte) 0xaa;
		// 版本 1
		totalFrame[2] = 0x01;
		// 数据长度
		totalFrame[3] = (byte) (frame.length & 0xff);
		totalFrame[4] = (byte) ((frame.length >> 8) & 0xff);
		// 数据
		int checkSum = 0;
		for (int i = 0; i < frame.length; i++) {
			totalFrame[5 + i] = frame[i];
			checkSum += (frame[i] & 0xff);
		}
		totalFrame[5 + frame.length] = (byte) (checkSum & 0xff);
		totalFrame[6 + frame.length] = (byte) ((checkSum >> 8) & 0xff);
		totalFrame[7 + frame.length] = (byte) '\r';
		totalFrame[8 + frame.length] = (byte) '\n';

		port.write(totalFrame, 1000);
	}

}

class ChannelCollection {

	ArrayList<SignalChannel> listChannels = new ArrayList<SignalChannel>();

	public ChannelCollection(SignalModule module, int channelNum) {
		// TODO Auto-generated constructor stub
		for (int i = 0; i < channelNum; i++) {
			listChannels.add(new SignalChannel(module, i));
		}
	}

	public SignalChannel getChannel(int index) {
		if (index >= 0 && index < listChannels.size()) {
			return listChannels.get(index);
		}
		return null;
	}

	public void processFrame(byte[] frame) {

		byte cmd = frame[5];

		switch (cmd) {

		case (byte) 0x13: // 参数信息
		case (byte) 0x14: // 当前档位
		case (byte) 0x15:// AD数据
		{
			byte channel = frame[6];
			if (channel >= 0 && channel < listChannels.size()) {
				listChannels.get(channel).putFrame(frame);
			}
		}
			break;
		}

	}

	
	public void updateChannel()
	{
		for (SignalChannel channel : listChannels) {
			channel.updateChannel();
		}
	}
	public void run() {
		for (SignalChannel channel : listChannels) {
			channel.run();
		}
	}

	public void stop() {
		for (SignalChannel channel : listChannels) {
			channel.stop();
		}
	}

}

