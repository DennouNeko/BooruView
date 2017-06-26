package dennouneko.booruview;
import android.os.*;
import android.preference.*;
import android.widget.*;
import android.content.*;

public class ConfigActivity extends PreferenceActivity
{
	public static final String PREF_CACHE_PREVIEW = "pref_cachePreview";
	public static final String PREF_CACHE_SIZE = "pref_cacheSize";
	public static final String PREF_SERVER = "pref_currentServer";
	public static final String PREF_POST_LIMIT = "pref_postLimit";
	public static final String PREF_MODE_SAFE = "pref_modeSafe";
	
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
		findPreference("pref_cacheSizeBtn").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference p1) {
				Toast.makeText(getApplicationContext(), "TODO", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		
		updateCacheInfo();
	}
	
	private void updateCacheInfo() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		StringBuilder info = new StringBuilder();
		ImageCache cache = DataProvider.getInstance(getApplicationContext()).cache;
		info.append(String.format("Used: %s", Utils.formatSize(cache.getUsedSpace(cache.mDir))));
		findPreference("pref_cacheInfo").setSummary(info.toString());
		int cacheSize = pref.getInt(PREF_CACHE_SIZE, ImageCache.defaultSizeLimit);
		findPreference("pref_cacheSizeBtn").setSummary(Utils.formatSize(cacheSize));
	}
}
