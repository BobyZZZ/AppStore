package com.bb.googleplaybb.domain.album;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoEntity extends MediaEntity implements Parcelable {
    private int width;
    private int height;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public VideoEntity() {
    }

    public VideoEntity(Parcel in) {
        super(in);
        this.width = in.readInt();
        this.height = in.readInt();
    }

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

    public static final Creator<VideoEntity> CREATOR = new Creator<VideoEntity>() {
        @Override
        public VideoEntity createFromParcel(Parcel source) {
            return new VideoEntity(source);
        }

        @Override
        public VideoEntity[] newArray(int size) {
            return new VideoEntity[size];
        }
    };
}
