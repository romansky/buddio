package io.budd;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private PlayerState playerState = null;

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

}
