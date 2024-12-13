package com.rossyn.instagrampro.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlbumDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlbumData albumdata);

    @Query("DELETE FROM album_gallery_table")
    void deleteAllMedias();

    @Query("SELECT * FROM album_gallery_table")
    LiveData<List<AlbumData>> getAllMedias();

    @Query("DELETE FROM album_gallery_table WHERE id LIKE :ID")
    void deleteSingleMedia(long ID);

    @Update(entity = AlbumData.class)
    void updateSingleAlbumData(AlbumData singleAlbumData);
}
