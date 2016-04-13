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
	ViewFlipper flipper;
	SwipeListener swipper;
	float oldTouchValue;
	int postLimit = 15;
	int pageNum = 1;
	int viewNum = 0;
	
	public String curServer = "http://safebooru.donmai.us";
	
	private void updateViewContent() {
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		// TODO: update for any child count
		final View cur = flipper.getCurrentView();
		final int curId = flipper.getDisplayedChild();
		final int cnt = flipper.getChildCount();
		int nextId = curId + 1 >= cnt ? 0 : curId + 1;
		int prevId = curId - 1 < 0 ? cnt - 1 : curId - 1;
		View next = flipper.getChildAt(nextId);
		View prev = flipper.getChildAt(prevId);
		
		// do the updates
		TextView nextLabel = (TextView)next.findViewById(R.id.panelLabel);
		final TextView curLabel  = (TextView)cur.findViewById(R.id.panelLabel);
		TextView prevLabel = (TextView)prev.findViewById(R.id.panelLabel);
		nextLabel.setText(String.format("Page %d", pageNum + 1));
		//curLabel.setText(String.format("Page %d, Panel %d/%d", pageNum, curId + 1, cnt));
		curLabel.setText(String.format("Loading page %d...", pageNum));
		prevLabel.setText(String.format("Page %d", pageNum - 1));
		
		GridView gridPrev = (GridView)prev.findViewById(R.id.mainGridView);
		final GridView grid = (GridView)cur.findViewById(R.id.mainGridView);
		GridView gridNext = (GridView)next.findViewById(R.id.mainGridView);
		
		gridPrev.setAdapter(null);
		gridNext.setAdapter(null);
		
		data.loadPage(curServer + String.format("/posts.json?limit=%d&page=%d", postLimit, pageNum), new DataProvider.DataCallback() {
			public void onDataReady(Object in) {
				String val = (String)in;
				curLabel.setText(String.format("Page %d, Panel %d/%d", pageNum, curId + 1, cnt));
				try {
					final JSONArray pageData = new JSONArray(val);
					grid.setAdapter(new BooruAdapter(BooruViewActivity.this, pageData));
					grid.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
							try {
								JSONObject item = pageData.getJSONObject(position);
								Intent myIntent = new Intent(BooruViewActivity.this, PreviewActivity.class);
								myIntent.putExtra(PreviewActivity.PREVIEW_SRC, curServer + item.getString("large_file_url"));
								myIntent.putExtra(PreviewActivity.FULL_SRC, curServer + item.getString("file_url"));
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
		if(pageNum <= 0) {
			// TODO: check for new posts
			// and update pageNum if necessary
		}
		if(pageNum > 0) {
			pageNum--;
			Animation in = AnimationHelper.inFromLeftAnimation();
			Animation out = AnimationHelper.outToRightAnimation();
			in.setAnimationListener(onAnimationEnd());
			flipper.setInAnimation(in);
			flipper.setOutAnimation(out);
			flipper.showPrevious();
		}
		else {
			Toast.makeText(getApplicationContext(), "No new images.", Toast.LENGTH_SHORT).show();
		}
	}
	
	public void doNextPage() {
		/* if(panelNum >= bufferedImageCount - 1) {
		 // TODO: fetch new chunk of images,
		 // try to sync if data shifted?
		 // update panelNum if necessary
		 }//*/
		// if(panelNum < bufferedImageCount - 1) {
		pageNum++;
		Animation in = AnimationHelper.inFromRightAnimation();
		Animation out = AnimationHelper.outToLeftAnimation();
		in.setAnimationListener(onAnimationEnd());
		flipper.setInAnimation(in);
		flipper.setOutAnimation(out);
		flipper.showNext();
		//}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId()) {
			case R.id.menuPurgeCache:
				DataProvider data = DataProvider.getInstance(BooruViewActivity.this);
				data.clearCache();
				String msg = getResources().getString(R.string.msgPurged);
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		swipper.onTouch(flipper, ev);
		return super.dispatchTouchEvent(ev);
	}
}
