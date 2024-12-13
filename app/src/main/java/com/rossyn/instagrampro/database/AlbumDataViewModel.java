package com.rossyn.instagrampro.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumDataViewModel extends AndroidViewModel {

    public static AlbumDataRepository repository;
    private final LiveData<List<AlbumData>> allMedias;

    public AlbumDataViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = new AlbumDataRepository(application.getApplicationContext());
        allMedias = repository.getAllMedias();
    }

    public LiveData<List<AlbumData>> getAllMedias() {
        return allMedias;
    }

    public void insert(AlbumData albumdata) {
        repository.insert(albumdata);
    }

    public void deleteAllMedias() {
        repository.deleteAllMedias();
    }

    public void deleteSingleMedia(long id) {
        repository.deleteSingleMedia(id);
    }

    public void updateSingleAlbumData(AlbumData singleAlbumData) {
        repository.updateSingleAlbumData(singleAlbumData);
    }
}
