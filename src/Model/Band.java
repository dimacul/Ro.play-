package Model;
import Model.Enums.MusicGenre;

import java.util.List;

public class Band extends Artist {
    private List<SoloArtist> members;

    public Band(int id, String name, MusicGenre genre, String contactEmail, List<SoloArtist> members, String description) {
        super(id, name, genre, contactEmail, description);
        this.members = members;
    }

    public List<SoloArtist> getMembers() {
        return members;
    }

    public void setMembers(List<SoloArtist> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Band:\n" +
                "name: " + name + '\'' +
                ", members:" + members +
                '\n';
    }
}
