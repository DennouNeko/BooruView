package dennouneko.booruview;
import android.widget.*;
import android.os.*;
import java.io.*;
import java.net.*;
import android.util.*;
import android.graphics.*;
import android.content.*;
import java.util.*;

public class DataProvider
{
	Context mCtx;
	CookieManager jar;
	
	public DataProvider(Context ctx) {
		mCtx = ctx;
		CookieHandler.setDefault(jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}
	
	private void get(String url, final DataHandler handler, final DataCallback callback) {
		AsyncTask<String, Void, Object> bg = new AsyncTask<String, Void, Object>() {
			private static final String DEBUG_TAG = "DataProvider$query$AsyncTask";
			private int responseCode = -1;

			protected Object doInBackground(String ... srcs) {
				try {
					URL src = new URL(srcs[0]);
					HttpURLConnection conn = (HttpURLConnection)src.openConnection();
					conn.setReadTimeout(10000 /* milliseconds */);
					conn.setConnectTimeout(15000 /* milliseconds */);
					conn.setRequestMethod("GET");
					conn.setDoInput(true);
					// Starts the query
					conn.connect();
					responseCode = conn.getResponseCode();
					Log.d(DEBUG_TAG, "The response is: " + responseCode);
					return handler.process(conn.getInputStream());
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(Object in) {
				/*CookieStore jar = getJar();
				List<HttpCookie> cookies = jar.getCookies();

				StringBuilder tmp = new StringBuilder();

				for(HttpCookie c : cookies) {
					if(tmp.length() > 0) tmp.append("\n");
					tmp.append(c.getName() + "=" + c.getValue());
				}

				Toast.makeText(mCtx.getApplicationContext(), tmp.toString(), Toast.LENGTH_LONG).show();
				//*/
				if(responseCode < 200 || responseCode >= 300) {
					callback.onError(in, responseCode);
				}
				else {
					callback.onDataReady(in);
				}
			}
		};

		bg.execute(url);
	}
	
	public void loadPage(String url, final DataCallback callback) {
		get(url, new DataHandler() {
			public Object process(InputStream in) {
				return getAsString(in);
			}
		}, callback);
	}
	
	public void loadImage(final String src, final ImageView dest) {
		// TODO: make it cancellable?
		// TODO: image cache (yes, here!)
		
		get(src, new DataHandler() {
			public Object process(InputStream in) {
				Bitmap bmp = BitmapFactory.decodeStream(in);
				return bmp;
			}
		}, new DataCallback() {
			public void onDataReady(Object in) {
				Bitmap bmp = (Bitmap)in;
				dest.setImageBitmap(bmp);
			//	Toast.makeText(mCtx.getApplicationContext(), "Done.", Toast.LENGTH_SHORT).show();
			}
			
			public void onError(InputStream in, int code) {
				Toast.makeText(mCtx.getApplicationContext(), String.format("GET %d\n%s", code, src), Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public static String getAsString(InputStream in) {
		StringBuilder val = new StringBuilder();
		try {
			Reader r = new InputStreamReader(in, "UTF-8");
			char buf[] = new char[1024];
			int rd;
			while((rd = r.read(buf)) >= 0) {
				val.append(buf, 0, rd);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return val.toString();
	}
	
	public CookieStore getJar() {
		return jar.getCookieStore();
	}
	
	public static abstract class DataCallback {
		// TODO: add params: header and content
		public void onDataReady(Object in) {}
		public void onError(Object in, Object code) {}
	}
	
	public static abstract class DataHandler {
		public Object process(InputStream in) {return in;}
	}
}
