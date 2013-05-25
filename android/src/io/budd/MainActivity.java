package io.budd;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.budd.util.JsonCommunicator;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends Activity implements PlayerState.OnConfigChangeListener {


	private static Logger logger = Logger.getLogger(MainActivity.class.getName());

	private PlayerState playerState = null;

	private MediaPlayer player = null;

	private Handler handler = new Handler();


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		findViewById(R.id.player_control__skip).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				skip();
			}
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_music)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { if (playerState != null) playerState.toggleMusic(isChecked); }
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_podcasts)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { if (playerState != null) playerState.togglePodcast(isChecked); }
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_news)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { if (playerState != null) playerState.toggleNews(isChecked);; }
		});

		initialSetup();
		startUpdatingTime();

	}

	private void startUpdatingTime(){
		handler.postDelayed(updateTimeTask,500);
	}

	private Runnable updateTimeTask = new Runnable() {

		@Override
		public void run() {
			if (player != null) {
				long totalDuration = player.getDuration();
				long currentDuration = player.getCurrentPosition();

				updatePlayingTrackStats(totalDuration, currentDuration);

			}

			handler.postDelayed(this, 500);
		}
	};

	private void initialSetup() {
		new JsonCommunicator<Configuration>(Configuration.class) {
			@Override
			protected void onPostExecute(List<Configuration> fullItems) {
				Configuration config = fullItems.get(0);
				playerState = PlayerState.getInstance(config);
				playerState.setOnConfigChangeListener(MainActivity.this);
			}
		}.execute("v1","settings");
	}

	private void skip(){

	}

	private void updatePlayingTrackStats(long totalSecs, long remainingSecs){
		String totalStr = milliSecondsToTime(totalSecs);
		String remainingStr = milliSecondsToTime(remainingSecs);

		((TextView)findViewById(R.id.playing_view__time_remaining)).setText(remainingStr + "/" + totalStr);
	}

	private void startPlayingTrack(Tracks.Track track){
		startPlayingFile(track.url);
		((TextView)findViewById(R.id.playing_view__text)).setText(track.title);
	}


	private void startPlayingFile(String fileUri){
		Uri myUri = Uri.parse(fileUri);

		if (this.player == null) {
			this.player = new MediaPlayer();
		}

		try {
			player.reset();
			player.setDataSource(this, myUri);//"http://mp1.somafm.com:8032");
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.prepare(); //don't use prepareAsync for mp3 playback
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		player.start();
	}

	private void stopPlaying(){
		player.stop();
	}

	private String milliSecondsToTime(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int)( milliseconds / (1000*60*60));
		int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		// Add hours if there
		if(hours > 0){
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if(seconds < 10){
			secondsString = "0" + seconds;
		}else{
			secondsString = "" + seconds;}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

	@Override
	public void configChanged(Configuration newConfig) {
		((ToggleButton)findViewById(R.id.player_control__toggle_music)).setChecked(newConfig.music.on);
		((ToggleButton)findViewById(R.id.player_control__toggle_news)).setChecked(newConfig.news.on);
		((ToggleButton)findViewById(R.id.player_control__toggle_podcasts)).setChecked(newConfig.podcasts.on);

		new JsonCommunicator<Tracks>(Tracks.class) {
			@Override
			protected void onPostExecute(List<Tracks> tracksContainer) {
				Tracks tracks = tracksContainer.get(0);
				if (tracks.tracks != null &&  tracks.tracks.size() > 0) {
					startPlayingTrack(tracks.tracks.get(0));
				} else {
					stopPlaying();
				}
			}
		}.execute("v1","next");




	}
}
