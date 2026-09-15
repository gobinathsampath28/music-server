package music_server.model;

import jakarta.persistence.*;
import java.util.Objects;
@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String artist;

    private String album;

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

    private long duration;

    @Column(nullable = false)
    private boolean favorite = false;

    public Song() {
    }

    public Song(String title,
                String artist,
                String album,
                String fileName,
                long duration) {

        this.title = title;
        this.artist = artist;
        this.album = album;
        this.fileName = fileName;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Song song)) {
            return false;
        }

        return id != null &&
                Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {

        return getClass().hashCode();
    }
}