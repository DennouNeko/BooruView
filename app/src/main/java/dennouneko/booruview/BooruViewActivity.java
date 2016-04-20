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

public class BooruViewActivity extends Activity 
{
	private final Context context = this;
	ViewFlipper flipper;
	SwipeListener swipper;
	float oldTouchValue;
	int postLimit = 15;
	int pageNum = 1;
	int viewNum = 0;
	
	static final String[] suff = {"B", "KB", "MB", "GB", "TB"};
	
	DownloadJob runningJob = null;
	BooruAdapter curAdapter = null;
	String searchTags = "";
	
	public String curServer = "http://safebooru.donmai.us";
	
	private void updateViewContent() {
		if(runningJob != null) {
			runningJob.cancel(true);
			runningJob = null;
		}
		if(curAdapter != null) {
			curAdapter.abort();
			curAdapter = null;
		}
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		// TODO: update for any child count
		final View cur = flipper.getCurrentView();
		final int curId = flipper.getDisplayedChild();
		final int cnt = flipper.getChildCount();
		int nextId = curId + 1 >= cnt ? 0 : curId + 1;
		int prevId = curId - 1 < 0 ? cnt - 1 : curId - 1;
		View next = flipper.getChildAt(nextId);
		View prev = flipper.getChildAt(prevId);
		
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
				curLabel.setText(label);
				try {
					final JSONArray pageData = new JSONArray(val);
					curAdapter = new BooruAdapter(BooruViewActivity.this, pageData);
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
			}
			
			public void onError(Object in, int code) {
				runningJob = null;
				curLabel.setText(String.format("Error: %d", code));
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

		updateViewContent();
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
	
	public void showSearchDialog() {
		LayoutInflater li = LayoutInflater.from(context);
		View alertLayout = li.inflate(R.layout.alert_text_prompt, null);
		AlertDialog.Builder alert = new AlertDialog.Builder(BooruViewActivity.this);
		alert.setView(alertLayout);
		
		final EditText alertValue = (EditText)alertLayout.findViewById(R.id.alertTextValue);
		alertValue.setText(searchTags);
		
		alert
			.setCancelable(false)
			.setPositiveButton("Search...",
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
	
	public static String formatSize(int s) {
		float s1 = (float)s;
		int i = 0;
		
		while((s1 >= 1000) && (i < suff.length - 1)) {
			i++;
			s1 /= 1024.0f;
		}
		
		return String.format("%.2f%s", s1, suff[i]);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		DataProvider data = DataProvider.getInstance(getApplicationContext());
		switch(item.getItemId()) {
			case R.id.menuCacheInfo:
				StringBuilder tmp = new StringBuilder();
				tmp.append(String.format("Used: %s", formatSize(data.getCacheUsed())));
				Toast.makeText(getApplicationContext(), tmp.toString(), Toast.LENGTH_LONG).show();
				break;
			case R.id.menuPurgeCache:
				data.clearCache();
				String msg = getResources().getString(R.string.msgPurged);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				break;
			case R.id.menuTagClear:
				doSearch("");
				break;
			case R.id.menuSearchPost:
				showSearchDialog();
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
