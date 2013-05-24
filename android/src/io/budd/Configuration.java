package io.budd;

import java.util.List;
import java.util.Map;

/**
 * Created: 5/24/13 11:13 PM
 */
public class Configuration {

	public Music music;
	public News news;
	public Podcasts podcasts;

	public class Podcasts {
		public boolean on;
		public boolean p_short;
		public boolean p_medium;
		public boolean p_long;
	}

	public class News {
		public boolean on;
		public List<String> geners;
	}

	public class Music {
		public boolean on;
		public List<String> geners;
	}
}


