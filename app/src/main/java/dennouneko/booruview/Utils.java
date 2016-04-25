package dennouneko.booruview;

public class Utils
{
	static final String[] suff = {"B", "KB", "MB", "GB", "TB"};
	
	public static String formatSize(long s) {
		float s1 = (float)s;
		int i = 0;

		while((s1 >= 1000) && (i < suff.length - 1)) {
			i++;
			s1 /= 1024.0f;
		}

		return String.format("%.2f%s", s1, suff[i]);
	}
}
