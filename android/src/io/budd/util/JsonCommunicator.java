package io.budd.util;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created: 5/24/13 11:01 PM
 */
public class JsonCommunicator<T> extends AsyncTask<String, Void, List<T>> {

	private final Class<T> className;

	public JsonCommunicator(Class<T> className){
		super();
		this.className = className;
	}


	@Override
	protected List<T> doInBackground(String... params) {

		String url = "127.0.0.1";
		String reqUrlStr = url.concat("/api");
		for(String param: params) { reqUrlStr = reqUrlStr.concat('/' + param); }

		try {

			URL url1 = new URL(reqUrlStr);
			HttpURLConnection request = (HttpURLConnection) url1.openConnection();
			request.connect();
			String jsonStr = ConvertStreamToString(request.getInputStream());
			Gson gson = new Gson();
			List<T> items = new ArrayList<T>();
			JsonElement element = new JsonParser().parse(jsonStr);
			if (element.isJsonArray()){
				for (JsonElement el : element.getAsJsonArray()) {
					items.add(gson.fromJson(el, className));
				}
			} else if (element.isJsonObject()) {
				items.add(gson.fromJson(jsonStr, className));
			} else {
				Logger.getLogger(JsonCommunicator.class.toString()).severe("could not deal with json object: " +
						jsonStr);
			}
			return items;

		} catch (Exception e) {
			Logger.getLogger(JsonCommunicator.class.toString()).severe( "exception, url:" + reqUrlStr);
			e.printStackTrace();
		}
		return null;
	}


	public static String ConvertStreamToString(InputStream inputStream) throws IOException {
		if (inputStream != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];

			try {

				Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				inputStream.close();
			}
			return writer.toString();
		} else {
			return "";
		}

	}

}
