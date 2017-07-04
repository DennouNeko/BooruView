package dennouneko.booruview;
import java.sql.*;
import java.util.Date;
import java.text.*;

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
	
	public static Date DateFromISO(String str)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date ret = null;
		try
		{
			ret = df.parse(str);
		}
		catch(ParseException ex)
		{
		}
		return ret;
	}
}
