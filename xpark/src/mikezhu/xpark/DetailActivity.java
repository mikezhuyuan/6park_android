package mikezhu.xpark;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.loopj.android.image.SmartImageView;

public class DetailActivity extends Activity {
	ProgressBar _progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		getActionBar().hide();
		Intent intent = getIntent();
		String url = intent.getStringExtra("url");
		
		setContentView(R.layout.activity_detail);
		_progressBar = (ProgressBar)findViewById(R.id.progressBar);
		final LinearLayout contents = (LinearLayout)findViewById(R.id.contents);		
		
		AsyncHttpClient client = new AsyncHttpClient();
		_progressBar.setVisibility(View.VISIBLE);
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] bytes) {
				_progressBar.setVisibility(View.GONE);
				String content = null;
				try {
					content = new String(bytes, "GB2312");
				} catch (UnsupportedEncodingException e) {
					Utils.log(e.toString());
				}

				Utils.log(content);
				
				Pattern pattern = Pattern.compile("<div class=\"cnt\">(.+?)</article>", Pattern.DOTALL);
				Matcher matcher = pattern.matcher(content);
				if(!matcher.find()) return;
				
				content = matcher.group(1);
				pattern = Pattern.compile("<(.+?)>", Pattern.DOTALL);	
				matcher = pattern.matcher(content);
				int last = 0;
				boolean center = false;
				
				while(matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					String tag = matcher.group(1);
					String str = content.substring(last, start).replaceAll("^\\s+|\\s+$", "");
					
					if(str.contains("AUCNER")) break;						
					
					if(!str.isEmpty()) {
						TextView textView = new TextView(DetailActivity.this);						
						textView.setPadding(0, 0, 0, 24);
						if(center) {
							textView.setGravity(Gravity.CENTER_HORIZONTAL);
							textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
							textView.setText(str);
						} else {
							textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
							textView.setText("        " + str);
						}
						
						contents.addView(textView);
					}
					
					String src = extractSrc(tag);
					if(src != null) {
						SmartImageView imageView = new SmartImageView(DetailActivity.this);						
						imageView.setImageUrl(src);
						imageView.setPadding(0, 0, 0, 12);
						contents.addView(imageView);
					}
					
					if(tag.startsWith("center")) center = true;
					if(tag.startsWith("/center")) center = false;
					last = end;
				}				
			}

			@Override
			public void onFailure(Throwable error, String response) {
				_progressBar.setVisibility(View.GONE);
				Utils.log(error.toString());
			}
		});
	}
	
	static final Pattern RegexSrc = Pattern.compile("src=['\"](.+?)['\"]", Pattern.DOTALL);
	
	static String extractSrc(String str) {		
		Matcher matcher = RegexSrc.matcher(str);
		if(matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
