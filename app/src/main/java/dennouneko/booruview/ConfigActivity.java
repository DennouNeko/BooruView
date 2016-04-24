package dennouneko.booruview;
import android.os.*;
import android.preference.*;

public class ConfigActivity  extends PreferenceActivity
{
	static final String SETTING_CACHE_PREVIEW = "pref_cachePreview";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// update the config view with xml data
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
