

package Service;

import BazaDeDate.DAOs.*;
import Model.*;
import Model.Enums.MusicGenre;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import Audit.Audit;

public class AdministratorService {
    private final AdministratorDAO adminDAO;
    private final SongDAO songDAO;
    private final SoloArtistDAO soloDAO;
    private final BandDAO bandDAO;

    public AdministratorService(Connection connection) {
        this.adminDAO = AdministratorDAO.getInstance(connection);
        this.songDAO = SongDAO.getInstance(connection);
        this.soloDAO = SoloArtistDAO.getInstance(connection);
        this.bandDAO = BandDAO.getInstance(connection);
    }

    public boolean login(String username, String password) {
        Administrator admin = adminDAO.findByUsername(username);
        if (admin == null) {
            return false;
        }
        return admin.getPassword().equals(password);
    }

    public void adminMenu(Administrator admin, Connection connection) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n---------------------------- Meniu administrator -----------------------------");
            System.out.println("1. Add Admin");
            System.out.println("2. Delete Admin");
            System.out.println("3. Add Song");
            System.out.println("4. Add Artist");
            System.out.println("5. View All Songs");
            System.out.println("6. Search Song");
            System.out.println("7. Edit Song");
            System.out.println("8. Exit");
            System.out.print("Choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Email: ");
                    String email = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String password = scanner.nextLine().trim();
                    System.out.print("Admin Password Code: ");
                    String parolaAdmin = scanner.nextLine().trim();
                    Administrator newAdmin = new Administrator(0, username, email, password, parolaAdmin);
                    try{
                        adminDAO.create(newAdmin);
                        System.out.println("Admin added with ID = " + newAdmin.getId());
                        Audit.logInAudit("Administratorul "+admin.getUsername() + " a adaugat admin-ul: "+newAdmin.getUsername());
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                }
                case 2 -> {
                    List<Administrator> admins = adminDAO.readAll();
                    if (admins.isEmpty()) {
                        System.out.println("No administrators found.");
                        break;
                    }
                    System.out.println("Administratori:");
                    for (int i = 0; i < admins.size(); i++) {
                        Administrator a = admins.get(i);
                        System.out.printf("%d. %s (ID: %d)%n", i + 1, a.getUsername(), a.getId());
                    }
                    System.out.print("Enter ID of admin to delete: ");
                    int idToDelete;
                    try {
                        idToDelete = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                        break;
                    }
                    try{
                        adminDAO.delete(idToDelete);
                        System.out.println("Admin with ID " + idToDelete + " deleted.");
                        Audit.logInAudit("Administratorul "+admin.getUsername() + " a eliminat admin-ul cu id: "+idToDelete);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        break;
                    }

                }
//                case 3 -> {
//                    System.out.print("Title: ");
//                    String title = scanner.nextLine().trim();
//                    System.out.print("Artist ID: ");
//                    int artistId;
//                    try {
//                        artistId = Integer.parseInt(scanner.nextLine().trim());
//                    } catch (NumberFormatException e) {
//                        System.out.println("Invalid artist ID.");
//                        break;
//                    }
//                    System.out.print("Album: ");
//                    String album = scanner.nextLine().trim();
//                    System.out.print("Path: ");
//                    String path = scanner.nextLine().trim();
//                    Song newSong = new Song(0, title, null, album, path);
//                    songDAO.create(newSong);
//                    System.out.println("Song added with ID = " + newSong.getId());
//                }
                case 3 -> {
                    System.out.print("Title: ");
                    String title = scanner.nextLine().trim();
                    System.out.println("Tip artist: SOLO/BAND:");
                    String tip_artist = scanner.nextLine().trim();
                    System.out.print("Artist ID: ");
                    int artistId;
                    try {
                        artistId = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid artist ID.");
                        break;
                    }
                    Artist artist = null;
                    SoloArtistDAO soloDAO = SoloArtistDAO.getInstance(connection);
                    BandDAO bandDAO = BandDAO.getInstance(connection);
                    SoloArtist solo = null;
                    Band band  = null;
                    if (tip_artist.equals("SOLO")) {
                        solo = soloDAO.read(artistId);
                    }
                    else{
                        band = bandDAO.read(artistId);
                    }


                    if (solo != null) {
                        artist = solo;
                    } else if (band != null) {
                        artist = band;

                    }
                    else{
                        System.out.println("Artist not found.");
                        break;
                    }
                    System.out.print("Album: ");
                    String album = scanner.nextLine().trim();
                    System.out.print("Path: ");
                    String path = scanner.nextLine().trim();
                    Song newSong = new Song(0, title, artist, album, path);
                    try{
                        songDAO.create(newSong);
                        System.out.println("Song added with ID: " + newSong.getId());
                        Audit.logInAudit("Admin-ul"+admin.getUsername() + " a adaugat melodia: " + newSong.getTitle()+", id_song: "+newSong.getId());
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                }

                case 4 -> {
                    System.out.println("1. Add Solo Artist");
                    System.out.println("2. Add Band");
                    System.out.print("Choice: ");
                    int type;
                    try {
                        type = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid option.");
                        break;
                    }
                    if (type == 1) {
                        System.out.print("Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Genre (options: " + List.of(MusicGenre.values()) + "): ");
                        MusicGenre genre;
                        try {
                            genre = MusicGenre.valueOf(scanner.nextLine().trim());
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid genre.");
                            break;
                        }
                        System.out.print("Contact Email: ");
                        String contact = scanner.nextLine().trim();
                        System.out.print("Country: ");
                        String country = scanner.nextLine().trim();
                        System.out.print("Description: ");
                        String desc = scanner.nextLine().trim();
                        SoloArtist solo = new SoloArtist(0, name, genre, contact, country, desc);
                        try{
                            soloDAO.create(solo);
                            System.out.println("Solo artist added successfully.");
                            Audit.logInAudit("Administratorul "+admin.getUsername()+" a adaugat artistul: "+solo.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    } else if (type == 2) {
                        System.out.print("Band Name: ");
                        String bname = scanner.nextLine().trim();
                        System.out.print("Genre (options: " + List.of(MusicGenre.values()) + "): ");
                        MusicGenre bgenre;
                        try {
                            bgenre = MusicGenre.valueOf(scanner.nextLine().trim());
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid genre.");
                            break;
                        }
                        System.out.print("Contact Email: ");
                        String bcontact = scanner.nextLine().trim();
                        System.out.print("Description: ");
                        String bdesc = scanner.nextLine().trim();
                        Band band = new Band(0, bname, bgenre, bcontact, List.of(), bdesc);
                        bandDAO.create(band);
                        int bandId = band.getId();
                        System.out.print("How many members to add? ");
                        int count;
                        try {
                            count = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid number.");
                            break;
                        }
                        for (int i = 0; i < count; i++) {
                            System.out.print("Enter Solo Artist ID for member #" + (i+1) + ": ");
                            int memberId;
                            try {
                                memberId = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid ID, skipping.");
                                continue;
                            }
                            bandDAO.addMember(bandId, memberId);
                        }
                        System.out.println("Band added with ID: " + bandId);
                        Audit.logInAudit("Administratorul "+admin.getUsername()+" a adaugat trupa: "+band.getName());
                    } else {
                        System.out.println("Invalid option.");
                    }
                }
                case 5 -> {
                    List<Song> allSongs = songDAO.readAll();
                    Collections.sort(allSongs);
                    if (allSongs.isEmpty()) {
                        System.out.println("No songs found.");
                    } else {
                        for (Song s : allSongs) {
                            System.out.printf("%d. %s - %s%n", s.getId(), s.getTitle(), s.getArtist().getName());
                        }
                    }
                }
                case 6 -> {
                    System.out.print("Enter title to search: ");
                    String q = scanner.nextLine().trim().toLowerCase();
                    List<Song> all = songDAO.readAll();
                    for (Song s : all) {
                        if (s.getTitle().toLowerCase().contains(q)) {
                            System.out.printf("%d. %s - %s%n", s.getId(), s.getTitle(), s.getArtist().getName());
                        }
                    }
                }
                case 7 -> {
                    System.out.print("Enter Song ID to edit: ");
                    int sid;
                    try {
                        sid = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID.");
                        break;
                    }
                    Song s = songDAO.read(sid);
                    if (s == null) {
                        System.out.println("Song not found.");
                        break;
                    }
                    System.out.println("1. Edit Title");
                    System.out.println("2. Edit Album");
                    System.out.println("3. Edit Path");
                    System.out.println("4. Edit Artist ID");
                    System.out.print("Choice: ");
                    int ec;
                    try {
                        ec = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid.");
                        break;
                    }
                    switch (ec) {
                        case 1 -> {
                            System.out.print("New Title: ");
                            s.setTitle(scanner.nextLine().trim());
                        }
                        case 2 -> {
                            System.out.print("New Album: ");
                            s.setAlbum(scanner.nextLine().trim());
                        }
                        case 3 -> {
                            System.out.print("New Path: ");
                            s.setPath(scanner.nextLine().trim());
                        }
                        case 4 -> {
                            System.out.print("New Artist ID: ");
                            int aid;
                            try {
                                aid = Integer.parseInt(scanner.nextLine().trim());
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid ID.");
                                break;
                            }
                            Artist a = null;
                            SoloArtist sa = soloDAO.read(aid);
                            if (sa != null) a = sa;
                            else {
                                Band b = bandDAO.read(aid);
                                if (b != null) a = b;
                            }
                            if (a == null) {
                                System.out.println("Artist not found.");
                                break;
                            }
                            s.setArtist(a);
                        }
                        default -> {
                            System.out.println("Invalid option.");
                            continue;
                        }
                    }
                    try{
                        songDAO.update(s);
                        System.out.println("Song updated.");
                        Audit.logInAudit("Admin. "+admin.getUsername()+" a updatat melodia cu id: "+s.getId());
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                }
                case 8 -> {
                    System.out.println("Exiting meniu admin.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
}
