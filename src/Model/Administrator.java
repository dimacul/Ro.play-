package Model;
import Model.Account;

public class Administrator extends Account{
    private String parolaAdmin;
    public Administrator(int id, String username, String email, String password, String parolaAdmin) {
        super(id, username, email, password);
        this.parolaAdmin = parolaAdmin;

    }

    public String getParolaAdmin() {
        return parolaAdmin;
    }
    public void setParolaAdmin(String parolaAdmin) {
        this.parolaAdmin = parolaAdmin;
    }

}
