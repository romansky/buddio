package io.budd;

import java.util.HashSet;
import java.util.Set;

/**
 * Created: 5/24/13 6:50 PM
 */
public class PlayerState {

	private static PlayerState instance = null;

	private Set<OnConfigChangeListener> listeners;

	private Configuration curConfig = null;

	public static PlayerState getInstance(Configuration config){
		if (instance == null) {
			instance = new PlayerState();
			instance.curConfig = config;
		}
		return instance;
	}

	private PlayerState() {
		this.listeners = new HashSet<OnConfigChangeListener>();
	}

	public void toggleMusic(boolean isEnabled){
		this.curConfig.music.on = isEnabled;
		this.notifyOnConfigChange();
	}

	public void toggleNews(boolean isEnabled){
		this.curConfig.news.on = isEnabled;
		this.notifyOnConfigChange();
	}

	public void togglePodcast(boolean isEnabled){
		this.curConfig.podcasts.on = isEnabled;
		this.notifyOnConfigChange();
	}

	private void notifyOnConfigChange(){
		for (OnConfigChangeListener listener : listeners) {
			listener.configChanged(this.curConfig);
		}
	}

	public void setOnConfigChangeListener(OnConfigChangeListener l){
		listeners.add(l);
		l.configChanged(curConfig);
	}

	public void removeOnConfigChangeListener(OnConfigChangeListener l){
		listeners.remove(l);
	}

	public interface OnConfigChangeListener {
		abstract void configChanged(Configuration newConfig);
	}

	public Configuration getConfiguration(){
		return this.curConfig;
	}

}
