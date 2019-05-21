package com.bb.googleplaybb.domain.album;


import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

public class Entity implements Parcelable {

    private int index;
    protected long mId;
    protected String mName;
    protected String mPath;
    protected long mSize;   //byte
    protected long mDate;
    protected String mBucketId;  //Directory ID
    protected String mBucketName;  //Directory Name
    protected Uri mUri;
    protected String mMimeType;

    public long getDate() {
        return mDate;
    }

    public void setDate(long date) {
        mDate = date;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        this.mSize = size;
    }

    public String getBucketId() {
        return mBucketId;
    }

    public void setBucketId(String bucketId) {
        this.mBucketId = bucketId;
    }

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mName);
        dest.writeString(this.mPath);
        dest.writeLong(this.mSize);
        dest.writeLong(this.mDate);
        dest.writeString(this.mBucketId);
        dest.writeString(this.mBucketName);
        dest.writeString(this.mMimeType);
        dest.writeParcelable(mUri, flags);
    }

    public Entity() {
    }

    protected Entity(Parcel in) {
        this.mId = in.readLong();
        this.mName = in.readString();
        this.mPath = in.readString();
        this.mSize = in.readLong();
        this.mDate = in.readLong();
        this.mBucketId = in.readString();
        this.mBucketName = in.readString();
        this.mMimeType = in.readString();
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return mPath != null ? mPath.equals(entity.mPath) : entity.mPath == null;
    }

    @Override
    public int hashCode() {
        return mPath != null ? mPath.hashCode() : 0;
    }

    public static Entity fromCursor(Cursor data) {
        int columnIndex = data.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
        String mimeType = data.getString(columnIndex);
        if (mimeType.startsWith("video")) {
            return MediaEntity.videoFromCursor(data);
        } else {
            return ImageEntity.fromCursor(data);
        }
    }

    public static final Creator<Entity> CREATOR = new Creator<Entity>() {
        @Override
        public Entity createFromParcel(Parcel source) {
            return new Entity(source);
        }

        @Override
        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };
}