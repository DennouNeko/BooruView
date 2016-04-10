package dennouneko.booruview;
import android.graphics.*;
import java.io.*;
import android.widget.*;
import android.content.*;

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
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			bmp = BitmapFactory.decodeFile(filename, options);
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
}
