package io.budd;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.budd.util.JsonCommunicator;

import java.io.IOException;
import java.util.logging.Logger;

public class MainActivity extends Activity {


	private static Logger logger = Logger.getLogger(MainActivity.class.getName());

	private PlayerState playerState = null;

	private MediaPlayer player = null;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		playerState = PlayerState.getInstance();

		findViewById(R.id.player_control__skip).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				skip();
			}
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_music)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { onMusicToggle(isChecked); }
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_podcasts)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { onPodcastToggle(isChecked); }
		});

		((ToggleButton)findViewById(R.id.player_control__toggle_news)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { onNewsToggle(isChecked); }
		});

		String settings = "{\"music\":{\"on\":true,\"geners\":[\"Rock\",\"Classic\"]},\"podcasts\":{\"on\":false,\"short\":false,\"medium\":false,\"long\":false},\"news\":{\"on\":false,\"repeat\":0}}";
		String song = "{\"tracks\":[{\"id\":\"ID\",\"title\":\"Title\",\"source\":\"HTTPSOURCE\"}]}";
//		JsonCommunicator[.
		JsonElement element = new JsonParser().parse(settings);

//		startPlayingFile("http://media.blubrry.com/uxpodcast/cdn.uxpodcast.com/uxpodcast-episode-047-uxlx2013-07.mp3");

	}

	private void skip(){

	}

	private void onMusicToggle(boolean isEnabled){
		playerState.setIsMusicEnabled(isEnabled);
	}

	private void onNewsToggle(boolean isEnabled){
		playerState.setIsNewsEnabled(isEnabled);
	}

	private void onPodcastToggle(boolean isEnabled){
		playerState.setIsPodcastEnabled(isEnabled);
	}


	private void startPlayingFile(String fileUri){
		Uri myUri = Uri.parse(fileUri);

		if (this.player == null) {
			this.player = new MediaPlayer();
		}

		try {
			player.setDataSource(this, myUri);//"http://mp1.somafm.com:8032");
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.prepare(); //don't use prepareAsync for mp3 playback
		} catch (IOException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		player.start();
	}

}
