package com.rossyn.instagrampro.activites;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.downloader.Error;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rossyn.instagrampro.R;
import com.rossyn.instagrampro.database.AlbumData;
import com.rossyn.instagrampro.database.AlbumDataViewModel;
import com.rossyn.instagrampro.utils.DownloadManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private AlbumDataViewModel albumDataViewModel;

    DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // add these two lines in Application Class to register Download Service.
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setDatabaseEnabled(true).build();
        PRDownloader.initialize(this, config);


        downloadManager = new DownloadManager(this);

        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(AlbumDataViewModel.class);

        List<AlbumData> albumDataList = loadAlbumData(this);

        Button addSingle = findViewById(R.id.insert_data);
        addSingle.setOnClickListener(v -> {
            if (albumDataList != null && !albumDataList.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(albumDataList.size());
                AlbumData randomAlbum = albumDataList.get(randomIndex);

                String url = randomAlbum.getPath();

                // modify your download path.
                String downloadDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/insta_pro";


                AlbumData albumData = new AlbumData();
                albumData.setUserId(randomAlbum.getUserId());
                albumData.setUserName(randomAlbum.getUserName());
                albumData.setThumbnail(randomAlbum.getThumbnail());
                albumData.setDate(System.currentTimeMillis());

                long downloadId = downloadManager.startDownload(this, url, downloadDir, albumData);

                // use your download id if you want.
            }

        });

        Button clearAll = findViewById(R.id.clear_data);
        clearAll.setOnClickListener(v -> albumDataViewModel.deleteAllMedias());

        Button gallery = findViewById(R.id.gallery_activity);

        gallery.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GalleryActivity.class)));


        TextView textView = findViewById(R.id.text_view);
        albumDataViewModel.getAllMedias().observe(this, data -> {
            textView.setText("database items: " + data.size());
        });
    }


    public static List<AlbumData> loadAlbumData(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("album_data.json");
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<AlbumData>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}