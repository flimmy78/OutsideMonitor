package com.lon.outsidemonitor;

import com.lon.outsidemonitor.core.IWorkModeChangedListener;
import com.lon.outsidemonitor.core.ModuleManager;
import com.lon.outsidemonitor.core.SignalChannel;
import com.lon.outsidemonitor.core.SignalModule;
import com.lon.outsidemonitor.core.WorkMode;
import com.lon.outsidemonitor.signal.ISignal;
import com.lon.outsidemonitor.signal.ISignalChangedListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SignalDetailActivity extends Activity {

	SignalDetailView signalDetailView;
	ISignalChangedListener signalChangedListener;
	IWorkModeChangedListener workModeChangedListener;
	int channelIndex = 0;

	int secondIndex = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle("信号图形分析");
		setContentView(R.layout.activity_signal_detail);

		signalDetailView = (SignalDetailView) findViewById(R.id.signalView);

		Intent intent = getIntent();

		channelIndex = intent.getIntExtra("channel", 0);

		signalChangedListener = new ISignalChangedListener() {

			@Override
			public void onSignalChanged(ISignal signal) {
				// TODO Auto-generated method stub

				if (signal == null) {
					signalDetailView.refreshRawData(null);
					signalDetailView.refreshSpectrumData(null);
				} else {
					float[] rawData = signal.getRawData();
					float[] spectrumData = signal.getSpectrumData();
					signalDetailView.refreshRawData(rawData);
					signalDetailView.refreshSpectrumData(spectrumData);
					signalDetailView.updateSpecCenter((int)signal.getFreq());
					 signalDetailView.addSignalAmpl(signal.getAmpl());
					secondIndex++;
				}
			}
		};

		// 工作模式改变事件
		workModeChangedListener = new IWorkModeChangedListener() {

			@Override
			public void onWorkModeChanged(WorkMode wkMode) {
				// TODO Auto-generated method stub
				if (wkMode != null) {
//					signalDetailView.setLimit(wkMode.getUpper(),
//							wkMode.getLower());
				}
			}
		};

		SignalModule module=ModuleManager.getInstance().getModule(channelIndex / 3);
		final SignalChannel channel = module.getChannel(channelIndex % 3);

		channel.addSignalChangedListener(signalChangedListener); // 信号改变监听
		channel.addWorkModeChangedListener(workModeChangedListener); // 工作模式监听

		WorkMode wkMode = channel.getWorkMode();
		if (wkMode != null) {
			signalDetailView.setLimit(wkMode.getUpper(), wkMode.getLower());
		}
		// 放大按钮
		ImageButton button1 = (ImageButton) findViewById(R.id.xZoomIn);
		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.zoomIn();
			}
		});
		// 缩小按钮
		ImageButton button2 = (ImageButton) findViewById(R.id.xZoomOut);
		button2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.zoomOut();
			}
		});

		// 暂停按钮
		final ImageButton buttonPlay = (ImageButton) findViewById(R.id.buttonPlay);
		buttonPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.stoped = !signalDetailView.stoped;
				if (signalDetailView.stoped) {
					buttonPlay.setImageResource(R.drawable.play);
				} else {
					buttonPlay.setImageResource(R.drawable.pause);
				}
			}
		});

		// 左移
		ImageButton buttonLeft = (ImageButton) findViewById(R.id.moveLeft);
		buttonLeft.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.moveXCursor(-10);
			}
		});

		// 右移
		ImageButton buttonRight = (ImageButton) findViewById(R.id.moveRight);
		buttonRight.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.moveXCursor(+10);
			}
		});

		// 上移
		ImageButton buttonUp = (ImageButton) findViewById(R.id.moveUp);
		buttonUp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.moveYCursor(5);
			}
		});
		// 上移
		ImageButton buttonDown = (ImageButton) findViewById(R.id.moveDown);
		buttonDown.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.moveYCursor(-5);
			}
		});

		ImageButton buttonYZoomIn = (ImageButton) findViewById(R.id.yZoomIn);
		buttonYZoomIn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.zoomYIn();
			}
		});

		ImageButton buttonYZoomOut = (ImageButton) findViewById(R.id.yZoomOut);
		buttonYZoomOut.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				signalDetailView.zoomYOut();
			}
		});

		Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		// 将可选内容与ArrayAdapter连接起来
		ArrayAdapter adapter2 = ArrayAdapter.createFromResource(this,
				R.array.display_mode, android.R.layout.simple_spinner_item);

		// 设置下拉列表的风格
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// 将adapter2 添加到spinner中
		spinner1.setAdapter(adapter2);

		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0:
					signalDetailView.setDisplayMode(DrawMode.SignalData);
					channel.setDisplayMode(0);
					break;
				case 1:
					signalDetailView.setDisplayMode(DrawMode.SignalAmpl);
					channel.setDisplayMode(1);
					break;
				
				case 2:
					signalDetailView.setDisplayMode(DrawMode.SignalFreq);
					channel.setDisplayMode(2);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		// 设置默认值
		spinner1.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signal_detail, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		SignalChannel channel = ModuleManager.getInstance()
				.getModule(channelIndex / 3).getChannel(channelIndex % 3);

		channel.removeSignalChangedListener(signalChangedListener);
		channel.setDisplayMode(-1);

	}

}
