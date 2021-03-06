package de.greenrobot.daoexample;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import de.greenrobot.daoexample.ShareMessage;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table SHARE_MESSAGE.
*/
public class ShareMessageDao extends AbstractDao<ShareMessage, Long> {

    public static final String TABLENAME = "SHARE_MESSAGE";

    /**
     * Properties of entity ShareMessage.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property User_id = new Property(1, Long.class, "user_id", false, "USER_ID");
        public final static Property Sender_id = new Property(2, Long.class, "sender_id", false, "SENDER_ID");
        public final static Property Message = new Property(3, String.class, "message", false, "MESSAGE");
        public final static Property Share_pic = new Property(4, String.class, "share_pic", false, "SHARE_PIC");
    };


    public ShareMessageDao(DaoConfig config) {
        super(config);
    }
    
    public ShareMessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'SHARE_MESSAGE' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'USER_ID' INTEGER," + // 1: user_id
                "'SENDER_ID' INTEGER," + // 2: sender_id
                "'MESSAGE' TEXT," + // 3: message
                "'SHARE_PIC' TEXT);"); // 4: share_pic
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'SHARE_MESSAGE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, ShareMessage entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long user_id = entity.getUser_id();
        if (user_id != null) {
            stmt.bindLong(2, user_id);
        }
 
        Long sender_id = entity.getSender_id();
        if (sender_id != null) {
            stmt.bindLong(3, sender_id);
        }
 
        String message = entity.getMessage();
        if (message != null) {
            stmt.bindString(4, message);
        }
 
        String share_pic = entity.getShare_pic();
        if (share_pic != null) {
            stmt.bindString(5, share_pic);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public ShareMessage readEntity(Cursor cursor, int offset) {
        ShareMessage entity = new ShareMessage( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // user_id
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // sender_id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // message
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // share_pic
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, ShareMessage entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUser_id(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setSender_id(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setMessage(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setShare_pic(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(ShareMessage entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(ShareMessage entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
