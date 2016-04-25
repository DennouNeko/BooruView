package dennouneko.booruview;
import android.os.*;
import android.preference.*;
import android.widget.*;

public class ConfigActivity extends PreferenceActivity
{
	static final String SETTING_CACHE_PREVIEW = "pref_cachePreview";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// update the config view with xml data
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		findPreference("pref_purgeBtn").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference p1) {
				DataProvider.getInstance(getApplicationContext()).clearCache();
				String msg = getResources().getString(R.string.msgPurged);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				updateCacheInfo();
				return true;
			}
		});
		
		updateCacheInfo();
	}
	
	private void updateCacheInfo() {
		StringBuilder info = new StringBuilder();
		ImageCache cache = DataProvider.getInstance(getApplicationContext()).cache;
		info.append(String.format("Used: %s", Utils.formatSize(cache.getUsedSpace(cache.mDir))));
		findPreference("pref_cacheInfo").setSummary(info.toString());
	}
}
