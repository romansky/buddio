package io.budd;

import java.util.List;

/**
 * Created: 5/25/13 6:00 AM
 */
public class Tracks {

	public List<Track> tracks;

	public class Track {
		public String id;
		public String title;
		public long length;
		public String url;
	}

}
