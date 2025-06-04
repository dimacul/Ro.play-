package Model;

import Model.Enums.MusicGenre;

public abstract class Artist {
    protected int id;
    protected String name;
    protected MusicGenre genre;
    protected String contactEmail;
    protected String description;
    public Artist() {}

    public Artist(int id, String name, MusicGenre genre, String contactEmail, String description) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.contactEmail = contactEmail;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre.toString();
    }

    public String getContactEmail() {
        return contactEmail;
    }
    public String getDescription() {
        return description;
    }
    public int getId() {
        return id;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenre(MusicGenre genre) {
        this.genre = genre;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

}
