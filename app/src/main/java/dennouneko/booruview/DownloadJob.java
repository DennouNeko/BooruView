package dennouneko.booruview;

import android.os.*;
import android.util.*;
import java.net.*;
import java.io.*;

public class DownloadJob extends AsyncTask<String, Void, Object>
{
	private static final String DEBUG_TAG = "DownloadJob";
	private int responseCode = -1;
	private HttpURLConnection conn = null;
	DataHandler handler;
	DataCallback callback;

	public DownloadJob(DataHandler h, DataCallback c) {
		handler = h;
		callback = c;
	}

	protected Object doInBackground(String ... srcs) {
		Object ecode = "";
		try {
			URL src = new URL(srcs[0]);
			conn = (HttpURLConnection)src.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			responseCode = conn.getResponseCode();
			Log.d(DEBUG_TAG, "The response is: " + responseCode);
			if(responseCode >= 400 && responseCode <= 599)
			{
				ecode = handler.process(conn.getErrorStream());
			}
			else
			{
				ecode = handler.process(conn.getInputStream());
			}
		}
		catch(Exception e)
		{
			responseCode = -2;
			e.printStackTrace();
			ecode = Log.getStackTraceString(e) + "\n" + e.getMessage();
		}
		finally {
			if(conn != null) {
				conn = null;
			}
		}
		return ecode;
	}

	protected void onPostExecute(Object in) {
		if(responseCode < 200 || responseCode >= 300) {
			callback.onError(in, responseCode);
		}
		else {
			callback.onDataReady(in);
		}
	}

	protected void onCancelled() {
		if(conn != null) {
			conn.disconnect();
		}
		super.onCancelled();
	}
	
	public static abstract class DataCallback {
		// TODO: add params: header and content
		public void onDataReady(Object in) {}
		public void onError(Object in, int code) {}
	}
	
	public static abstract class DataHandler {
		public Object process(InputStream in) {return in;}
	}
}
