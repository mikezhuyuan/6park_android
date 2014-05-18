package mikezhu.xpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PostListAdapter extends ArrayAdapter<Post> {
	List<Post> _posts;
	DbHelper _dbHelper;
	LayoutInflater _inflator;
	Context _context;
	
	public PostListAdapter(Context context) {
		this(context, new ArrayList<Post>());
		_dbHelper = new DbHelper(context);
		_posts.addAll(_dbHelper.getPosts());		
		_inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		_context = context;
		
	}
	
	protected PostListAdapter(Context context, ArrayList<Post> posts) {
		super(context, 0, posts);
		_posts = posts;
	}
	
	@Override
	public long getItemId(int position) {
        return getItem(position).id;
    }
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {		
		if(convertView == null){
			convertView = _inflator.inflate(android.R.layout.simple_list_item_1, parent, false);
			convertView.setTag(convertView.findViewById(android.R.id.text1));
		}
		
		TextView title = (TextView)convertView.getTag();
		
		Post post = getItem(position);
		if(post.hasVisited)
			title.setTextColor(_context.getResources().getColor(android.R.color.darker_gray));
		else
			title.setTextColor(_context.getResources().getColor(android.R.color.black));
		
		title.setText(post.title);
		
		return convertView;
	}
	
	public int addPosts(List<Post> posts) {
		List<Post> addedPosts = _dbHelper.addPost(posts);
		Collections.sort(addedPosts, new Comparator<Post>() {
			@Override
			public int compare(Post a, Post b) {
				return a.id - b.id;
			}
		});
		
		for(Post post: addedPosts) {
			_posts.add(0, post);
		}
		
		notifyDataSetChanged();
		
		return addedPosts.size();
	}
	
	public void setVisited(int position) {
		Post post = getItem(position);
		post.hasVisited = true;
		notifyDataSetChanged();
		_dbHelper.setPostVisited(post.id);
	}
}
