package dennouneko.booruview;
import java.util.*;

public class Tags
{
	private ArrayList<String> tagList;
	
	public Tags()
	{
		tagList = new ArrayList<String>();
	}
	
	public Tags(String tags)
	{
		this();
		for(String k : tags.split(" "))
		{
			if(!k.isEmpty())
				tagList.add(k);
		}
	}

	@Override
	public String toString()
	{
		int cnt = tagList.size();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < cnt; i++)
		{
			if(i > 0)
			{
				if(i < cnt - 1)
					sb.append(", ");
				else
					sb.append(" and ");
			}
			sb.append(tagList.get(i));
		}
		return sb.toString();
	}
	
	public boolean isEmpty()
	{
		return tagList.size() == 0;
	}
	
	public String[] getList()
	{
		String[] array = new String[tagList.size()];
		array = tagList.toArray(array);
		return array;
	}
	
	public boolean hasTag(String tag)
	{
		return tagList.contains(tag);
	}
}
