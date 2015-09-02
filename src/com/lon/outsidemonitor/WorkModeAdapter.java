package com.lon.outsidemonitor;

import com.lon.outsidemonitor.core.DevInfo;
import com.lon.outsidemonitor.core.ModuleManager;
import com.lon.outsidemonitor.core.SignalChannel;
import com.lon.outsidemonitor.core.SignalModule;
import com.lon.outsidemonitor.core.WorkMode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WorkModeAdapter extends BaseAdapter {

	Context mContext;
	ModuleManager moduleManager;

	public WorkModeAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.moduleManager = ModuleManager.getInstance();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return moduleManager.getModuleNum() * 4; // 包含标题栏
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if ((position % 4) == 0)
			return null;
		return moduleManager.getModule(position / 4).getChannel(
				position % 4 - 1);
	}

	@Override
	public long getItemId(int id) {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {

			if ((position % 4) == 0) {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.list_item_module_info_grp, null);
				TextView textView = (TextView) convertView
						.findViewById(R.id.addexam_list_item_text);
				textView.setText(getModeString(position));
				convertView.setTag(new ViewHolder(null, textView, null));
			} else {
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.mode_setting, null);

				ImageView imgView = (ImageView) convertView
						.findViewById(R.id.imageView1);
				imgView.setImageResource(R.drawable.gear_wheel);
				TextView textView = (TextView) convertView
						.findViewById(R.id.textView1);

				textView.setText(getModeString(position));
				ImageView imgView2 = (ImageView) convertView
						.findViewById(R.id.imageView2);
				imgView2.setImageResource(R.drawable.mode_list);

				convertView.setTag(new ViewHolder(imgView, textView, imgView2));
			}

		} else {
			ViewHolder holder = (ViewHolder) convertView.getTag();

			holder.txtView.setText(getModeString(position));

		}
		return convertView;
	}

	private String getModeString(int position) {

		SignalModule module = moduleManager.getModule(position / 4);

		if ((position % 4) == 0) {
			if(module==null) return "";
			DevInfo info=module.getDevInfo();
			if(info==null) return "";
			return module.getDevInfo().getName();
		}

		StringBuffer sb = new StringBuffer();
		sb.append("通道" + ((position % 4) - 1) + ":   ");
		WorkMode wkMode = null;
		if (module != null) {
			SignalChannel channel = module.getChannel((position % 4) - 1);
			if (channel != null) {
				wkMode = channel.getWorkMode();
			}
		}

		if (wkMode == null) {
			sb.append("未设置");
		} else {
			sb.append(wkMode.getGearInfo());
		}

		return sb.toString();

	}

	class ViewHolder {
		ImageView img1;
		TextView txtView;
		ImageView img2;

		public ViewHolder(ImageView img1, TextView txtView, ImageView img2) {
			// TODO Auto-generated constructor stub
			this.img1 = img1;
			this.txtView = txtView;
			this.img2 = img2;
		}

	}

}