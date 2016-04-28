package dennouneko.booruview;
import android.app.*;
import android.os.*;
import java.util.*;

public class ServerEditActivity extends Activity
{
	ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	private class ServerInfo
	{
		public String name;
		public String addr;
		
		public String user;
		public String pass;
		public String akey;
	};
}
