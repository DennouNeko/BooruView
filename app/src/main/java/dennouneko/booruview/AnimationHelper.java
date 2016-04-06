package dennouneko.booruview;
import android.view.animation.*;

public class AnimationHelper
{
	public static Interpolator getAnimationInterpolator() {
		return new AccelerateDecelerateInterpolator();
	}

	public static Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		inFromRight.setDuration(500);
		inFromRight.setInterpolator(getAnimationInterpolator());
		return inFromRight;
	}

	public static Animation outToRightAnimation() {
		Animation outToRight = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		outToRight.setDuration(500);
		outToRight.setInterpolator(getAnimationInterpolator());
		return outToRight;
	}

	public static Animation inFromLeftAnimation() {
		Animation inFromLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		inFromLeft.setDuration(500);
		inFromLeft.setInterpolator(getAnimationInterpolator());
		return inFromLeft;
	}

	public static Animation outToLeftAnimation() {
		Animation outToLeft = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
			Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f
		);
		outToLeft.setDuration(500);
		outToLeft.setInterpolator(getAnimationInterpolator());
		return outToLeft;
	}
}
