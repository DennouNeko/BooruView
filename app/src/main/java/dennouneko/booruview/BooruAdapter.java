package dennouneko.booruview;
import android.widget.*;
import android.content.*;
import org.json.*;
import android.view.*;

public class BooruAdapter extends BaseAdapter
{
	private Context mCtx;
	JSONArray imgArray;
	
	public BooruAdapter(Context ctx, JSONArray data) {
		mCtx = ctx;
		imgArray = data;
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
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView ret;
		if(convertView == null) {
			ret = new ImageView(mCtx);
			ret.setLayoutParams(new GridView.LayoutParams(85, 85));
			ret.setScaleType(ImageView.ScaleType.CENTER_CROP);
			ret.setPadding(8, 8, 8, 8);
		}
		else
		{
			ret = (ImageView)convertView;
		}
		try {
			DataProvider data = DataProvider.getInstance(mCtx.getApplicationContext());
			JSONObject item = (JSONObject)getItem(position);
			ret.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_launcher));
			data.loadImage(((BooruViewActivity)mCtx).curServer + item.getString("preview_file_url"), ret);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
}
