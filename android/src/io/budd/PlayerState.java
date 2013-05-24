package io.budd;

/**
 * Created: 5/24/13 6:50 PM
 */
public class PlayerState {

	private static PlayerState instance = null;
	private boolean isMusicEnabled = false;
	private boolean isPodcastEnabled = false;
	private boolean isNewsEnabled = false;

	public static PlayerState getInstance(){
		if (instance == null) instance = new PlayerState();
		return instance;
	}

	private boolean isPlaying = false;

	private PlayerState(){

	}

	public void setIsPlaying(boolean isPlaying){ this.isPlaying = isPlaying; }
	public boolean getIsPlaying(){ return isPlaying; }

	public void setIsMusicEnabled(boolean enabled) { this.isMusicEnabled = enabled; }
	public boolean getIsMusicEnagled(){ return this.isMusicEnabled; }

	public void setIsNewsEnabled(boolean enabled) { this.isNewsEnabled = enabled; }
	public boolean getIsNewsEnagled(){ return this.isNewsEnabled; }

	public void setIsPodcastEnabled(boolean enabled) { this.isPodcastEnabled = enabled; }
	public boolean getIsPodcastEnabled(){ return this.isPodcastEnabled; }
}
