package com.example.codedefuse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CodeDefuseDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_SCORES = "scores";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SCORES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "score INTEGER, " +
                "result TEXT, " +
                "remaining_time INTEGER, " +
                "mistake_count INTEGER, " +
                "solved_modules INTEGER, " +
                "total_modules INTEGER DEFAULT 4, " +
                "date TEXT)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_SCORES + " ADD COLUMN total_modules INTEGER DEFAULT 4");
        }
    }

    public void insertScore(String username, int score, String result, int remainingTime,
                            int mistakeCount, int solvedModuleCount, int totalModuleCount, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("username", username);
        values.put("score", score);
        values.put("result", result);
        values.put("remaining_time", remainingTime);
        values.put("mistake_count", mistakeCount);
        values.put("solved_modules", solvedModuleCount);
        values.put("total_modules", totalModuleCount);
        values.put("date", date);

        db.insert(TABLE_SCORES, null, values);
    }

    public ArrayList<Score> getAllScores() {
        ArrayList<Score> scoreList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCORES + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Score score = new Score(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("score")),
                        cursor.getString(cursor.getColumnIndexOrThrow("result")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("remaining_time")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("mistake_count")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("solved_modules")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("total_modules")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                );

                scoreList.add(score);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return scoreList;
    }

    public void clearScores() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SCORES, null, null);
    }
}
