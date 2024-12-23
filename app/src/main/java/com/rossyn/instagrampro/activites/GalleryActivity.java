package com.rossyn.instagrampro.activites;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.rossyn.instagrampro.R;
import com.rossyn.instagrampro.adapter.GalleryAdapter;
import com.rossyn.instagrampro.database.AlbumData;
import com.rossyn.instagrampro.database.AlbumDataViewModel;
import com.rossyn.instagrampro.utils.DownloadManager;
import com.rossyn.instagrampro.utils.Helper;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private AlbumDataViewModel albumDataViewModel;

    private final MutableLiveData<Integer> tabPosition = new MutableLiveData<>(0);

    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private GalleryAdapter adapter;
    int currentPositionTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gallery);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication()).create(AlbumDataViewModel.class);

        tabLayout = findViewById(R.id.items_tab);

        tabLayout.addTab(tabLayout.newTab().setText("Photos"));
        tabLayout.addTab(tabLayout.newTab().setText("Videos"));
        tabLayout.addTab(tabLayout.newTab().setText("Audios"));
        tabLayout.addTab(tabLayout.newTab().setText("Downloading"));

        recyclerView = findViewById(R.id.items_recycler);

        adapter = new GalleryAdapter();


        recyclerView.setLayoutManager(new GridLayoutManager(this, 3, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                tabPosition.postValue(tab.getPosition());
            }

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
            }
        });


        callAlbumViewModel();

        findViewById(R.id.back_Button).setOnClickListener(v -> backPress());
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                backPress();
            }
        });


        tabPosition.observe(this, position -> {
            currentPositionTab = position;
            adapter.setPositionTab(position);
            switch (position) {
                case 0:
                    adapter.setAlbumDataList(photos);
                    break;
                case 1:
                    adapter.setAlbumDataList(videos);
                    break;
                case 2:
                    adapter.setAlbumDataList(audios);
                    break;
                case 3:
                    adapter.setAlbumDataList(activeList);
                    break;
            }
            adapter.notifyDataSetChanged();

            if (adapter.getItemCount() > 0) {
                findViewById(R.id.no_items_found).setVisibility(View.GONE);
            } else {
                findViewById(R.id.no_items_found).setVisibility(View.VISIBLE);
            }

        });

    }

    private void backPress() {
        finish();
    }

    private void callAlbumViewModel() {
        albumDataViewModel.getAllMedias().observe(GalleryActivity.this, data -> {
            filterAlbumDataByType(data);
        });

        DownloadManager.getActiveDownloads().observe(this, activeDownloads -> {
            adapter.setActiveDownloads(activeDownloads);
            boolean updateRequired = false;
            activeList.clear();
            for (AlbumData album : activeDownloads.keySet()) {
                if (!activeList.contains(album)) {
                    activeList.add(album);
                }

                Integer progress = activeDownloads.get(album);
                if (progress != null && progress == 100) {
                    updateRequired = true;
                }
            }
            if (currentPositionTab == 3 && updateRequired) {
                adapter.setAlbumDataList(new ArrayList<>(activeList));
                adapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean reader = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writer = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                for version 10 only read permission required
                if (reader && (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q || writer)) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    callAlbumViewModel();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You Denied Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumDataViewModel.getAllMedias().removeObservers((LifecycleOwner) this);

    }

    List<AlbumData> photos = new ArrayList<>();
    List<AlbumData> videos = new ArrayList<>();
    List<AlbumData> audios = new ArrayList<>();
    List<AlbumData> activeList = new ArrayList<>();


    private void filterAlbumDataByType(List<AlbumData> albumDataList) {
        photos.clear();
        videos.clear();
        audios.clear();
        for (AlbumData album : albumDataList) {
            String path = album.getPath().toLowerCase();
            if (Helper.endsWithAny(path, Helper.photoExtensions)) {
                if (!photos.contains(album)) photos.add(album);
            } else if (Helper.endsWithAny(path, Helper.videoExtensions)) {
                if (!videos.contains(album)) videos.add(album);
            } else if (Helper.endsWithAny(path, Helper.audioExtensions)) {
                if (!audios.contains(album)) audios.add(album);
            }
        }

        tabPosition.postValue(currentPositionTab);
    }


}