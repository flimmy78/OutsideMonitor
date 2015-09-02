package com.lon.outsidemonitor;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.lon.outsidemonitor.core.ModuleManager;
import com.lon.outsidemonitor.core.SignalChannel;
import com.lon.outsidemonitor.core.WorkMode;



import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WorkModeActivity extends Activity {

	WorkModeAdapter modeAdapter;
	Timer refreshTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTitle("ͨ��ģʽ����");
		setContentView(R.layout.activity_mode_setting);

		ListView listView = (ListView) findViewById(R.id.listView1);

		modeAdapter = new WorkModeAdapter(this);

		listView.setAdapter(modeAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				if((position%4)==0) return;
				SignalChannel channel = ModuleManager.getInstance().getModule(position/4)
						.getChannel((position%4)-1);

				List<WorkMode> listModes = channel.getModeList();

				if (listModes == null || listModes.size() <= 0) {
					Toast.makeText(WorkModeActivity.this,
							"ͨ��" + ((position%4) - 1) + "�޵�λ��Ϣ", Toast.LENGTH_SHORT)
							.show();
				} else {
					setWorkMode(listModes, position, channel.getWorkMode());
				}

			}

		});

		refreshTimer = new Timer();

		refreshTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				mHandler.sendEmptyMessage(0);

			}
		}, 1000, 1000);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				modeAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	private void setWorkMode(final List<WorkMode> listModes,
			final int channelNum, WorkMode currentMode) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("ͨ��" + ((channelNum%4) - 1) + "ģʽ����");
		int cnt = listModes.size();
		String[] modeInfos = new String[cnt];
		int modeCode = -1;
		int selectIndex = 0;
		if (currentMode != null) {
			modeCode = currentMode.getGear();
		}
		for (int i = 0; i < cnt; i++) {
			WorkMode mode = listModes.get(i);
			StringBuilder sb = new StringBuilder();
			
			sb.append(mode.getGearInfo());
			
			modeInfos[i] = sb.toString();
			if (mode.getGear() == modeCode) {
				selectIndex = i;
			}
		}

		builder.setSingleChoiceItems(modeInfos, selectIndex, null);

		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				SignalChannel channel = ModuleManager.getInstance().getModule(channelNum/4).getChannel((channelNum%4)-1);
				
				int gear = ((AlertDialog) dialog).getListView()
						.getCheckedItemPosition();
				channel.setGear(gear);
			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});

		builder.show();

	}

}
