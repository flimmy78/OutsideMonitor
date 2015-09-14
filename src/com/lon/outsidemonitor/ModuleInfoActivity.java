package com.lon.outsidemonitor;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import com.lon.outsidemonitor.core.ModuleInfo;
import com.lon.outsidemonitor.core.ModuleManager;
import com.lon.outsidemonitor.core.SignalChannel;
import com.lon.outsidemonitor.core.SignalModule;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
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
		this.setTitle("数据测试");
		

		

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
				intent.putExtra("channel", position/4*3+(position%4)-1); //通道的序号
				startActivity(intent);
				
				
			}
		});
		
		
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@Override
			public void onCreateContextMenu(ContextMenu menu, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub
              menu.add(0, 0, 0, "不滤波"); 
              menu.add(0, 1, 0, "[000-400]低通"); 
              menu.add(0, 2, 0, "[500-600]带通"); 
              menu.add(0, 3, 0, "[600-700]带通"); 
              menu.add(0, 4, 0, "[700-800]带通"); 
              menu.add(0, 5, 0, "[800-900]带通"); 
              menu.add(0, 6, 0, "[1650-1750]带通"); 
              menu.add(0, 7, 0, "[1950-2050]带通"); 
              menu.add(0, 8, 0, "[2250-2350]带通"); 
              menu.add(0, 9, 0, "[2550-2650]带通"); 
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
		timer.schedule(task, 1000, 1000); // 延时1000ms后执行，1000ms执行一次

	}
	
	 // 长按菜单响应函数 
	@Override
    public boolean onContextItemSelected(MenuItem item) { 

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo(); 
            
            int id=item.getItemId();
            
            int pos= listview.getSelectedItemPosition();
           
            
          SignalModule module=  ModuleManager.getInstance().getModule(pos/4);
          if(module!=null)
          {
        	SignalChannel channel=  module.getChannel((pos%4)-1);
        	if(channel!=null)
        	{
        		channel.setFilter(id-1);
        	}
          }
            
          return super.onContextItemSelected(item); 

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
//		switch (item.getItemId()) { // 响应每个菜单项(通过菜单项的ID)
//		case R.id.action_version:
//			startActivity(new Intent(this,HostVersionActivity.class));
//			break;
//		case R.id.action_param:
//			startActivity(new Intent(this,HostParamActivity.class));
//			break;
//		default: // 对没有处理的事件，交给父类来处理
//			return super.onOptionsItemSelected(item);
//		} // 返回true表示处理完菜单项的事件，不需要将该事件继续传播下去了
//		return true;
//	}


}
