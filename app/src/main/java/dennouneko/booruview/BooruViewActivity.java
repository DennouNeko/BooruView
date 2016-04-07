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

public class BooruViewActivity extends Activity 
{
	ViewFlipper flipper;
	float oldTouchValue;
	int panelNum = 0;
	int viewNum = 0;
	
	public String curServer = "http://safebooru.donmai.us";
	
	private void updateViewContent() {
		final DataProvider data = DataProvider.getInstance(getApplicationContext());
		// TODO: update for any child count
		View cur = flipper.getCurrentView();
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
		nextLabel.setText(String.format("Panel %d", panelNum + 1));
		curLabel.setText(String.format("Panel %d, %d/%d", panelNum, curId + 1, cnt));
		prevLabel.setText(String.format("Panel %d", panelNum - 1));
		
		final ImageView img = (ImageView)cur.findViewById(R.id.panelImage);
		img.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
		data.loadPage(curServer + "/posts/2262900.json", new DataProvider.DataCallback() {
			public void onDataReady(Object in) {
				try {
					String val = (String)in;
					final JSONObject root = new JSONObject(val);
					curLabel.append("\n" + val);
					// curLabel.append("\n" + root.getString("md5"));
					makeImageButton(img, root);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void makeImageButton(ImageView obj, JSONObject root) {
		try {
			DataProvider.getInstance(getApplicationContext()).loadImage(curServer + root.getString("preview_file_url"), obj);
			addPreview(obj, curServer + root.getString("large_file_url"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addPreview(View obj, final String src) {
		obj.setOnClickListener(new OnClickListener() {
			public void onClick(View p1) {
				try {
					Intent myIntent = new Intent(BooruViewActivity.this, PreviewActivity.class);
					myIntent.putExtra(PreviewActivity.PREVIEW_SRC, src);
					startActivity(myIntent);
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
		// remove title
		/*requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);//*/
        setContentView(R.layout.main);
		
		flipper = (ViewFlipper)findViewById(R.id.flipper);
		
		flipper.setOnTouchListener(new SwipeListener(BooruViewActivity.this) {
			public void onSwipeRight() {
				if(panelNum <= 0) {
					// TODO: check for new posts
					// and update panelNum if necessary
				}
				if(panelNum > 0) {
					panelNum--;
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
				panelNum++;
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
