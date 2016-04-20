package dennouneko.booruview;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;
import org.json.*;
import android.text.method.*;

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
		
		RelativeLayout viewHost = (RelativeLayout)findViewById(R.id.previewHost);
		TextView tagsView = (TextView)viewHost.findViewById(R.id.previewTags);
		tagsView.setMovementMethod(new ScrollingMovementMethod());
		
		Intent intent = getIntent();
		String src = intent.getStringExtra(PREVIEW_SRC);
		curServer = intent.getStringExtra(SERVER_URL);
		try {
			item = new JSONObject(intent.getStringExtra(ITEM_DETAILS));
			fullImage = curServer + item.getString("file_url");
			String tag_string_character = item.getString("tag_string_character");
			String tag_string_copyright = item.getString("tag_string_copyright");
			String tagbox = "";
			if(!tag_string_character.isEmpty()) {
				tagbox = tag_string_character;
			}
			if(!tag_string_copyright.isEmpty()) {
				tag_string_copyright = "(" + tag_string_copyright + ")";
				if(!tagbox.isEmpty()) {
					tagbox += "\n";
				}
				tagbox += tag_string_copyright;
			}
			tagsView.setText(tagbox);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		pullImage(src);
	}
	
	public void pullImage(String src) {
		ImageView img = (ImageView)findViewById(R.id.previewImageView);
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		data.loadImage(src, img, false, null); // TODO: Make the preview caching an option
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
				break;
			case R.id.menuDetails:
				try {
					msg = item.getString("tag_string");
					Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
				}
				catch(Exception e) {
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.menuCopyPage:
				try {
					StringBuilder url = new StringBuilder();
					url.append(curServer);
					url.append("/posts/");
					url.append(item.getString("id"));
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
			case R.id.menuCopyImage:
				try {
					StringBuilder url = new StringBuilder();
					url.append(curServer);
					url.append(item.getString("file_url"));
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
			default:
				return super.onOptionsItemSelected(menuItem);
		}
		return true;
	}
}
