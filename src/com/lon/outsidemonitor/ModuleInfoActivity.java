package com.lon.outsidemonitor;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.lon.outsidemonitor.core.ModuleInfo;
import com.lon.outsidemonitor.core.ModuleManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ModuleInfoActivity extends Activity {

	private ListView listview;
	ModuleInfoAdapter adapter;
	Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_module_info);
		this.setTitle("���ݲ���");
		

		

		listview = (ListView) findViewById(R.id.listView_list);

		List<ModuleInfo> list = ModuleManager.getInstance().getModuleInfos();
		adapter = new ModuleInfoAdapter(this, list);
		listview.setAdapter(adapter);

		listview.setSelection(ModuleInfoAdapter.SelectIndex);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ModuleInfoAdapter.SelectIndex=position;
				Intent intent=new Intent(ModuleInfoActivity.this, SignalDetailActivity.class);
				intent.putExtra("channel", position/4*3+(position%4)-1); //ͨ�������
				startActivity(intent);
				
				
			}
		});
		
		
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		TimerTask task = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};

		timer = new Timer(true);
		timer.schedule(task, 1000, 1000); // ��ʱ1000ms��ִ�У�1000msִ��һ��

	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.host_info, menu);
//		return true;
//	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		switch (item.getItemId()) { // ��Ӧÿ���˵���(ͨ���˵����ID)
//		case R.id.action_version:
//			startActivity(new Intent(this,HostVersionActivity.class));
//			break;
//		case R.id.action_param:
//			startActivity(new Intent(this,HostParamActivity.class));
//			break;
//		default: // ��û�д�����¼�����������������
//			return super.onOptionsItemSelected(item);
//		} // ����true��ʾ������˵�����¼�������Ҫ�����¼�����������ȥ��
//		return true;
//	}


}
