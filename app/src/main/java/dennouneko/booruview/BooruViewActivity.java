package dennouneko.booruview;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.animation.*;
import android.view.View.*;
import android.view.*;
import java.io.*;
import android.content.*;
import android.text.method.*;
import org.json.*;
import android.widget.AdapterView.*;
import java.util.*;
import android.content.res.*;
import android.preference.*;

public class BooruViewActivity extends Activity
{
	public static final int REQUEST_SERVER = 1;
	public static final String SEARCH_INTENT = "doSearch";
	
	private final Context context = this;
	ViewFlipper flipper;
	SwipeListener swipper;
	float oldTouchValue;
	// int postLimit = 15;
	int pageNum = 1;
	int viewNum = 0;
	
	DownloadJob runningJob = null;
	BooruAdapter curAdapter = null;
	String searchTags = "";
	
	private void updateViewContent() {
		if(runningJob != null) {
			runningJob.cancel(true);
			runningJob = null;
		}
		if(curAdapter != null) {
			curAdapter.abort();
			curAdapter = null;
		}
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		int postLimit = pref.getInt(ConfigActivity.PREF_POST_LIMIT, 15);
		String cs = pref.getString(ConfigActivity.PREF_SERVER, "");
		if(cs.isEmpty()) {
			//Toast.makeText(getApplicationContext(), "Please select server first.", Toast.LENGTH_LONG).show();
			//return;
			if(pref.getBoolean(ConfigActivity.PREF_MODE_SAFE, true))
			{
				cs = "http://safebooru.donmai.us";
			}
			else
			{
				cs = "http://danbooru.donmai.us";
			}
		}
		final String curServer = cs;
		
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		final ImageCache cache = data.cache;
		// TODO: update for any child count
		final View cur = flipper.getCurrentView();
		final int curId = flipper.getDisplayedChild();
		final int cnt = flipper.getChildCount();
		int nextId = curId + 1 >= cnt ? 0 : curId + 1;
		int prevId = curId - 1 < 0 ? cnt - 1 : curId - 1;
		View next = flipper.getChildAt(nextId);
		View prev = flipper.getChildAt(prevId);
		
		cache.tidy();
		
		String curLabelText = String.format("Loading page %d...", pageNum);
		String curSearch = "";
		
		if(!searchTags.isEmpty()) {
			curSearch = String.format("[%s]", searchTags);
			curLabelText += "\n" + curSearch;
		}
		
		final String curSearch2 = curSearch;
		
		// do the updates
		TextView nextLabel = (TextView)next.findViewById(R.id.panelLabel);
		final TextView curLabel  = (TextView)cur.findViewById(R.id.panelLabel);
		TextView prevLabel = (TextView)prev.findViewById(R.id.panelLabel);
		nextLabel.setText(String.format("Panel +1"));
		//curLabel.setText(String.format("Page %d, Panel %d/%d", pageNum, curId + 1, cnt));
		curLabel.setText(curLabelText);
		prevLabel.setText(String.format("Panel -1"));
		
		GridView gridPrev = (GridView)prev.findViewById(R.id.mainGridView);
		final GridView grid = (GridView)cur.findViewById(R.id.mainGridView);
		GridView gridNext = (GridView)next.findViewById(R.id.mainGridView);
		
		gridPrev.setAdapter(null);
		grid.setAdapter(null);
		gridNext.setAdapter(null);
		
		StringBuilder url = new StringBuilder();
		
		url.append(curServer);
		url.append(String.format("/posts.json?limit=%d&page=%d", postLimit, pageNum));
		if(!searchTags.isEmpty()) {
			String[] tags = searchTags.split(" ");
			StringBuilder b = new StringBuilder();
			for(String t : tags) {
				if(b.length() > 0) {
					b.append("+");
				}
				b.append(t);
			}
			url.append(String.format("&tags=%s", b.toString()));
		}
		
		runningJob = data.loadPage(url.toString(), new DownloadJob.DataCallback() {
			public void onDataReady(Object in) {
				runningJob = null;
				String val = (String)in;
				String label = String.format("Page %d", pageNum);
				if(!curSearch2.isEmpty()) {
					label += "\n" + curSearch2;
				}
				try {
					final JSONArray pageData = new JSONArray(val);
					
					if(pageData == null)
					{
						label += "\n<request error>\n" + val;
					}
					if(pageData.length() == 0) {
						label += "\n<no posts>";
					}
					curAdapter = new BooruAdapter(BooruViewActivity.this, pageData, curServer);
					grid.setAdapter(curAdapter);
					grid.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
							try {
								JSONObject item = pageData.getJSONObject(position);
								Intent myIntent = new Intent(BooruViewActivity.this, PreviewActivity.class);
								myIntent.putExtra(PreviewActivity.PREVIEW_SRC, curServer + item.getString("large_file_url"));
								myIntent.putExtra(PreviewActivity.ITEM_DETAILS, item.toString());
								myIntent.putExtra(PreviewActivity.SERVER_URL, curServer);
								startActivity(myIntent);
							}
							catch(Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				curLabel.setText(label);
			}
			
			public void onError(Object in, int code) {
				runningJob = null;
				String ecode = (String)in;
				boolean handled = false;
				try {
					final JSONObject reqStatus = new JSONObject(ecode);

					if(reqStatus != null)
					{
						String statName = "success";
						if(reqStatus.has(statName) && !reqStatus.getBoolean(statName))
						{
							String msg = reqStatus.getString("message");
							curLabel.setText("Request failed:\n" + msg);
							handled = true;
						}
					}
				}
				catch (JSONException e1)
				{
					e1.printStackTrace();
				}
				if(!handled)
				{
					curLabel.setText(String.format("Error: %d\n%s", code, ecode));
				}
			}
		});
	}
	
	private Animation.AnimationListener onAnimationEnd() {
		return new Animation.AnimationListener() {
			public void onAnimationStart(Animation ani) {}
			public void onAnimationRepeat(Animation ani) {}
			public void onAnimationEnd(Animation ani) {
				updateViewContent();
			}
		};
	}
	
	private SwipeListener makeSwipeListener(Context ctx) {
		return new SwipeListener(ctx) {
			public void onSwipeRight() {
				doPrevPage();
				super.onSwipeRight();
			}

			public void onSwipeLeft() {
				doNextPage();
				super.onSwipeLeft();
			}

			public void onSwipeCancel() {
				final View currentView = flipper.getCurrentView();
				currentView.layout(0, 
								   currentView.getTop(), currentView.getRight(), 
								   currentView.getBottom());
				super.onSwipeCancel();
			}

			public void onFingerMove(float x1, float y1, float x2, float y2) {
				final View currentView = flipper.getCurrentView();
				currentView.layout((int)(x2 - x1), 
								   currentView.getTop(), currentView.getRight(), 
								   currentView.getBottom());
				super.onFingerMove(x1, y1, x2, y2);
			}
		};
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		swipper = makeSwipeListener(BooruViewActivity.this);
		flipper = (ViewFlipper)findViewById(R.id.flipper);
		
		for(int i = 0; i < flipper.getChildCount(); i++) {
			View v = flipper.getChildAt(i);
			TextView t = (TextView)v.findViewById(R.id.panelLabel);
			t.setMovementMethod(new ScrollingMovementMethod());
		}
		
		Intent intent = getIntent();
		if(intent.hasExtra(SEARCH_INTENT))
		{
			searchTags = intent.getStringExtra(SEARCH_INTENT);
		}

		updateViewContent();
    }

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		setIntent(intent);
		if(intent.hasExtra(SEARCH_INTENT))
		{
			String search = intent.getStringExtra(SEARCH_INTENT);
			Toast.makeText(getApplicationContext(), "Searching for " + search, Toast.LENGTH_LONG).show();
			doSearch(search);
		}
	}

	@Override
	public void onBackPressed()
	{
		if(!searchTags.isEmpty())
		{
			doSearch("");
		}
		else
		{
			new AlertDialog.Builder(this)
				.setTitle("Really?")
				.setMessage("Are you sure you want to exit?")
				.setNegativeButton("No", null)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						BooruViewActivity.super.onBackPressed();
					}
				})
				.create().show();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	public void doPrevPage() {
		if(pageNum <= 1) {
			updateViewContent();
		}
		else
		{
			pageNum--;
			Animation in = AnimationHelper.inFromLeftAnimation();
			Animation out = AnimationHelper.outToRightAnimation();
			in.setAnimationListener(onAnimationEnd());
			flipper.setInAnimation(in);
			flipper.setOutAnimation(out);
			flipper.showPrevious();
		}
	}
	
	public void doNextPage() {
		pageNum++;
		Animation in = AnimationHelper.inFromRightAnimation();
		Animation out = AnimationHelper.outToLeftAnimation();
		in.setAnimationListener(onAnimationEnd());
		flipper.setInAnimation(in);
		flipper.setOutAnimation(out);
		flipper.showNext();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	public void doSearch(String tags) {
		searchTags = tags;
		pageNum = 1;
		updateViewContent();
	}
	
	public void doSettings() {
		Intent config = new Intent(this, ConfigActivity.class);
		startActivity(config);
	}
	
	public void showSearchDialog() {
		LayoutInflater li = LayoutInflater.from(context);
		View alertLayout = li.inflate(R.layout.alert_search, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(BooruViewActivity.this);
		TextView textPrompt = (TextView)alertLayout.findViewById(R.id.alertTextPrompt);
		textPrompt.setText(R.string.alertTagSearch);
		alert.setView(alertLayout);
		
		final EditText alertValue = (EditText)alertLayout.findViewById(R.id.alertTextValue);
		alertValue.setText(searchTags);
		
		alert
			.setCancelable(false)
			.setPositiveButton("Search",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					doSearch(alertValue.getText().toString());
				}
			})
			.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			
			alert.create().show();
	}
	
	public void showPageDialog() {
		LayoutInflater li = LayoutInflater.from(context);
		View alertLayout = li.inflate(R.layout.alert_page_jump, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(BooruViewActivity.this);
		TextView textPrompt = (TextView)alertLayout.findViewById(R.id.alertTextPrompt);
		textPrompt.setText(R.string.alertJumpPage);
		alert.setView(alertLayout);
		
		final NumberPicker n1 = (NumberPicker)alertLayout.findViewById(R.id.alertNumber1);
		final NumberPicker n2 = (NumberPicker)alertLayout.findViewById(R.id.alertNumber2);
		
		n1.setMinValue(0);
		n1.setMaxValue(9999);
		n2.setMinValue(0);
		n2.setMaxValue(99);
		n2.setFormatter(new NumberPicker.Formatter(){
			public String format(int val) {
				return String.format("%02d", val);
			}
		});
		
		int nv1 = pageNum / 100;
		int nv2 = pageNum % 100;
		n1.setValue(nv1);
		n2.setValue(nv2);
		
		NumberPicker.OnValueChangeListener limiter = new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker np, int oldval, int newval) {
				int range = n2.getMaxValue() - n2.getMinValue() + 1;
				int halfrange = (n2.getMaxValue() + n2.getMinValue()) / 2;
				int diff = newval - oldval;
				if(diff > range/2) diff -= range;
				if(diff < -range/2) diff += range;
				int v1 = n1.getValue();
				int v2 = n2.getValue();
				if(np == n2) {
					if(diff > 0 && (newval - diff) < 0 && newval >= 0) {
						v1++;
						n1.setValue(v1);
					}
					else if(diff < 0 && oldval >= 0 && (oldval + diff) < 0) {
						v1--;
						n1.setValue(v1);
					}
				}
			}
		};
		
		n1.setOnValueChangedListener(limiter);
		n2.setOnValueChangedListener(limiter);
		
		limiter.onValueChange(n2, nv2, nv2);
		
		alert
			.setCancelable(false)
			.setPositiveButton("Go",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					pageNum = n1.getValue() * 100 + n2.getValue();
					if(pageNum < 1) pageNum = 1;
					updateViewContent();
				}
			})
			.setNeutralButton("Page 1",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					pageNum = 1;
					updateViewContent();
				}
			})
			.setNegativeButton("Cancel",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
		
		alert.create().show();
	}
	
	public void doServerSettings() {
		Intent intent = new Intent(this, ServerEditActivity.class);
		startActivityForResult(intent, REQUEST_SERVER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent resultData)
	{
		switch(requestCode) {
		case REQUEST_SERVER:
			Toast.makeText(getApplicationContext(), String.format("Server result: %d", resultCode), Toast.LENGTH_LONG).show();
			break;
		}
		super.onActivityResult(requestCode, resultCode, resultData);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuTagClear:
				doSearch("");
				break;
			case R.id.menuSearchPost:
				showSearchDialog();
				break;
			case R.id.menuGoToPage:
				showPageDialog();
				break;
			case R.id.menuPreferences:
				doSettings();
				break;
			case R.id.menuServer:
				doServerSettings();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		swipper.onTouch(flipper, ev);
		return super.dispatchTouchEvent(ev);
	}
}
