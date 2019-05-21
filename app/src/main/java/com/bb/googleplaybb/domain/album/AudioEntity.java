package com.bb.googleplaybb.domain.album;

import android.net.Uri;
import android.os.Parcel;

public class AudioEntity extends MediaEntity {

    private String mArtist;
    private long mArtistId;
    private Uri mCoverUri;
    private String mAlbum;
    private long mAlbumId;

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(long artistId) {
        mArtistId = artistId;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    public Uri getCoverUri() {
        return mCoverUri;
    }

    public void setCoverUri(Uri coverUri) {
        mCoverUri = coverUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mArtist);
        dest.writeLong(this.mArtistId);
        dest.writeParcelable(this.mCoverUri, flags);
        dest.writeString(this.mAlbum);
        dest.writeLong(this.mAlbumId);
    }

    public AudioEntity() {
    }

    protected AudioEntity(Parcel in) {
        super(in);
        this.mArtist = in.readString();
        this.mArtistId = in.readLong();
        this.mCoverUri = in.readParcelable(Uri.class.getClassLoader());
        this.mAlbum = in.readString();
        this.mAlbumId = in.readLong();
    }

    public static final Creator<AudioEntity> CREATOR =
            new Creator<AudioEntity>() {
                @Override
                public AudioEntity createFromParcel(Parcel source) {
                    return new AudioEntity(source);
                }

                @Override
                public AudioEntity[] newArray(int size) {
                    return new AudioEntity[size];
                }
            };
}
