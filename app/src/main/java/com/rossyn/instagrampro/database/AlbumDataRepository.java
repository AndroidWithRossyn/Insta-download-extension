package com.rossyn.instagrampro.database;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AlbumDataRepository {

    private final AlbumDataDao albumDataDao;
    private final LiveData<List<AlbumData>> allMedia;

    public AlbumDataRepository(final Context context) {
        AppRoomDataBase db = AppRoomDataBase.getDatabase(context);
        albumDataDao = db.albumDataDao();
        allMedia = albumDataDao.getAllMedias();
    }
    private static final int NUMBER_OF_THREADS = 5;
    private final ScheduledExecutorService dataBaseWriteExecutor = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);

    public LiveData<List<AlbumData>> getAllMedias() {
        return allMedia;
    }

    public void insert(AlbumData album_data) {
        dataBaseWriteExecutor.execute(() -> {
            albumDataDao.insert(album_data);
        });
    }

    public void deleteAllMedias() {
        dataBaseWriteExecutor.execute(albumDataDao::deleteAllMedias);
    }

    public void deleteSingleMedia(long id) {
        dataBaseWriteExecutor.execute(() -> albumDataDao.deleteSingleMedia(id));
    }

    public void updateSingleAlbumData(AlbumData singleAlbumData) {
        dataBaseWriteExecutor.execute(() -> albumDataDao.updateSingleAlbumData(singleAlbumData));
    }
}
