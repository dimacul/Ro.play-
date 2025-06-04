package Model;

import java.util.LinkedHashSet;
import java.util.Objects;
import Model.User;

public class Playlist {
    private int id;
    private String name;
    private LinkedHashSet<Song> songs; //set, mentine ordinea, add/remove/contains in O(1)
    private User owner;

    public Playlist(int id, String name, LinkedHashSet<Song> songs, User owner) {
        this.id = id;
        this.name = name;
        this.songs = songs;
        this.owner = owner;
    }

    public Playlist(String name, LinkedHashSet<Song> songs, User owner) {
        this(0, name, songs, owner);
    }

    // Getters și Setters

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public LinkedHashSet<Song> getSongs() { return songs; }

    public void setSongs(LinkedHashSet<Song> songs) { this.songs = songs; }

    public User getOwner() { return owner; }

    public void setOwner(User owner) { this.owner = owner; }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", songs=" + songs +
                ", owner=" + owner.getUsername() +
                '}';
    }
}

