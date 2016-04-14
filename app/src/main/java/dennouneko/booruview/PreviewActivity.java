package dennouneko.booruview;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import org.json.*;

public class PreviewActivity extends Activity
{
	final static public String PREVIEW_SRC="src.preview";
	final static public String ITEM_DETAILS="json.details";
	final static public String SERVER_URL="url.server";
	
	String fullImage;
	String curServer;
	JSONObject item;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		Intent intent = getIntent();
		String src = intent.getStringExtra(PREVIEW_SRC);
		curServer = intent.getStringExtra(SERVER_URL);
		try {
			item = new JSONObject(intent.getStringExtra(ITEM_DETAILS));
			fullImage = curServer + item.getString("large_file_url");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		pullImage(src);
	}
	
	public void pullImage(String src) {
		ImageView img = (ImageView)findViewById(R.id.previewImageView);
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		data.loadImage(src, img);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.preview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem)
	{
		String msg;
		switch(menuItem.getItemId()) {
			case R.id.menuSave:
				String msgSaving = getResources().getString(R.string.msgSaving);
				msg = String.format(msgSaving, fullImage);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				DataProvider data = DataProvider.getInstance(getApplicationContext());
				data.downloadFile(fullImage, null);
				return true;
			case R.id.menuDetails:
				try {
					msg = item.getString("tag_string");
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
				catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				return true;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}
}
