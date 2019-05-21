package com.bb.googleplaybb.domain.album;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.MediaStore;

import java.io.File;

public abstract class MediaEntity extends Entity {

    protected long mDuration;
    protected int mRotationHint;

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mDuration);
        dest.writeInt(this.mRotationHint);
    }

    public int getRotationHint() {
        return mRotationHint;
    }

    public void setRotationHint(int rotationHint) {
        this.mRotationHint = rotationHint;
    }

    public MediaEntity() {
    }

    protected MediaEntity(Parcel in) {
        super(in);
        this.mDuration = in.readLong();
        this.mRotationHint = in.readInt();
    }

    public static MediaEntity videoFromCursor(Cursor data) {
        VideoEntity video = new VideoEntity();
        video.setId(data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
        video.setName(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
        video.setPath(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
        video.setSize(data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));
        video.setBucketId(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)));
        video.setBucketName(
                data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)));
        video.setDate(data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)));
        video.setMimeType(data.getString(data.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));
        video.setDuration(data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
        video.setUri(Uri.fromFile(new File(video.getPath())));
        video.setWidth(data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)));
        video.setHeight(data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)));
        return video;
    }

    public static AudioEntity audioFromCursor(Cursor data) {
        AudioEntity video = new AudioEntity();
        video.setId(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
        video.setName(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        video.setPath(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
        video.setSize(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
        video.setArtistId(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)));
        video.setArtist(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
        video.setAlbumId(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
        video.setAlbum(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
        video.setDate(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)));
        video.setDuration(data.getLong(data.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
        video.setMimeType(data.getString(data.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)));
        video.setUri(Uri.fromFile(new File(video.getPath())));
        return video;
    }
}
