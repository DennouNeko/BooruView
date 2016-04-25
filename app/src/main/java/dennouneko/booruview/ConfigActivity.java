package dennouneko.booruview;
import android.os.*;
import android.preference.*;

public class ConfigActivity extends PreferenceActivity
{
	static final String SETTING_CACHE_PREVIEW = "pref_cachePreview";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// update the config view with xml data
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		StringBuilder info = new StringBuilder();
		ImageCache cache = DataProvider.getInstance(getApplicationContext()).cache;
		info.append(String.format("Used: %s", BooruViewActivity.formatSize(cache.getUsedSpace(cache.mDir))));
		findPreference("pref_cacheInfo").setSummary(info.toString());
	}
}
