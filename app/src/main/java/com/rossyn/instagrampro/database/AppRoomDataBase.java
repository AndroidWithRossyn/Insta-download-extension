package com.rossyn.instagrampro.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {AlbumData.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppRoomDataBase extends RoomDatabase {

    public abstract AlbumDataDao albumDataDao();
    private static volatile AppRoomDataBase INSTANCE;

    public static AppRoomDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDataBase.class) {
                INSTANCE = Room.databaseBuilder(context, AppRoomDataBase.class, "AppRoomDataBase")
                        .fallbackToDestructiveMigration().build();
            }
        }
        return INSTANCE;
    }
}
