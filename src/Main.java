import Audit.Audit;
import BazaDeDate.DAOs.*;
import Model.*;
import Service.AccountService;
import Service.AdministratorService;
import Service.SongService;
import Service.UserService;

import java.util.Scanner;
import java.sql.Connection;

public class Main {

    public static void main(String[] args) {
        Account currentAccount = null;

        Connection connection = DatabaseConnection.getConnection();
        UserService userService = new UserService(connection);
        AdministratorService administratorService = new AdministratorService(connection);

        Scanner sc = new Scanner(System.in);

        System.out.println(" -------------------------------------------------------------------------------------------");
        System.out.println("|                                     Welcome to Ro.play()                                  |");
        System.out.println("|                              - muzica (si manele) cate vrei -                             |");
        System.out.println(" -------------------------------------------------------------------------------------------");
        boolean ok = true;
        while (ok) {
            System.out.println("\n");
            System.out.println("                                        MENIU PRINCIPAL");
            System.out.println(" Selecteaza o optiune:   ");
            System.out.println("                                      1. Shuffle");
            if (currentAccount != null) {
                System.out.println("                                      2. Logout");
                System.out.println("                                      3. Exit");
                System.out.println("                                      4. Meniu");
            } else {
                System.out.println("                                      2. Login");
                System.out.println("                                      3. Sign up");
                System.out.println("                                      4. Exit");
            }

            int option = sc.nextInt();

            switch (option) {
                case 1 -> {
                    SongService ss = new SongService(connection);
                    ss.shuffleAndPlayAll(currentAccount instanceof User ? (User) currentAccount : null);
                }
                case 2 -> {
                    if (currentAccount instanceof Administrator) {
                        Audit.logInAudit("Administratorul " + currentAccount.getUsername() + " s-a deconectat");
                        currentAccount = null;
                        System.out.println("V-ati deconectat de la contul de administrator!");
                    } else if (currentAccount instanceof User) {
                        Audit.logInAudit("Userul " + currentAccount.getUsername() + " s-a deconectat");
                        currentAccount = null;
                        System.out.println("V-ati deconectat de la contul de user!");
                    } else {
                        Account result = null;
                        AccountService as = new AccountService(connection);
                        boolean loggingIn = true;
                        while (loggingIn) {
                            result = as.login();
                            if (result != null) break;
                            else {
                                System.out.print("Incerci din nou? ( DA / NU ): ");
                                sc.nextLine();
                                String raspuns = sc.nextLine().toLowerCase().trim();
                                if (raspuns.equals("nu")) loggingIn = false;
                            }
                        }
                        if (result != null) {
                            currentAccount = result;
                            if (result instanceof User) {
                                Audit.logInAudit("Userul " + currentAccount.getUsername() + " s-a conectat");
                                userService.userMenu((User) currentAccount, connection);
                            } else {
                                Audit.logInAudit("Administratorul " + currentAccount.getUsername() + " s-a conectat");
                            }
                        }
                    }
                }
                case 3 -> {
                    if (currentAccount != null) {
                        ok = false;
                        Audit.logInAudit("Se inchide aplicatia");
                    } else {
                        User newUser = userService.signUp();
                        if (newUser != null) {
                            Audit.logInAudit("Un user nou si-a facut cont: " + newUser.getUsername());
                            System.out.println("Autentifica-te cu noul tau cont!");

                            boolean loggingIn = true;
                            while (loggingIn) {
                                sc.nextLine();
                                System.out.print("Username: ");
                                String username = sc.nextLine().trim();
                                System.out.print("Password: ");
                                String password = sc.nextLine().trim();
                                if (userService.login(username, password)) {
                                    System.out.println("Bine ai venit, " + newUser.getUsername() + "!");
                                    currentAccount = newUser;
                                    Audit.logInAudit("Userul " + currentAccount.getUsername() + " s-a conectat");
                                    break;
                                } else {
                                    System.out.print("Date invalide. Incerci din nou? (DA/NU): ");
                                    Audit.logInAudit("Sign in esuat: username incercat: " + username + "/parola incercata: " + password);
                                    String raspuns = sc.nextLine().toLowerCase().trim();
                                    if (raspuns.equals("nu")) loggingIn = false;
                                }
                            }
                        } else {
                            System.out.println("Sign up failed!");
                        }
                    }
                }
                case 4 -> {
                    if (currentAccount instanceof User) {
                        Audit.logInAudit("Userul " + currentAccount.getUsername() + " acceseaza meniul");
                        userService.userMenu((User) currentAccount, connection);
                    } else if (currentAccount instanceof Administrator) {
                        Audit.logInAudit("Administratorul " + currentAccount.getUsername() + " acceseaza meniul");
                        administratorService.adminMenu((Administrator) currentAccount, connection);
                    } else {
                        ok = false;
                    }
                }
                default -> ok = false;
            }
        }
    }
}










//import Audit.Audit;
//import BazaDeDate.DAOs.*;
//import Model.*;
//import Service.AccountService;
//import Service.AdministratorService;
//import Service.SongService;
//import Service.UserService;
//
//import java.util.Scanner;
//import java.sql.Connection;
//
//public class Main {
//
//    public static void main(String[] args) {
//        User currentUser = null;
//        Administrator currentAdministrator = null;
//
//        Connection connection = DatabaseConnection.getConnection();
//        UserService userService = new UserService(connection);
//        AdministratorService administratorService = new AdministratorService(connection);
//
//        Scanner sc = new Scanner(System.in);
//
//        System.out.println(" -------------------------------------------------------------------------------------------");
//        System.out.println("|                                     Welcome to Ro.play()                                  |");
//        System.out.println("|                              - muzica (si manele) cate vrei -                             |");
//        System.out.println(" -------------------------------------------------------------------------------------------");
//        boolean ok = true;
//        while (ok) {
//            System.out.println("\n");
//            System.out.println("                                        MENIU PRINCIPAL");
//            System.out.println(" Selecteaza o optiune:   ");
//            System.out.println("                                      1. Shuffle");
//            if (currentUser != null || currentAdministrator != null) {
//                System.out.println("                                      2. Logout");
//                System.out.println("                                      3. Exit");
//                System.out.println("                                      4. Meniu");
//            }
//            else{
//                System.out.println("                                      2. Login");
//                System.out.println("                                      3. Sign up");
//                System.out.println("                                      4. Exit");
//            }
//
//
//
//            int option = sc.nextInt();
//
//            switch (option) {
//                case 1:
//                    SongService ss = new SongService(connection);
//                    ss.shuffleAndPlayAll(currentUser);
//                    break;
//                case 2:
//                    if (currentAdministrator != null) {
//                        Audit.logInAudit("Administratorul "+ currentAdministrator.getUsername() + " s-a deconectat");
//                        currentAdministrator = null;
//                        System.out.println("V-ati deconectat de la contul de administrator!");
//                    }
//                    else if(currentUser != null){
//                        Audit.logInAudit("Userul "+currentUser.getUsername() + " s-a deconectat");
//                        currentUser = null;
//                        System.out.println("V-ati deconectat de la contul de user!");
//                    }
//                    else {
//                        Account result = null;
//                        AccountService as = new AccountService(connection);
//                        boolean logingIn = true;
//                        while (logingIn) {
//                            result = as.login();
//                            if (result != null)
//                                break;
//                            else {
//                                System.out.print("Incerci din nou? ( DA / NU ): ");
//                                sc.nextLine();
//                                String raspuns = sc.nextLine().toLowerCase().trim();
//                                if (raspuns.equals("nu"))
//                                    logingIn = false;
//                            }
//
//                        }
//                        if (result != null) {
//                            if (result instanceof User) {
//                                currentUser = (User) result;
//                                Audit.logInAudit("Userul "+currentUser.getUsername() + " s-a conectat");
//                                userService.userMenu(currentUser, connection);
//                            } else if (result instanceof Administrator) {
//
//                                currentAdministrator = (Administrator) result;
//                                Audit.logInAudit("Administratorul "+currentAdministrator.getUsername() + " s-a conectat");
//                            }
//                        }
//                    }
//                    break;
//                case 3:
//                    //Sign up + login SAU exit daca sunt deja logat
//                    if (currentAdministrator != null || currentUser != null) {
//                        ok = false;
//                        Audit.logInAudit("Se inchide aplicatia");
//                    }
//                    else{
//                        User newUser = userService.signUp();
//                        if (newUser != null) {
//                            Audit.logInAudit("Un user nou si-a facut cont: "+newUser.getUsername());
//                            System.out.println("Autentifica-te cu noul tau cont!");
//
//                            boolean loggingIn = true;
//                            while (loggingIn) {
//                                sc.nextLine();
//                                System.out.print("Username: ");
//                                String username = sc.nextLine().trim();
//                                System.out.print("Password: ");
//                                String password = sc.nextLine().trim();
//                                if (userService.login(username, password)) {
//                                    System.out.println("Bine ai venit, " + newUser.getUsername() + "!");
//                                    currentUser = newUser;
//                                    Audit.logInAudit("Userul "+currentUser.getUsername() + " s-a conectat");
//                                    break;
//                                }
//                                else {
//                                    System.out.print("Date invalide. Incerci din nou? (DA/NU): ");
//                                    Audit.logInAudit("Sign in esuat: "+"username incercat: "+username +"/parola incercata: "+password);
//                                    String raspuns = sc.nextLine().toLowerCase().trim();
//                                    if (raspuns.equals("nu")) {
//                                        loggingIn = false;
//                                    }
//                                }
//                            }
//                        } else {
//                            System.out.println("Sign up failed!");
//
//                        }
//                    }
//
//                    break;
//                case 4:
//                    if (currentUser != null) {
//                        Audit.logInAudit("Userul "+currentUser.getUsername() + " acceseaza meniul");
//                        userService.userMenu(currentUser, connection);
//                    }
//                    else if (currentAdministrator != null) {
//                        Audit.logInAudit("Administratorul "+currentAdministrator.getUsername() + " acceseaza meniul");
//                        administratorService.adminMenu(currentAdministrator, connection);
//                    }
//                    else{
//                        ok = false;
//                    }
//                    break;
//                default:
//                    ok = false;
//                    break;
//
//            }
//        }
//
//
//
//    }
//}
