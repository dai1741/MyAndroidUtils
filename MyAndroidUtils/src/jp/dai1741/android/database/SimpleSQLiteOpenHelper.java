package jp.dai1741.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * シンプルな{@code SQLiteOpenHelper}。
 * db作成用SQL文字列とリスナーのみ受け付ける。
 * 
 */
public class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {

    protected String mCreateSatement;
    protected OnCreateListener mOnCreateListener;
    protected OnUpgradeListener mOnUpgradeListener;

    public SimpleSQLiteOpenHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    public SimpleSQLiteOpenHelper(Context context, String name, CursorFactory factory,
            int version, String createStatement) {
        super(context, name, factory, version);
        mCreateSatement = createStatement;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (mCreateSatement != null) db.execSQL(mCreateSatement);
        if (mOnCreateListener != null) mOnCreateListener.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (mOnUpgradeListener != null) mOnUpgradeListener.onUpgrade(db, oldVersion,
                newVersion);
    }
    
    public void setOnCreateListener(OnCreateListener listener) {
        mOnCreateListener = listener;
    }
    
    @Deprecated
    public void setOnCreateListenre(OnCreateListener listener) {
        mOnCreateListener = listener;
    }
    
    public void setOnUpgradeListener(OnUpgradeListener listener) {
        mOnUpgradeListener = listener;
    }

    public static interface OnCreateListener {
        void onCreate(SQLiteDatabase db);
    }

    public static interface OnUpgradeListener {
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
    }

}
