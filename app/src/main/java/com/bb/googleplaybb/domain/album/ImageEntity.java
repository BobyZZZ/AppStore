package com.bb.googleplaybb.domain.album;

import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.File;

public class ImageEntity extends Entity {

    private int width;
    private int height;
    private int mOrientation;

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(this.mOrientation);
    }

    public ImageEntity() {
    }

    protected ImageEntity(Parcel in) {
        super(in);
        width = in.readInt();
        height = in.readInt();
        this.mOrientation = in.readInt();
    }

    public static final Parcelable.Creator<ImageEntity> CREATOR =
            new Parcelable.Creator<ImageEntity>() {
                @Override
                public ImageEntity createFromParcel(Parcel source) {
                    return new ImageEntity(source);
                }

                @Override
                public ImageEntity[] newArray(int size) {
                    return new ImageEntity[size];
                }
            };

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static ImageEntity fromCursor(Cursor data) {
        ImageEntity image = new ImageEntity();
        image.setId(data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
        image.setName(data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)));
        image.setPath(data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
        image.setSize(data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
        image.setBucketId(
                data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)));
        image.setBucketName(
                data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
        image.setDate(data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)));
        image.setOrientation(
                data.getInt(data.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)));
        image.setMimeType(
                data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)));
        image.setUri(Uri.fromFile(new File(image.getPath())));
        image.setWidth(data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)));
        image.setHeight(data.getInt(data.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)));
        return image;
    }
}
