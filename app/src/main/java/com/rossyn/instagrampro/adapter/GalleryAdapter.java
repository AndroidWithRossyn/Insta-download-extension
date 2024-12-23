package com.rossyn.instagrampro.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.rossyn.instagrampro.R;
import com.rossyn.instagrampro.database.AlbumData;
import com.rossyn.instagrampro.utils.Helper;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<AlbumData> albumDataList;

    private Map<AlbumData, Integer> activeDownloads = new HashMap<>();

    int positionTab = 0;

    public GalleryAdapter() {

    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.gallery_image_list, parent, false);
        return new GalleryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        if (!albumDataList.isEmpty()) {
            AlbumData albumData = albumDataList.get(position);

            String str = albumData.getThumbnail();

            Glide.with(holder.imageView.getContext())
                    .load(str)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.imageView);

            holder.username.setText(albumData.getUserName());
            holder.date.setText(Helper.getCurrentDateTime(albumData.getDate()));
            String path = albumData.getPath().toLowerCase();


            if (positionTab != 3) {
                holder.progressBar.setVisibility(View.GONE);
                if (Helper.endsWithAny(path, Helper.videoExtensions)) {
                    holder.mediaType.setVisibility(View.VISIBLE);
                } else {
                    holder.mediaType.setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(v -> openFile(v.getContext(), albumData.getPath()));


            } else {
                holder.mediaType.setVisibility(View.GONE);
                if (activeDownloads != null && activeDownloads.containsKey(albumData)) {
                    Integer progress = activeDownloads.get(albumData);
                    if (progress != null) {
                        holder.progressBar.setVisibility(View.VISIBLE);
                        holder.progressBar.setProgress(progress);
                    } else {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                } else {
                    holder.progressBar.setVisibility(View.GONE);
                }

            }
        }


    }

    public void openFile(Context context, String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    file
            );

            // Determine the MIME type
            String mimeType = context.getContentResolver().getType(fileUri);
            if (mimeType == null) {
                mimeType = "*/*"; // Default to any file type
            }


            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Start activity to open the file
            context.startActivity(Intent.createChooser(intent, "Open file with"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error opening file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return albumDataList != null ? albumDataList.size() : 0;
    }

    public void setAlbumDataList(List<AlbumData> albumDataList) {
        this.albumDataList = albumDataList;
    }

    public void setPositionTab(int value) {
        positionTab = value;
    }

    public void setActiveDownloads(Map<AlbumData, Integer> activeDownloads) {
        this.activeDownloads = activeDownloads;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView mediaType;
        private final TextView username;
        private final TextView date;

        private final ProgressBar progressBar;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.Gallery_Image);
            mediaType = itemView.findViewById(R.id.Gallery_Media_Type);
            username = itemView.findViewById(R.id.username);
            date = itemView.findViewById(R.id.date);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
