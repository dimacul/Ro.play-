package Model;

public class User extends Account {
    private boolean premium;
    private String descriere;
    public User (int id, String username, String email, String password, boolean premium, String descriere) {
        super(id, username, email, password);
        this.premium = premium;
        this.descriere = descriere;
    }
    public boolean isPremium() {
        return premium;
    }
    public void setPremium(boolean premium) {
        this.premium = premium;
    }
    public String getDescriere() {
        return descriere;
    }
    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

}
