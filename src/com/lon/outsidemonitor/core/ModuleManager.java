package com.lon.outsidemonitor.core;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

	private static ModuleManager manager = null;

	List<SignalModule> listModule = new ArrayList<SignalModule>();

	List<ModuleInfo> listModuleInfo = new ArrayList<ModuleInfo>();

	private ModuleManager() {
	}

	public static ModuleManager getInstance() {
		if (manager == null) {
			manager = new ModuleManager();
		}
		return manager;
	}

	public void reset() {
		for (int i = 0; i < listModule.size(); i++) {
			listModule.get(i).stop();
		}
		listModule.clear();
		listModuleInfo.clear();
		SerialPortManager.getInstance().reset();

		for (int i = 0; i < 10; i++) // 最大10个模块
		{

			SignalModule module = new SignalModule(i);
			if (module.isValid() == false)
				break;
			module.run();
			listModule.add(module);
			ModuleInfo info = new ModuleInfo(module, -1, listModuleInfo.size());
			listModuleInfo.add(info);
			info = new ModuleInfo(module, 0, listModuleInfo.size());
			listModuleInfo.add(info);
			info = new ModuleInfo(module, 1, listModuleInfo.size());
			listModuleInfo.add(info);
			info = new ModuleInfo(module, 2, listModuleInfo.size());
			listModuleInfo.add(info);

		}

	}
	
	public List<ModuleInfo> getModuleInfos()
	{
		return listModuleInfo;
	}
	
	public SignalModule getModule(int index)
	{
		if(listModule.size()>index)
		{
			return listModule.get(index);
		}
		return null;
	}
	
	public int getModuleNum()
	{
		return listModule.size();
	}
}
