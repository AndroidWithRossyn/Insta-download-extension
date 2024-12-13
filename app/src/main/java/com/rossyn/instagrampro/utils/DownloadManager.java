package com.rossyn.instagrampro.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.rossyn.instagrampro.database.AlbumData;
import com.rossyn.instagrampro.database.AlbumDataRepository;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DownloadManager {

    public static AlbumDataRepository repository;

    private static final MutableLiveData<Map<AlbumData, Integer>> activeDownloads = new MutableLiveData<>(new HashMap<>());

    public static LiveData<Map<AlbumData, Integer>> getActiveDownloads() {
        return activeDownloads;
    }


    public DownloadManager(Context context) {
        repository = new AlbumDataRepository(context.getApplicationContext());
    }

    public long startDownload(Context context, String url, String downloadPath, AlbumData albumData) {

        if (context == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(downloadPath)) {
            Log.e("DownloadHelper", "Invalid download parameters");
            return -1;
        }

        if (!isInternetAvailable(context)) {
            return -1;
        }

        String fileName = getFilenameFromURL(url);
        String absolutePath = downloadPath + "/" + fileName;
        albumData.setPath(absolutePath);


        Map<AlbumData, Integer> currentDownloads = new HashMap<>(activeDownloads.getValue());
        currentDownloads.put(albumData, 0);
        activeDownloads.postValue(currentDownloads);


        long downloadId = PRDownloader.download(url, downloadPath, fileName).build().setOnStartOrResumeListener(() -> {
            Log.d("DownloadHelper", "Download started/resumed: " + fileName);

            Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show();
        }).setOnPauseListener(() -> {
            Toast.makeText(context, "Download Paused", Toast.LENGTH_SHORT).show();

            Log.d("DownloadHelper", "Download paused: " + fileName);

        }).setOnCancelListener(() -> {
            Toast.makeText(context, "Download Cancelled", Toast.LENGTH_SHORT).show();

            Log.d("DownloadHelper", "Download cancelled: " + fileName);

            Map<AlbumData, Integer> updatedDownloads = new HashMap<>(activeDownloads.getValue());
            updatedDownloads.remove(albumData);
            activeDownloads.postValue(updatedDownloads);

        }).setOnProgressListener((Progress progress) -> {
            int progressPercent = (int) (progress.currentBytes * 100 / progress.totalBytes);
            Map<AlbumData, Integer> updatedDownloads = new HashMap<>(activeDownloads.getValue());
            updatedDownloads.put(albumData, progressPercent);
            activeDownloads.postValue(updatedDownloads);

        }).start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show();
                Map<AlbumData, Integer> updatedDownloads = new HashMap<>(activeDownloads.getValue());
                updatedDownloads.remove(albumData);
                activeDownloads.postValue(updatedDownloads);

                MediaScannerConnection.scanFile(context, new String[]{absolutePath}, null,
                        (path, uri1) -> {
                        });

                repository.insert(albumData);
            }

            @Override
            public void onError(com.downloader.Error error) {
                Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
                Map<AlbumData, Integer> updatedDownloads = new HashMap<>(activeDownloads.getValue());
                updatedDownloads.remove(albumData);
                activeDownloads.postValue(updatedDownloads);
            }

        });

        return downloadId;
    }


    public boolean pauseDownload(int downloadId) {
        if (downloadId != -1) {
            try {
                PRDownloader.pause(downloadId);
                Log.d("DownloadHelper", "Download paused: " + downloadId);
                return true;
            } catch (Exception e) {
                Log.e("DownloadHelper", "Error pausing download", e);
                return false;
            }
        }
        return false;
    }

    public boolean resumeDownload(int downloadId) {
        if (downloadId != -1) {
            try {
                PRDownloader.resume(downloadId);
                Log.d("DownloadHelper", "Download resumed: " + downloadId);
                return true;
            } catch (Exception e) {
                Log.e("DownloadHelper", "Error resuming download", e);
                return false;
            }
        }
        return false;
    }

    public boolean cancelDownload(int downloadId) {
        if (downloadId != -1) {
            try {
                PRDownloader.cancel(downloadId);
                Log.d("DownloadHelper", "Download cancelled: " + downloadId);
                return true;
            } catch (Exception e) {
                Log.e("DownloadHelper", "Error cancelling download", e);
                return false;
            }
        }
        return false;
    }

    public String getFilenameFromURL(String url) {
        String timestamp = getCurrentTimestamp();
        try {
            String fileName = new File(new URI(url).getPath()).getName();

            if (url.contains(".webp")) {
                return fileName.replace(".webp", "") + "_" + timestamp + ".jpg";
            } else if (url.contains(".mp4")) {
                return fileName.replace(".mp4", "") + "_" + timestamp + ".mp4";
            } else {
                return timestamp + "_" + fileName;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();

            if (url.contains(".jpg") || url.contains(".webp")) {
                return "file_" + timestamp + ".jpg";
            } else if (url.contains(".mp4")) {
                return "file_" + timestamp + ".mp4";
            } else {
                return "file_" + timestamp + ".m4a";

            }
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities activeNetwork = connectivityManager.getNetworkCapabilities(network);
            return activeNetwork != null && ((activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN)));
        } else {
            NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
            if (nwInfo == null) {
                return false;
            } else {
                return nwInfo.isConnected();
            }
        }
    }


}
