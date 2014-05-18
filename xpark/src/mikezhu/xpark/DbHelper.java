package mikezhu.xpark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DbHelper extends SQLiteOpenHelper {
	public DbHelper(Context context){
		super(context, "6park.db", null, 1);
	}	
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table posts (_id integer primary key, id integer, title text, hasVisited integer)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists posts");
		onCreate(db);
	}
	
	public ArrayList<Post> getPosts() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("select id, title, hasVisited from posts order by id desc", null);
		ArrayList<Post> result = new ArrayList<Post>(); 
		while(cursor.moveToNext()) {
			Post post = new Post();
			post.id = cursor.getInt(cursor.getColumnIndex("id"));
			post.title = cursor.getString(cursor.getColumnIndex("title"));
			post.hasVisited = cursor.getInt(cursor.getColumnIndex("hasVisited")) !=0;
			
			result.add(post);
		}
		
		Collections.sort(result, new Comparator<Post>() {
			@Override
			public int compare(Post a, Post b) {
				return b.id - a.id;
			}
		});
		return result;
	}
	
	public List<Post> addPost(List<Post> posts) {
		SQLiteDatabase db = this.getWritableDatabase();
		posts = filterExisting(posts);
		for(Post post:posts) {
			ContentValues values = new ContentValues();		
			values.put("id", post.id);
			values.put("title", post.title);
			values.put("hasVisited", post.hasVisited);
			
			db.insert("posts", null, values);
		}
		
		return posts;
	}
	
	public void setPostVisited(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("hasVisited", 1);
		db.update("posts", values, "id=?", new String[]{Integer.toString(id)});
	}
	
	ArrayList<Post> filterExisting(List<Post> posts) {
		ArrayList<Post> result = new ArrayList<Post>();
		SQLiteDatabase db = this.getReadableDatabase();
		
		for(Post post:posts) {
			Cursor cursor = db.rawQuery("select 1 from posts where id = ?", new String[]{Integer.toString(post.id)});
			if(cursor.getCount() == 0) {
				result.add(post);
			}
		}
		
		return result;
	}
}