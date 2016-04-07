package dennouneko.booruview;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;

public class PreviewActivity extends Activity
{
	final static public String PREVIEW_SRC="preview.src";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview);
		
		Intent intent = getIntent();
		String src = intent.getStringExtra(PREVIEW_SRC);
		pullImage(src);
	}
	
	public void pullImage(String src) {
		ImageView img = (ImageView)findViewById(R.id.previewImageView);
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		data.loadImage(src, img);
	}
}
