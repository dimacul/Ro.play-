package Model;

import java.util.Objects;

public class Song implements Comparable<Song>{
    private int id;
    private String title;
    private Artist artist;
    private String album;
    private String path;

    public Song() {
    }

    public Song(int id, String title, Artist artist, String album, String path) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id: " + id +
                "\n Title: '" + title +
                "\n Artist: " + artist.getName() +
                "\n Album:" + album +
                "\n gen: " + artist.getGenre() +
                "\n path: '" + path + '\n';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, artist, album, path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song other = (Song) o;
        return id == other.id
                && Objects.equals(title, other.title)
                && Objects.equals(artist, other.artist)
                && Objects.equals(album, other.album)
                && Objects.equals(path, other.path);
    }

    @Override
    public int compareTo(Song other) {
        return this.title.compareToIgnoreCase(other.title);
    }

}

