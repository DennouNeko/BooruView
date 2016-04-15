package dennouneko.booruview;
import android.widget.*;
import android.os.*;
import java.io.*;
import java.net.*;
import android.util.*;
import android.graphics.*;
import android.content.*;
import java.util.*;
import android.app.*;
import android.net.*;

public class DataProvider
{
	Context mCtx;
	CookieManager jar;
	ImageCache cache;
	String cacheDir;
	static DataProvider instance = null;
	
	private DataProvider(Context ctx) {
		mCtx = ctx;
		CookieHandler.setDefault(jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		cacheDir = ctx.getCacheDir().getAbsolutePath();
		cache = new ImageCache(ctx, cacheDir);
	}
	
	public void clearCache() {
		cache.purge(cacheDir);
	}
	
	static public DataProvider getInstance(Context appCtx) {
		if(instance == null) {
			instance = new DataProvider(appCtx);
		}
		return instance;
	}
	
	public DownloadJob loadPage(String url, final DataCallback callback) {
		DownloadJob bg = new DownloadJob(new DataHandler() {
			public Object process(InputStream in) {
				return getAsString(in);
			}
		}, callback);
		bg.execute(url);
		return bg;
	}
	
	public DownloadJob loadImage(final String src, final ImageView dest) {
		return loadImage(src, dest, true);
	}
	
	public DownloadJob loadImage(final String src, final ImageView dest, final boolean doCache) {
		Bitmap cbmp = cache.get(src);
		DownloadJob bg = null;
		if(cbmp == null) {
			dest.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_launcher));
			bg = new DownloadJob(new DataHandler() {
				public Object process(InputStream in) {
					Bitmap bmp = BitmapFactory.decodeStream(in);
					return bmp;
				}
			},
			new DataCallback() {
				public void onDataReady(Object in) {
					Bitmap bmp = (Bitmap)in;
					if(doCache) {
						cache.put(src, bmp);
					}
					dest.setImageBitmap(bmp);
				}

				public void onError(InputStream in, int code) {
					Toast.makeText(mCtx.getApplicationContext(), String.format("GET %d\n%s", code, src), Toast.LENGTH_LONG).show();
				}
			});
			bg.execute(src);
		}
		else
		{
			dest.setImageBitmap(cbmp);
		}
		return bg;
	}
	
	public void downloadFile(String src, String dst) {
		try {
			DownloadManager dm = (DownloadManager)mCtx.getSystemService(Context.DOWNLOAD_SERVICE);
			if(dst == null) {
				dst = Environment.DIRECTORY_DOWNLOADS;
			}
			Uri srcUri = Uri.parse(src);
			String filename = srcUri.getLastPathSegment();
			DownloadManager.Request request = new DownloadManager.Request(srcUri);
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
			request.setAllowedOverRoaming(true);
			request.setTitle(filename);
			request.setDestinationInExternalPublicDir(dst, filename);
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			dm.enqueue(request);
		}
		catch(Exception e) {
			Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
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
		public void onError(Object in, int code) {}
	}
	
	public static abstract class DataHandler {
		public Object process(InputStream in) {return in;}
	}
	
	public class DownloadJob extends AsyncTask<String, Void, Object> {
		private static final String DEBUG_TAG = "DataProvider$DownloadJob";
		private int responseCode = -1;
		private HttpURLConnection conn = null;
		DataHandler handler;
		DataCallback callback;
		
		public DownloadJob(DataHandler h, DataCallback c) {
			handler = h;
			callback = c;
		}

		protected Object doInBackground(String ... srcs) {
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
				return handler.process(conn.getInputStream());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			finally {
				if(conn != null) {
					conn = null;
				}
			}
			return null;
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
	};
}
