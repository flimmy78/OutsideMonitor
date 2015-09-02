package com.lon.outsidemonitor;

import java.util.List;

import com.lon.outsidemonitor.core.ModuleInfo;
import com.lon.outsidemonitor.signal.ISignal;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class ModuleInfoAdapter extends BaseAdapter{  
	  
	 Activity activity;
	 List<ModuleInfo> list;
	 public static int SelectIndex=1;
	public ModuleInfoAdapter(Activity activity,List<ModuleInfo> list)
	 {
		 this.activity=activity;
		 this.list=list;
	 }
	 

  @Override  
  public int getCount() {  
      // TODO Auto-generated method stub  
      return list.size();  
  }  

  @Override  
  public Object getItem(int position) {  
      // TODO Auto-generated method stub  
      return list.get(position);  
  }  

  @Override  
  public long getItemId(int position) {  
      // TODO Auto-generated method stub  
      return position;  
  }  
  @Override  
  public boolean isEnabled(int position) {  
      // TODO Auto-generated method stub  
       if(list.get(position).isGroup()) return false;
       return super.isEnabled(position);  
  }  
  @Override  
  public View getView(int position, View convertView, ViewGroup parent) {  
      // TODO Auto-generated method stub  
      View view=convertView;  
      ModuleInfo info=list.get(position);
      if(info.isGroup()){  
          view=LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.list_item_module_info_grp, null);  
      }else{  
          view=LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.list_item_module_info, null);  
          ImageView img=(ImageView)view.findViewById(R.id.addexam_list_icon);
          img.setBackgroundResource(info.getImageId());
      }  
      TextView text=(TextView) view.findViewById(R.id.addexam_list_item_text);  
      String name=info.getName();
      if(info.isGroup()==false)
      {
    	  ISignal signal=info.getSignal();
    	  if(signal!=null)
    	  {
    		  name+=signal.getSignalInfo();
    	  }
      	
      }
      text.setText(name);  
      return view;  
  }  
    
}  