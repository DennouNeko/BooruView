package dennouneko.booruview;
import android.widget.*;
import android.content.*;
import org.json.*;
import android.view.*;

public class BooruAdapter extends BaseAdapter
{
	private Context mCtx;
	JSONArray imgArray;
	int gridSize = 100;
	DownloadJob[] jobs = null;
	
	public BooruAdapter(Context ctx, JSONArray data) {
		mCtx = ctx;
		imgArray = data;
		jobs = new DownloadJob[data.length()];
	}
	
	public int getCount() {
		return imgArray.length();
	}
	
	public Object getItem(int p1)
	{
		try {
			return imgArray.get(p1);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public long getItemId(int p1) {
		return p1;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView ret;
		if(jobs[position] != null) {
			jobs[position].cancel(true);
			jobs[position] = null;
		}
		if(convertView == null) {
			ret = new ImageView(mCtx);
			ret.setLayoutParams(new GridView.LayoutParams(gridSize, gridSize));
			ret.setScaleType(ImageView.ScaleType.CENTER_CROP);
			//ret.setPadding(8, 8, 8, 8);
		}
		else
		{
			ret = (ImageView)convertView;
		}
		try {
			DataProvider data = DataProvider.getInstance(mCtx.getApplicationContext());
			JSONObject item = (JSONObject)getItem(position);
			jobs[position] = data.loadImage(((BooruViewActivity)mCtx).curServer + item.getString("preview_file_url"), ret, true,
			new DownloadJob.DataCallback() {
				public void onDataReady(Object in) {
					jobs[position] = null;
				}
				public void onError(Object in, int code) {
					jobs[position] = null;
				}
			});
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public void abort() {
		for(int i = 0; i < jobs.length; i++) {
			DownloadJob j = jobs[i];
			jobs[i] = null;
			if(j != null) {
				j.cancel(true);
			}
		}
	}
}
