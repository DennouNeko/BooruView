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
	
	public int getCacheUsed() {
		return cache.getUsedSpace(cacheDir);
	}
	
	public ArrayList<ImageCache.FileInfo> indexCacheFiles() {
		ArrayList<ImageCache.FileInfo> ret = null;
		try {
			ret = cache.indexFiles(cacheDir);
		}
		catch(Exception e) {
			Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return ret;
	}
	
	static public DataProvider getInstance(Context appCtx) {
		if(instance == null) {
			instance = new DataProvider(appCtx);
		}
		return instance;
	}
	
	public DownloadJob loadPage(String url, final DownloadJob.DataCallback callback) {
		DownloadJob bg = new DownloadJob(new DownloadJob.DataHandler() {
			public Object process(InputStream in) {
				return getAsString(in);
			}
		}, callback);
		bg.execute(url);
		return bg;
	}
	
	public DownloadJob loadImage(final String src, final ImageView dest, final boolean doCache, final DownloadJob.DataCallback callback) {
		Bitmap cbmp = cache.get(src);
		DownloadJob bg = null;
		if(cbmp == null) {
			dest.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_launcher));
			bg = new DownloadJob(new DownloadJob.DataHandler() {
				public Object process(InputStream in) {
					Bitmap bmp = BitmapFactory.decodeStream(in);
					return bmp;
				}
			},
			new DownloadJob.DataCallback() {
				public void onDataReady(Object in) {
					Bitmap bmp = (Bitmap)in;
					if(doCache) {
						cache.put(src, bmp);
					}
					dest.setImageBitmap(bmp);
					if(callback != null) {
						callback.onDataReady(in);
					}
				}

				public void onError(InputStream in, int code) {
					Toast.makeText(mCtx.getApplicationContext(), String.format("GET %d\n%s", code, src), Toast.LENGTH_LONG).show();
					if(callback != null) {
						callback.onError(in, code);
					}
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
}
