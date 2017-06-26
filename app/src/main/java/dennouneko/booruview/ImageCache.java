package dennouneko.booruview;
import android.graphics.*;
import java.io.*;
import android.widget.*;
import android.content.*;
import android.os.*;
import java.util.*;
import android.preference.*;

public class ImageCache
{
	String mDir, mDataDir;
	Context mCtx;
	boolean tidying = false;
	
	public static final int defaultSizeLimit = 50 * 1024 * 1024;
	
	public ImageCache(Context ctx, String root_dir) {
		mDir = root_dir;
		mDataDir = mDir + "/imagecache";
		mCtx = ctx;
	}
	
	public Bitmap get(String url) {
		Bitmap bmp = null;
		try {
			String filename = url2path(url);
			File info = new File(filename);
			if(info.exists()) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				bmp = BitmapFactory.decodeFile(filename, options);
				info.setLastModified(System.currentTimeMillis());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bmp;
	}
	
	public void put1(String url, Bitmap bmp) {
		String filename = url2path(url);
		Toast.makeText(mCtx, filename, Toast.LENGTH_SHORT).show();
	}
	
	public void put(String url, Bitmap bmp) {
		String filename = url2path(url);
		FileOutputStream out = null;
		try {
			mkdir(filename);
			out = new FileOutputStream(filename);
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(mCtx, e.getMessage(), Toast.LENGTH_SHORT).show();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ArrayList<FileInfo> indexFiles(String path) {
		ArrayList<FileInfo> tmp = new ArrayList<FileInfo>();
		indexFiles(path, tmp);
		Collections.sort(tmp, new FileInfoComparator(false));
		return tmp;
	}
	
	private void indexFiles(String path, ArrayList<FileInfo> list) {
		File dir = new File(path);
		long now = System.currentTimeMillis();
		if(dir.isDirectory()) {
			for(File f : dir.listFiles()) {
				indexFiles(f.getAbsolutePath(), list);
			}
		}
		else {
			FileInfo t = new FileInfo();
			t.name = dir.getAbsolutePath();
			t.size = dir.length();
			t.time = dir.lastModified();
			t.score = (float)(now-t.time)/(1000*60*60) + (float)t.size/(1024*1024);
			list.add(t);
		}
	}
	
	public int getUsedSpace(String dirname) {
		int tmp = 0;
		File dir = new File(dirname);
		for(File sub : dir.listFiles()) {
			if(sub.isDirectory()) {
				tmp += getUsedSpace(sub.getAbsolutePath());
			}
			else
			{
				tmp += sub.length();
			}
		}
		return tmp;
	}
	
	public void cleanup() {
		purge(mDataDir);
	}
	
	public void purge(String dirname) {
		File dir = new File(dirname); 
		String[] children = dir.list();
		for (int i = 0; i < children.length; i++)
		{
			File c = new File(dir, children[i]);
			if(c.isDirectory()) purge(c.getAbsolutePath());
			c.delete();
		}
	}
	
	public void tidyPartial() {
		// TODO: delete all full images
	}
	
	public void tidy() {
		if(tidying) return;
		
		tidying = true;
		AsyncTask<String, Void, String> gc = new AsyncTask<String, Void, String>() {
			protected String doInBackground(String ... param) {
				ArrayList<FileInfo> files = null;
				StringBuilder log = new StringBuilder();
				log.append("Deleted:");
				try {
					files = indexFiles(mDataDir);
					long size = 0;
					for(int i = 0; i < files.size(); i++) {
						FileInfo fi = files.get(i);
						size += fi.size;
					}
					int sizeLimit = PreferenceManager.getDefaultSharedPreferences(mCtx).getInt(ConfigActivity.PREF_CACHE_SIZE, defaultSizeLimit);
					if(size > sizeLimit) {
						long delsize = 0;
						ArrayList<FileInfo> mark = new ArrayList<FileInfo>();
						for(int i = 0; i < files.size(); i++) {
							FileInfo fi = files.get(i);
							mark.add(fi);
							delsize += fi.size;
							if(size - delsize < sizeLimit) break;
						}
						for(int i = 0; i < mark.size(); i++) {
							try {
								FileInfo fi = mark.get(i);
								File f = new File(fi.name);
								log.append("\n");
								log.append(f.getName());
								f.delete();
							}
							catch(Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					tidying = false;
				}
				return log.toString();
			}
			
			protected void onPostExecute(String msg) {
				// Toast.makeText(mCtx, msg, Toast.LENGTH_LONG).show();
			}
		};
		
		gc.execute();
	}
	
	private void mkdir(String path) {
		File f = new File(path);
		f = new File(f.getAbsolutePath());
		f.getParentFile().mkdirs();
	}
	
	private String url2path(String url) {
		String url2 = url;
		try {
			String[] parts = url.split("://");
			url2 = parts[1];
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		String tmp = mDataDir + "/" + url2;
		return tmp;
	}
	
	public class FileInfo {
		String name;
		long size;
		long time;
		float score;
	}
	
	public class FileInfoComparator implements Comparator<FileInfo> {
		boolean asc = true;
		
		public FileInfoComparator(boolean ascending) {
			asc = ascending;
		}
		
		@Override
		public int compare(ImageCache.FileInfo p1, ImageCache.FileInfo p2) {
			int ord = asc ? 1 : -1;
			if(p1.score > p2.score) {
				return ord;
			}
			if(p1.score < p2.score) {
				return -ord;
			}
			return 0;
		}
	}
}
