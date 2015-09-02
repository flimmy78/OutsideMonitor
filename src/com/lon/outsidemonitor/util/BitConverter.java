package com.lon.outsidemonitor.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitConverter {

	public static int toInt(byte[] data,int offset)
	{
		ByteBuffer buffer=ByteBuffer.wrap(data,offset,4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		int num=buffer.getInt();
		return num;
	}
	
	public static byte[] getBytes(double val)
	{
		byte[] num=new byte[8];
		ByteBuffer buffer=ByteBuffer.wrap(num, 0, 8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.asDoubleBuffer().put(val);
		return num;
	}
	
	public static byte[] getBytes(float val)
	{
		byte[] num=new byte[4];
		ByteBuffer buffer=ByteBuffer.wrap(num, 0, 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.asFloatBuffer().put(val);
		return num;
	}
	public static byte[] getBytes(long val)
	{
		byte[] num=new byte[8];
		ByteBuffer buffer=ByteBuffer.wrap(num, 0, 8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.asLongBuffer().put(val);
		return num;
	}
	
	public static long toUint(byte[] val,int offset)
	{
		ByteBuffer buffer=ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(val, offset, 4);
		buffer.putInt(0);
		buffer.flip();
		return buffer.getLong();
		
	}
	
	public static float toSingle(byte[] val,int offset)
	{
		ByteBuffer buffer=ByteBuffer.wrap(val, offset, 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.getFloat();
	}
}
