package Model;
import Model.Enums.MusicGenre;

public class SoloArtist extends Artist {
    private String country;
    public SoloArtist(int id, String name, MusicGenre genre, String contactEmail, String country, String description)
     {
        super(id, name, genre, contactEmail, description);
        this.country = country;
     }

    public String getCountry() {
        return country;
    }

}

