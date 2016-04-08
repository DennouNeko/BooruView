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
	float oldTouchValue;
	int postLimit = 15;
	int pageNum = 1;
	int viewNum = 0;
	
	public String curServer = "http://safebooru.donmai.us";
	
	private void updateViewContent() {
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		// TODO: update for any child count
		final View cur = flipper.getCurrentView();
		int curId = flipper.getDisplayedChild();
		int cnt = flipper.getChildCount();
		int nextId = curId + 1 >= cnt ? 0 : curId + 1;
		int prevId = curId - 1 < 0 ? cnt - 1 : curId - 1;
		View next = flipper.getChildAt(nextId);
		View prev = flipper.getChildAt(prevId);
		
		// do the updates
		TextView nextLabel = (TextView)next.findViewById(R.id.panelLabel);
		final TextView curLabel  = (TextView)cur.findViewById(R.id.panelLabel);
		TextView prevLabel = (TextView)prev.findViewById(R.id.panelLabel);
		nextLabel.setText(String.format("Page %d", pageNum + 1));
		curLabel.setText(String.format("Page %d, Panel %d/%d", pageNum, curId + 1, cnt));
		prevLabel.setText(String.format("Page %d", pageNum - 1));
		
		data.loadPage(curServer + String.format("/posts.json?limit=%d&page=%d", postLimit, pageNum), new DataProvider.DataCallback() {
			public void onDataReady(Object in) {
				String val = (String)in;
				try {
					final JSONArray pageData = new JSONArray(val);
					GridView grid = (GridView)cur.findViewById(R.id.mainGridView);
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
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		flipper = (ViewFlipper)findViewById(R.id.flipper);
		
		flipper.setOnTouchListener(new SwipeListener(BooruViewActivity.this) {
			public void onSwipeRight() {
				if(pageNum <= 0) {
					// TODO: check for new posts
					// and update panelNum if necessary
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
				super.onSwipeRight();
			}
			
			public void onSwipeLeft() {
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
				super.onSwipeLeft();
				//}
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
		});
		
		for(int i = 0; i < flipper.getChildCount(); i++) {
			View v = flipper.getChildAt(i);
			TextView t = (TextView)v.findViewById(R.id.panelLabel);
			t.setMovementMethod(new ScrollingMovementMethod());
		}

		updateViewContent();
    }
}
