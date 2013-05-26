package io.budd;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.budd.util.JsonCommunicator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class MainActivity extends Activity implements PlayerState.OnConfigChangeListener {


	private static Logger logger = Logger.getLogger(MainActivity.class.getName());

	private PlayerState playerState = null;

	private MediaPlayer player = null;

	private Handler handler = new Handler();

	private LinkedList<Tracks.Track> remainingTracks;

	private Tracks.Track currentlyPlaying;


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
				long totalDuration = (player.isPlaying())? player.getDuration() : 0 ;
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
		playNextTrack();
	}

	private void updatePlayingTrackStats(long totalMiliSecs, long remainingMiliSecs){
		String totalStr = milliSecondsToTime((totalMiliSecs == 0) ? currentlyPlaying.length * 1000 : totalMiliSecs);
		String remainingStr = milliSecondsToTime(remainingMiliSecs);

		((TextView)findViewById(R.id.playing_view__time_remaining)).setText(remainingStr + "/" + totalStr);
	}


	private void playNextTrack(){
		if (remainingTracks != null && remainingTracks.size() > 0){
			Tracks.Track nextTrack = remainingTracks.pollFirst();
			startPlayingTrack(nextTrack);

			if (remainingTracks.size() > 0) {
				Tracks.Track futureTrack = remainingTracks.peek();
				((TextView)findViewById(R.id.next_view__text)).setText(futureTrack.title);
			}

		} else {
			stopPlaying();
		}
	}

	private void startPlayingTrack(Tracks.Track track){
		currentlyPlaying = track;
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
			player.setDataSource(this, myUri);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) { mp.start(); }
			});
			player.prepareAsync(); //don't use prepareAsync for mp3 playback
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopPlaying(){
		if (player != null) player.stop();
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

		Gson gson = new Gson();
		String jsonStr = gson.toJson(playerState.getConfiguration());


		final HttpPut httpPut = new HttpPut("http://192.168.43.80:9000/v1/settings");
//		httpPut.
		StringEntity entity = null;
		try {
			entity = new StringEntity(jsonStr);
			entity.setContentType("application/json;charset=UTF-8");//text/plain;charset=UTF-8
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
			httpPut.setEntity(entity);

			final DefaultHttpClient httpClient = new DefaultHttpClient();


			new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... params) {
					HttpResponse response = null;
					try {
						response = httpClient.execute(httpPut);
					} catch (IOException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
					HttpEntity entity1 = response.getEntity();

					new JsonCommunicator<Tracks>(Tracks.class) {
						@Override
						protected void onPostExecute(List<Tracks> tracksContainer) {
							Tracks tracks = tracksContainer.get(0);
							if (tracks.tracks != null &&  tracks.tracks.size() > 0) {
								remainingTracks = new LinkedList<Tracks.Track>(tracks.tracks);
								playNextTrack();
							} else {
								remainingTracks = new LinkedList<Tracks.Track>();
								stopPlaying();
							}
						}
					}.execute("v1", "next");
					return null;
				}
			}.execute();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
