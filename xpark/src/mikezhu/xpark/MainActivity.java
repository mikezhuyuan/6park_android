package mikezhu.xpark;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.*;

public class MainActivity extends Activity implements OnRefreshListener<ListView>, OnItemClickListener {
	PostListAdapter _postListAdapter;
	boolean _isLoading;
	PullToRefreshListView _postsListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		_postListAdapter = new PostListAdapter(this);
		_postsListView = (PullToRefreshListView) findViewById(R.id.postsList);
		_postsListView.setOnRefreshListener(this);
		_postsListView.setAdapter(_postListAdapter);
		_postsListView.setOnItemClickListener(this);
		
		updateList();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {				
		String url = "http://m.6park.com/index.php?act=wapnewsContent&nid=" + id;
		Intent intent = new Intent(MainActivity.this, DetailActivity.class);
		intent.putExtra("url", url);
		_postListAdapter.setVisited(position-1);
		MainActivity.this.startActivity(intent);
	}
	
	@Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
    	updateList();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		return super.onOptionsItemSelected(item);
//	}
	
	void updateList() {
		if(_isLoading) return;
		
		_isLoading = true;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get("http://m.6park.com/", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {				
				_isLoading = false;
				_postsListView.onRefreshComplete();
				
				if(statusCode != 200) return;
								
				String content = null;
				try {
					content = new String(bytes, "GB2312");
				} catch (UnsupportedEncodingException e) {
					Utils.log(e.toString());
				}

				Utils.log(content);

				Pattern pattern = Pattern.compile("<div class=\"it\">.+?nid=(\\d+).+?ttl.+?>(.+?)<.+?</div>", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(content);
				ArrayList<Post> posts = new ArrayList<Post>();				
				while (matcher.find()) {
					String id = matcher.group(1);
					String title = matcher.group(2);
					Post post = new Post();
					post.id = Integer.parseInt(id);
					post.title = title.trim();
					post.hasVisited = false;
					title = title.trim();
					posts.add(post);
				}

				int added = _postListAdapter.addPosts(posts);
				Toast.makeText(MainActivity.this, MainActivity.this.getResources().getString(R.string.news_found, added), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(Throwable error, String response) {
				_isLoading = false;
				_postsListView.onRefreshComplete();
				Utils.log(error.toString());
			}
		});
	}
}