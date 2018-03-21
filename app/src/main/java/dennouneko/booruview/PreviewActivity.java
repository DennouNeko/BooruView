package dennouneko.booruview;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import org.json.*;
import android.text.method.*;
import android.content.res.*;
import android.preference.*;
import android.database.*;
import java.util.*;
import android.widget.AdapterView.*;
import android.util.*;
import android.net.*;
import java.text.*;

public class PreviewActivity extends Activity
{
	final static public String PREVIEW_SRC="src.preview";
	final static public String ITEM_DETAILS="json.details";
	final static public String SERVER_URL="url.server";
	
	String fullImage;
	String curServer;
	JSONObject item;
	ArrayAdapter<String> adapter;
	ArrayList<String> tags;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		tags = new ArrayList<String>();
		
		RelativeLayout viewHost = (RelativeLayout)findViewById(R.id.previewHost);
		TextView tagsView = (TextView)viewHost.findViewById(R.id.previewTags);
		tagsView.setMovementMethod(new ScrollingMovementMethod());
		
		Intent intent = getIntent();
		String src = intent.getStringExtra(PREVIEW_SRC);
		curServer = intent.getStringExtra(SERVER_URL);
		try {
			item = new JSONObject(intent.getStringExtra(ITEM_DETAILS));
			fullImage = curServer + item.getString("file_url");
			Tags tag_string_character = new Tags(item.getString("tag_string_character"));
			Tags tag_string_copyright = new Tags(item.getString("tag_string_copyright"));
			Tags tag_string_artist = new Tags(item.getString("tag_string_artist"));
			Tags tag_string_general = new Tags(item.getString("tag_string_general"));
			Tags tag_string_meta = new Tags(item.getString("tag_string_meta"));
			StringBuilder tagbox = new StringBuilder();
			if(!tag_string_character.isEmpty()) {
				for(String s : tag_string_character.getList())
					tags.add(s);
				tagbox.append(tag_string_character);
			}
			if(!tag_string_copyright.isEmpty()) {
				for(String s : tag_string_copyright.getList())
					tags.add(s);
				if(tagbox.length() > 0) tagbox.append("\n");
				tagbox.append("(" + tag_string_copyright.toString() + ")");
			}
			if(!tag_string_artist.isEmpty()) {
				for(String s : tag_string_artist.getList())
					tags.add(s);
				if(tagbox.length() > 0) tagbox.append("\n");
				tagbox.append("by " + tag_string_artist);
			}
			if(!tag_string_general.isEmpty())
			{
				for(String s : tag_string_general.getList())
					tags.add(s);
			}
			if(!tag_string_meta.isEmpty())
			{
				for(String s : tag_string_meta.getList())
					tags.add(s);
			}
			tagsView.setText(tagbox);
		}
		catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
		}
		pullImage(src);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	public void pullImage(String src) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		ImageView img = (ImageView)findViewById(R.id.previewImageView);
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		boolean doCache = pref.getBoolean(ConfigActivity.PREF_CACHE_PREVIEW, false);
		data.loadImage(src, img, doCache, new DownloadJob.DataCallback() {
			public void onError(Object in, int code) {
				String ecode = (String)in;
				Toast.makeText(getApplicationContext(), ecode, Toast.LENGTH_LONG).show();
			}
		});
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
				try {
					String dest = Environment.DIRECTORY_DOWNLOADS;
					String rating = item.getString("rating");
					/*if(!rating.equals("s"))
					{
						dest += "/NSFW";
					}//*/
					Uri srcUri = Uri.parse(fullImage);
					String filename = srcUri.getLastPathSegment();
					String msgSaving = getResources().getString(R.string.msgSaving);
					msg = String.format(msgSaving, filename, dest);
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
					DataProvider data = DataProvider.getInstance(getApplicationContext());
					data.downloadFile(fullImage, dest);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menuDetails:
				try {
					showDetails();
				}
				catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menuCopyPage:
				try {
					String url = getPageUrl();
					ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("url", url);
					clipboard.setPrimaryClip(clip);
					msg = String.format("Copied \"%s\" to clipboard.", url);
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
				catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menuCopyImage:
				try {
					String url = getImageUrl();
					ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("url", url.toString());
					clipboard.setPrimaryClip(clip);
					msg = String.format("Copied \"%s\" to clipboard.", url.toString());
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
				catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menuOpenBrowser:
				Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(getPageUrl()));
				browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(browser);
				break;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
		return true;
	}
	
	public String getPageUrl()
	{
		StringBuilder url = new StringBuilder();
		url.append(curServer);
		url.append("/posts/");
		try {
			url.append(item.getString("id"));
		}
		catch(JSONException e)
		{}
		return url.toString();
	}
	
	public String getImageUrl()
	{
		StringBuilder url = new StringBuilder();
		url.append(curServer);
		try {
			url.append(item.getString("file_url"));
		}
		catch(JSONException e)
		{}
		return url.toString();
	}
	
	private void showDetails()
	{
		//msg = item.getString("tag_string");
		//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
		Dialog dlg = new Dialog(this);
		dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dlg.setContentView(R.layout.image_details);
		dlg.setCancelable(true);
		
		StringBuilder brief = new StringBuilder();
		TextView briefView = (TextView)dlg.findViewById(R.id.imageDetailsBrief);
		
		int w = 0, h = 0, id = 0, parent = 0;
		double s = 0;
		
		try {
			id = item.getInt("id");
			if(brief.length() > 0) brief.append("\n");
			brief.append(String.format("ID: %d", id));
			String rating = item.getString("rating");
			if(rating == null) rating = "?";
			brief.append(", ").append(getRatingName(rating));
			
			if(item.has("parent_id") && !item.isNull("parent_id"))
			{
				parent = item.getInt("parent_id");
				if(brief.length() > 0) brief.append("\n");
				brief.append(String.format("Parent: %d", parent));
			}
			
			w = item.getInt("image_width");
			h = item.getInt("image_height");
			s = item.getDouble("file_size") / 1024;
			
			if(brief.length() > 0) brief.append("\n");
			brief.append(String.format("[%dx%d] %.02fkB", w, h, s));
			
			String created_at = item.getString("created_at");
			if(brief.length() > 0) brief.append("\n");
			//brief.append(created_at).append("\n");
			Date created_date = Utils.DateFromISO(created_at);
			String created = created_date != null ? new SimpleDateFormat("yyyy-MM-dd hh:mm").format(created_date) : "null";
			brief.append(created);
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		
		briefView.setText(brief.toString());
		
		ListView taglist = (ListView)dlg.findViewById(R.id.imageDetailsTagList);
		if(taglist != null) {
			if(adapter == null)
			{
				adapter = new ArrayAdapter<String>(dlg.getContext(), R.layout.image_details_item, R.id.imageItemTagLabel, tags);
			}
			taglist.setAdapter(adapter);
			taglist.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long arg3)
				{
					// TODO: Selection and Search button
					//Toast.makeText(getApplicationContext(), tags.get(position), Toast.LENGTH_LONG).show();
					Intent myIntent = new Intent(PreviewActivity.this, BooruViewActivity.class);
					myIntent.putExtra(BooruViewActivity.SEARCH_INTENT, tags.get(position));
					startActivity(myIntent);
				}
			});
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Can't get the view!", Toast.LENGTH_LONG).show();
		}
		
		dlg.show();
	}
	
	public String getRatingName(String rating)
	{
		Resources res = getResources();
		switch(rating.toLowerCase())
		{
			case "s": return res.getString(R.string.ratingS);
			case "q": return res.getString(R.string.ratingQ);
			case "e": return res.getString(R.string.ratingE);
		}
		return "unknown";
	}
}
