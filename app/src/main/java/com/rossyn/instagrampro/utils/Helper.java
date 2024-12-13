package com.rossyn.instagrampro.utils;

import com.rossyn.instagrampro.database.AlbumData;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Helper {

//    public static HashMap<AlbumData, Long> storeDataAfterDownloadCompletion = new HashMap<>();
//
//    public static AlbumData getKeyFromValue(long value) {
//        for (Map.Entry<AlbumData, Long> entry : storeDataAfterDownloadCompletion.entrySet()) {
//            if (entry.getValue() == value) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }


    public static List<String> photoExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".bmp", ".gif", ".webp");
    public static List<String> videoExtensions = Arrays.asList(".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv");
    public static List<String> audioExtensions = Arrays.asList(".mp3", ".wav", ".aac", ".flac", ".ogg", ".m4a");

    public static boolean endsWithAny(String path, List<String> extensions) {
        for (String extension : extensions) {
            if (path.contains(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentDateTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault());
        Date currentDate = new Date(time);
        return dateFormat.format(currentDate);
    }
}
