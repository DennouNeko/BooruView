package dennouneko.booruview;
import android.graphics.*;
import java.io.*;
import android.widget.*;
import android.content.*;
import android.os.*;
import java.util.*;

public class ImageCache
{
	String mDir;
	Context mCtx;
	
	// TODO: add cache index and garbage collector
	
	public ImageCache(Context ctx, String root_dir) {
		mDir = root_dir;
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
	
	public void tidy() {
		(new AsyncTask<String, Void, Void>() {
			protected Void doInBackground(String ... param) {
				return null;
			}
		}).execute();
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
		String tmp = mDir + "/" + url2;
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
