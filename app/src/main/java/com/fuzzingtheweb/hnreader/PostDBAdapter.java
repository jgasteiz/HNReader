package com.fuzzingtheweb.hnreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by javi on 06/07/2014.
 */
public class PostDBAdapter {

    public static final String KEY_ROWID = "_id";
    public final String KEY_INDEX = "postIndex";
    public final String KEY_POST_ID = "postId";
    public final String KEY_TITLE = "title";
    public final String KEY_URL = "url";
    public final String KEY_PRETTY_URL = "prettyUrl";
    public final String KEY_SCORE = "score";
    public final String KEY_AUTHOR = "author";
    public final String KEY_POSTED_AGO = "postedAgo";
    public final String KEY_NUM_COMMENTS = "comments";

    private static final String TAG = "PostDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table posts (_id integer primary key autoincrement, " +
                    "postIndex text not null, postId text not null, title text not null, " +
                    "url text not null, prettyUrl text not null, score text not null, " +
                    "author text not null, postedAgo text not null, comments text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "posts";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS posts");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public PostDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws android.database.SQLException if the database could be neither opened or created
     */
    public PostDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Create a new post using the given parameters.
     *
     * @param index of the post
     * @param title of the post
     * @param url of the post
     * @param prettyUrl for showing in the post list
     * @param score the post was given in HN
     * @param author of the post in HN
     * @param postedAgo relative time to when it was posted in HN
     * @param numComments number of comments
     * @return true if created, false otherwise
     */
    public long createPost(String index, String postId, String title, String url,
                           String prettyUrl, String score, String author,
                           String postedAgo, String numComments) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_INDEX, index);
        initialValues.put(KEY_POST_ID, postId);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_URL, url);
        initialValues.put(KEY_PRETTY_URL, prettyUrl);
        initialValues.put(KEY_SCORE, score);
        initialValues.put(KEY_AUTHOR, author);
        initialValues.put(KEY_POSTED_AGO, postedAgo);
        initialValues.put(KEY_NUM_COMMENTS, numComments);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the post with the given rowId
     *
     * @param rowId id of post to delete
     * @return true if deleted, false otherwise
     */
    public boolean deletePost(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Delete all posts
     *
     * @return true if deleted, false otherwise
     */
    public boolean deleteAllPosts() {
        return mDb.delete(DATABASE_TABLE, null, null) > 0;
    }

    /**
     * Return a Cursor over the list of all posts in the database
     *
     * @return Cursor over all posts
     */
    public Cursor fetchAllPosts() {
        return mDb.query(DATABASE_TABLE, new String[] {
                KEY_ROWID, KEY_INDEX, KEY_POST_ID, KEY_TITLE, KEY_URL,
                KEY_PRETTY_URL, KEY_SCORE, KEY_AUTHOR, KEY_POSTED_AGO,
                KEY_NUM_COMMENTS},
                null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchPost(long rowId) throws SQLException {

        Cursor mCursor =
                mDb.query(true, DATABASE_TABLE, new String[] {
                        KEY_ROWID, KEY_INDEX, KEY_POST_ID, KEY_TITLE, KEY_URL,
                        KEY_PRETTY_URL, KEY_SCORE, KEY_AUTHOR, KEY_POSTED_AGO,
                        KEY_NUM_COMMENTS},
                        KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
}
