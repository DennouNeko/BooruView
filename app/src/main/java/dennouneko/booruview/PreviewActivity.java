package dennouneko.booruview;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.view.*;

public class PreviewActivity extends Activity
{
	final static public String PREVIEW_SRC="src.preview";
	final static public String FULL_SRC="src.full";
	
	String fullImage;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		Intent intent = getIntent();
		String src = intent.getStringExtra(PREVIEW_SRC);
		fullImage = intent.getStringExtra(FULL_SRC);
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
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.menuSave:
				String msgSaving = getResources().getString(R.string.msgSaving);
				String msg = String.format(msgSaving, fullImage);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
