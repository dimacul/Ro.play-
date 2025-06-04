package Service;

import BazaDeDate.DAOs.AdministratorDAO;
import BazaDeDate.DAOs.UserDAO;
import Model.Account;
import Model.Administrator;
import Model.User;

import java.sql.Connection;
import java.util.Scanner;

public class AccountService {
    private final UserService userService;
    private final AdministratorService administratorService;
    private final UserDAO userDAO;
    private final AdministratorDAO adminDAO;
    private final Scanner scanner;

    public AccountService(Connection connection) {
        this.userService = new UserService(connection);
        this.administratorService = new AdministratorService(connection);
        this.userDAO = UserDAO.getInstance(connection);
        this.adminDAO = AdministratorDAO.getInstance(connection);
        this.scanner = new Scanner(System.in);
    }

    public Account login() {
        System.out.println("Selecteaza tipul de cont la care vrei sa te loghezi:");
        System.out.println("1. User");
        System.out.println("2. Administrator");
        System.out.print("Optiunea ta: ");
        String optiune = scanner.nextLine().trim();

        System.out.print("\nUsername: ");
        String username = scanner.nextLine().trim();
        System.out.print("Parola: ");
        String password = scanner.nextLine().trim();

        if (optiune.equals("1")) {
            if (userService.login(username, password)) {
                User user = userDAO.findByUsername(username);
                System.out.println("Autentificat cu succes cu username-ul: " + user.getUsername());
                return user;
            }
            else{
                System.out.println("-----------------Autentificare esuata-----------------");
            }

        } else if (optiune.equals("2")) {
            if (administratorService.login(username, password)) {
                Administrator administrator = adminDAO.findByUsername(username);
                System.out.print("Introduceti parola de siguranta:");

                String parola_admin = scanner.nextLine().trim();
                if (parola_admin.equals(administrator.getParolaAdmin())) {
                    System.out.println("Autentificat cu succes ca administrator-ul: " + administrator.getUsername());
                    return administrator;
                }
                else{
                    System.out.println("-----------------Autentificare esuata-----------------");
                }
            }
        }
        else {
            System.out.println("-----------------Optiune invalida. Rusine!-----------------");
        }
        return null;
    }
}
