package dennouneko.booruview;

import android.app.*;
import android.os.*;
import android.widget.*;
import android.view.animation.*;
import android.view.View.*;
import android.view.*;

public class BooruViewActivity extends Activity 
{
	ViewFlipper flipper;
	float oldTouchValue;
	int panelNum = 0;
	int viewNum = 0;
	
	private Interpolator getAnimationInterpolator() {
		return new AccelerateDecelerateInterpolator();
	}
	
	private Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(getAnimationInterpolator());
		return inFromRight;
	}
	
	private Animation outToRightAnimation() {
		Animation outToRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		outToRight.setDuration(500);
		outToRight.setInterpolator(getAnimationInterpolator());
		return outToRight;
	}
	
	private Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(getAnimationInterpolator());
		return inFromLeft;
	}
	
	private Animation outToLeftAnimation() {
		Animation outToLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		outToLeft.setDuration(500);
		outToLeft.setInterpolator(getAnimationInterpolator());
		return outToLeft;
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
	
	private void updateViewContent() {
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
		TextView curLabel  = (TextView)cur.findViewById(R.id.panelLabel);
		TextView prevLabel = (TextView)prev.findViewById(R.id.panelLabel);
		nextLabel.setText(String.format("Panel %d", panelNum + 1));
		curLabel.setText(String.format("Panel %d, %d/%d", panelNum, curId + 1, cnt));
		prevLabel.setText(String.format("Panel %d", panelNum - 1));
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		flipper = (ViewFlipper)findViewById(R.id.flipper);
		
		flipper.setOnTouchListener(new SwipeListener(BooruViewActivity.this) {
			public void onSwipeRight() {
				if(panelNum > 0) {
					panelNum--;
					Animation in = inFromLeftAnimation();
					Animation out = outToRightAnimation();
					in.setAnimationListener(onAnimationEnd());
					flipper.setInAnimation(in);
					flipper.setOutAnimation(out);
					flipper.showPrevious();
				}
				super.onSwipeRight();
			}
			
			public void onSwipeLeft() {
				panelNum++;
				Animation in = inFromRightAnimation();
				Animation out = outToLeftAnimation();
				in.setAnimationListener(onAnimationEnd());
				flipper.setInAnimation(in);
				flipper.setOutAnimation(out);
				flipper.showNext();
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
		});
		
		updateViewContent();
    }
}
