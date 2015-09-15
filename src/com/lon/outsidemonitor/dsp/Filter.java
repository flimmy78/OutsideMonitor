package com.lon.outsidemonitor.dsp;

public class Filter {

	public final int Order = 6; // 6½×
	private float[] Xi = new float[Order + 1];
	private float[] Yi = new float[Order];
	int filterIndex = -1;
	private static final String[] FilterMode=new String[]{"²»ÂË²¨","µÍÍ¨","550","650","750","850","1700","2000","2300","2600"};
	private static final float[][] A = new float[][] {

			new float[] { 1,   -4.787135601f,    9.649518013f,   -10.46907902f,    6.441112041f,
				     -2.129038811f,   0.2951724231f}, // 400µÍÍ¨
			new float[] { 1,   -5.179812431f,    11.64168262f,   -14.46986103f,    10.48227596f,
				     -4.199623108f,   0.7301653624f }, // 550
			new float[] { 1,   -4.976490498f,    10.95242214f,   -13.51976871f,    9.861737251f,
				     -4.034776211f,   0.7301653624f },// 650
			new float[] {1,   -4.742486477f,    10.19330883f,   -12.48299885f,    9.178311348f,
				     -3.845053673f,   0.7301653624f },// 750
			new float[] { 1,   -4.479243755f,    9.383034706f,   -11.38578701f,    8.448824883f,
				     -3.631624937f,   0.7301653624f }, // 850

			new float[] { 1,   -1.331511736f,    3.277927399f,   -2.479004622f,    2.952423096f,
				     -1.079546332f,   0.7301653624f}, // 1700
			new float[] { 1,-7.245034375e-16f,    2.686157465f,-1.146492975e-15f,    2.419655085f,
					  -4.529407571e-16f,   0.7301653624f }, // 2000
			new float[] { 1,    1.331511736f,    3.277927399f,    2.479004622f,    2.952423096f,
				      1.079546332f,   0.7301653624f}, // 2300
			new float[] { 1,    2.589443922f,    4.924239159f,     5.29591608f,     4.43459034f,
				       2.09943676f,   0.7301653624f }, // 2600
	};

	private static final float[][] B = new float[][] {

			new float[] { 8.576556866e-06f,5.145934119e-05f,0.0001286483603f,0.0001715311373f,0.0001286483603f,
					  5.145934119e-05f,8.576556866e-06f},

			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f }, //550
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f}, //650
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f }, //750
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f}, //850

			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f}, //1700
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f}, //2000
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f }, //2300
			new float[] { 0.0004165461287f,              0,-0.001249638386f,              0, 0.001249638386f,
	                0,-0.0004165461287f }, //2600
					};

	public void setFilterIndex(int index) {
		this.filterIndex = index;
	}
	
	public int getFilterIndex()
	{
		return this.filterIndex;
	}


	public float doFilter(float ad) {
		if (filterIndex < 0)
			return ad;

		float y = 0;

	
		for (int i = 0; i < Order; i++) {
			Xi[i] = Xi[1 + i];
		}
		Xi[Order] = ad;

		for (int j = 0; j <= Order; j++) {
			y += B[filterIndex][j] * Xi[j];
			if (j > 0) {
				y -= A[filterIndex][j] * Yi[Order-j];
			}
		}

		for (int i = 0; i < Order - 1; i++) {
			Yi[i] = Yi[1 + i];
		}
		Yi[Order - 1] = y;

		return y;
	}
	
	public String toString()
	{
		int mode=filterIndex+1;
		return FilterMode[mode];
	}

}
