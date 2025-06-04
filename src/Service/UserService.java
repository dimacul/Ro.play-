package Service;

import BazaDeDate.DAOs.PlaylistDAO;
import BazaDeDate.DAOs.SongDAO;
import BazaDeDate.DAOs.UserDAO;
import BazaDeDate.DAOs.LikedSongsDAO;
import Model.Enums.MusicGenre;
import Model.Playlist;
import Model.Song;
import Model.User;
import Audit.Audit;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class UserService {
    private final UserDAO userDAO;

    public UserService(Connection connection) {
        this.userDAO = UserDAO.getInstance(connection);
    }

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }

    public User signUp() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("----------------------------- Sign up new user ---------------------------- // EXIT: (e)");

        String username;
        while (true) {
            System.out.print("Alege un username: ");
            username = scanner.nextLine().trim();
            if (username.equals("e")){
                return null;
            }
            if (username.isEmpty()) {
                System.out.println("Username nu poate fi gol.");
                continue;
            }
            if (userDAO.findByUsername(username) != null) {
                System.out.println("Acest username exista deja in baza de date. Alege altul.");
                continue;
            }
            break;
        }

        String email;
        while (true) {
            System.out.print("Email: ");
            email = scanner.nextLine().trim();
            if (email.equals("e")){
                return null;
            }
            if (email.isEmpty()) {
                System.out.println("Email nu poate fi gol.");
                continue;
            }
            if (userDAO.findByEmail(email) != null) {
                System.out.println("Email deja folosit. Introdu alt email.");
                continue;
            }
            break;
        }

        String password;
        while (true) {
            System.out.print("Parola: ");
            password = scanner.nextLine().trim();
            if (password.equals("e")){
                return null;
            }
            if (password.length() < 6) {
                System.out.println("Parola trebuie sa contina cel putin 6 caractere!");
                continue;
            }
            break;
        }

        User newUser = new User(
                0, // id va fi generat de baza de date
                username, email, password, false,
                "" //descrierea
        );
        userDAO.create(newUser);
        System.out.println("Inregistrare a fost efectuata cu succes! ID User: " + newUser.getId());
        Audit.logInAudit("User nou inregistrat: "+newUser.getUsername()+ ", id: "+newUser.getId());
        String likedTable = "likedSongs_" + username;
        String sqlCreate = "CREATE TABLE IF NOT EXISTS `" + likedTable + "` ("
                + " song_id INT NOT NULL, "
                + " added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + " PRIMARY KEY(song_id), "
                + " FOREIGN KEY(song_id) REFERENCES song(id) "
                + ")";
        try (Statement stmt = userDAO.getConnection().createStatement()) {
            stmt.executeUpdate(sqlCreate);
            Audit.logInAudit("Tabel " + likedTable + " creat pentru user.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newUser;
    }

    private void showLikedSongs(User user, Connection connection) {
        LikedSongsDAO likedDAO = new LikedSongsDAO(connection, user.getUsername());
        List<Song> liked = likedDAO.readAllLiked();
        if (liked.isEmpty()) {
            System.out.println("Nu ai apreciat nicio melodie inca.");
            return;
        }
        System.out.println("\n--- Melodii apreciate de " + user.getUsername() + " ---");
        SongDAO songDAO = SongDAO.getInstance(connection);

        // Vom reciti fiecare Song prin SongDAO.read(id) pentru a avea Artist dif de null
        List<Song> fullSongs = new ArrayList<>();
        for (Song s : liked) {
            Song full = songDAO.read(s.getId());
            if (full != null) {
                fullSongs.add(full);
            }
        }

        for (int i = 0; i < fullSongs.size(); i++) {
            Song s = fullSongs.get(i);
            System.out.printf("%d. %s - %s%n", i + 1, s.getTitle(), s.getArtist().getName());
        }

        System.out.println("\n0 – intoarce-te; 1 – alege o melodie pentru redare");
        System.out.print("Optiunea mea: ");
        Scanner scanner = new Scanner(System.in);
        int opt;
        try {
            opt = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (opt == 1) {
            System.out.print("Selecteaza numarul melodiei: ");
            int idx;
            try {
                idx = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid.");
                return;
            }
            if (idx < 1 || idx > fullSongs.size()) {
                System.out.println("In afara intervalului.");
                return;
            }
            Song chosen = fullSongs.get(idx - 1);
            SongService songService = new SongService(connection);
            songService.playSong(chosen, user);
        }
    }

    private void recommendSong(User user, Connection connection) {
        LikedSongsDAO likedDAO = new LikedSongsDAO(connection, user.getUsername());
        List<Song> likedPartial = likedDAO.readAllLiked();
        if (likedPartial.isEmpty()) {
            System.out.println("Nu ai apreciat nicio melodie, nu iti putem face recomandari!");
            return;
        }

        SongDAO songDAO = SongDAO.getInstance(connection);
        List<Song> likedFull = new ArrayList<>();
        for (Song partial : likedPartial) {
            Song full = songDAO.read(partial.getId());
            if (full != null) {
                likedFull.add(full);
            }
        }


        Map<MusicGenre, Integer> freq = new HashMap<>();
        for (Song s : likedFull) {
            MusicGenre genreEnum = MusicGenre.valueOf(s.getArtist().getGenre());
            freq.put(genreEnum, freq.getOrDefault(genreEnum, 0) + 1);
        }
        MusicGenre preferred = null;
        int maxCount = -1;
        for (var entry : freq.entrySet()) {
            if (entry.getValue() > maxCount) {
                preferred = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        Set<Integer> likedIds = likedFull.stream().map(Song::getId).collect(Collectors.toSet());

        Song recommendation = null;
        for (Song s : songDAO.readAll()) {
            MusicGenre g = MusicGenre.valueOf(s.getArtist().getGenre());
            if (g == preferred && !likedIds.contains(s.getId())) {
                recommendation = s;
                break;
            }
        }

        if (recommendation == null) {
            System.out.println("Nu am gasit nicio melodie noua din genul " + preferred + ", pe care am observat că il preferi");
            return;
        }

        System.out.println("Pentru ca apreciezi genul:  " + preferred + ", echipa RO.PLAY() iti recomanda: "
                + recommendation.getTitle() + " - " + recommendation.getArtist().getName());

        System.out.println("\n1. Play");
        System.out.println("2. Inapoi la meniu");
        System.out.print("Optiunea mea: ");

        Scanner scanner = new Scanner(System.in);
        int opt;
        try {
            opt = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return;
        }
        if (opt == 1) {
            SongService songService = new SongService(connection);
            songService.playSong(recommendation, user);
        }
    }

    private void editAccount(User user, Connection connection) {
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = UserDAO.getInstance(connection);

        boolean editing = true;
        while (editing) {
            System.out.println("\n--- Hello, " + user.getUsername() + ", aici iti poti edita contul! ---");
            System.out.println("1. Schimba email");
            System.out.println("2. Schimba descriere");
            System.out.println("3. Inapoi la meniu");
            System.out.print("Optiunea mea: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Optiune invalida. Incearca din nou.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    System.out.println("Email curent: " + user.getEmail());
                    System.out.print("Introdu noul email (sau lasa gol pentru a renunta): ");
                    String newEmail = scanner.nextLine().trim();
                    if (newEmail.isEmpty()) {
                        System.out.println("Renuntasi la schimbarea email-ului.");
                        break;
                    }
                    if (newEmail.equals(user.getEmail())) {
                        System.out.println("Email-ul introdus este acelasi cu cel curent.");
                        break;
                    }
                    if (userDAO.findByEmail(newEmail) != null) {
                        System.out.println("Email-ul este deja folosit.");
                        break;
                    }
                    user.setEmail(newEmail);
                    userDAO.update(user);
                    System.out.println("Email-ul a fost actualizat cu succes.");
                    Audit.logInAudit("Userul: "+user.getUsername()+" si-a schimbat emailul in: "+user.getEmail());
                }
                case 2 -> {
                    System.out.println("Descriere curenta: " + user.getDescriere());
                    System.out.print("Introdu noua descriere (sau lasa gol pentru a renunta): ");
                    String newDescr = scanner.nextLine().trim();
                    if (newDescr.isEmpty()) {
                        System.out.println("Renuntasi la schimbarea descrierii.");
                        break;
                    }
                    user.setDescriere(newDescr);
                    userDAO.update(user);
                    System.out.println("Descrierea a fost actualizata cu succes.");
                    Audit.logInAudit("Userul: "+user.getUsername()+" si-a schimbat descrierea in: "+user.getDescriere());
                }
                case 3 -> editing = false;
                default -> System.out.println("Optiune invalida. Incearca din nou.");
            }
        }
    }


    public void userMenu(User user, Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nSalutare, " + user.getUsername() + ", iata ce poti face in Ro.play():");
        boolean running = true;

        while (running) {
            System.out.println("\n------------------------------- Meniu -----------------------------------");
            System.out.println("1. Cauta o melodie");
            System.out.println("2. Vezi playlisturile tale");
            System.out.println("3. Creeaza playlist nou");
            System.out.println("4. Recomanda-mi o melodie");
            System.out.println("5. Liked songs");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            System.out.println("6. Edit account");
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
            System.out.println("7. Exit");
            System.out.print("Optiunea mea: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Optiune invalida...");
                continue;
            }

            switch (choice) {
                case 1 -> searchSongFlow(connection, user);
                case 2 -> listUserPlaylists(user, connection);
                case 3 -> createNewPlaylist(user, connection);
                case 4 -> recommendSong(user, connection);
                case 5 -> showLikedSongs(user, connection);
                case 6 -> editAccount(user, connection);
                case 7 -> running = false;
                default -> {
                    System.out.println("Optiune invalida...");
                    running = false;
                }
            }
        }
    }

    // Userul introduce un sir de carcatere si se cauta in bd toate melodiile ce contin acel sir fie
    // in titlu, fie in numele artistului, fie in genre
    private void searchSongFlow(Connection connection, User user) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Search song: ");
        String query = scanner.nextLine().toLowerCase().trim();

        Audit.logInAudit("Userul "+user.getUsername()+" a cautat: "+query);

        SongDAO songDAO = SongDAO.getInstance(connection);
        List<Song> allSongs = songDAO.readAll();
        List<Song> matches = new ArrayList<>();
        for (Song s : allSongs) {
            String title = s.getTitle().toLowerCase();
            String artistName = s.getArtist().getName().toLowerCase();
            String genre = s.getArtist().getGenre().toLowerCase();
            if (title.contains(query) || artistName.contains(query) || genre.contains(query)) {
                matches.add(s);
            }
        }

        System.out.println("\n" + matches.size() + " melodii coincid. ");
        if (matches.isEmpty()) return;

        System.out.print("Vezi melodii gasite? (DA/NU): ");
        String raspuns = scanner.nextLine().toLowerCase().trim();
        if (!raspuns.equals("da")) return;

        int limit = matches.size();
        for (int i = 0; i < limit; i++) {
            Song s = matches.get(i);
            System.out.printf("%d. %s - %s%n", i + 1, s.getTitle(), s.getArtist().getName());
        }

        System.out.print("\nSelecteaza o melodie (1–" + limit + "): ");
        int sel;
        try {
            sel = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Eroare!");
            return;
        }
        if (sel < 1 || sel > limit) {
            System.out.println("Selecteaza o optiune valida!");
            return;
        }

        Song chosen = matches.get(sel - 1);
        SongService songService = new SongService(connection);
        songService.playSong(chosen, user);
        Audit.logInAudit("Userul "+user.getUsername()+" a dat play dupa cautare melodiei: "+chosen.getTitle());
    }

    private void listUserPlaylists(User user, Connection connection) {
        PlaylistDAO playlistDAO = PlaylistDAO.getInstance(connection);
        List<Playlist> playlists = playlistDAO.readAllByUser(user.getId());
        if (playlists.isEmpty()) {
            System.out.println("Nu exista playlist-uri.");
            return;
        }

        System.out.println("\nPlaylisturile tale:");
        for (int i = 0; i < playlists.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, playlists.get(i).getName());
        }

        System.out.print("\nSelecteaza un playlist sau apasa 0 pentru a reveni: ");
        Scanner scanner = new Scanner(System.in);
        int sel;
        try {
            sel = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Ati introdus o valoare invalida");
            return;
        }
        if (sel == 0) {
            return;
        }
        if (sel < 1 || sel > playlists.size()) {
            System.out.println("Indice invalid.");
            return;
        }

        Playlist chosen = playlists.get(sel - 1);
        playlistMenu(chosen, connection, user);
    }

    private void playlistMenu(Playlist playlist, Connection connection, User user) {
        Scanner scanner = new Scanner(System.in);
        PlaylistDAO playlistDAO = PlaylistDAO.getInstance(connection);
        PlaylistService playlistService = new PlaylistService(connection);
        SongService songService = new SongService(connection);

        while (true) {
            //Reincarcam playlist-ul pentru a avea lista actualizata de melodii
            Playlist full = playlistDAO.read(playlist.getId());
            List<Song> songs = new ArrayList<>(full.getSongs());

            System.out.println("\n--- Playlist: " + full.getName() + " ---");
            if (songs.isEmpty()) {
                System.out.println("-- Acest playlist e gol. --");
            } else {
                for (int i = 0; i < songs.size(); i++) {
                    Song s = songs.get(i);
                    System.out.printf("%d. %s - %s%n", i + 1, s.getTitle(), s.getArtist().getName());
                }
            }

            System.out.println("\nCe doresti sa faci?");
            System.out.println("0. Asculta melodiile din acest playlist");
            System.out.println("1. Add song to playlist");
            System.out.println("2. Remove song from playlist");
            System.out.println("3. Delete playlist");
            System.out.println("4. Exit");
            System.out.print("Optiunea mea: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Optiune invalida. Incearca din nou.");
                continue;
            }

            switch (choice) {
                case 0 -> {
                    if (songs.isEmpty()) {
                        System.out.println("Playlist-ul este gol, nu sunt melodii de ascultat.");
                    } else {
                        Audit.logInAudit("Userul "+user.getUsername()+" asculta melodii din playlistul "+full.getName());
                        for (Song s : songs) {
                            songService.playSong(s, user);
                        }
                    }
                }
                case 1 -> {
                    System.out.print("Cauta o melodie in baza de date: ");
                    String query = scanner.nextLine().toLowerCase().trim();

                    SongDAO songDAO = SongDAO.getInstance(connection);
                    List<Song> allSongs = songDAO.readAll();
                    List<Song> matches = new ArrayList<>();
                    for (Song s : allSongs) {
                        String title      = s.getTitle().toLowerCase();
                        String artistName = s.getArtist().getName().toLowerCase();
                        if (title.contains(query) || artistName.contains(query)) {
                            matches.add(s);
                        }
                    }

                    System.out.println("\n" + matches.size() + " melodii gasite.");
                    if (matches.isEmpty()) break;

                    System.out.print("Vezi melodii gasite? (DA/NU): ");
                    String resp = scanner.nextLine().toLowerCase().trim();
                    if (!resp.equals("da")) break;

                    int limit = matches.size();
                    for (int i = 0; i < limit; i++) {
                        Song s = matches.get(i);
                        System.out.printf("%d. %s - %s%n", i + 1, s.getTitle(), s.getArtist().getName());
                    }

                    System.out.print("\nSelecteaza o melodie (1–" + limit + "): ");
                    int sel;
                    try {
                        sel = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Selectie invalida.");
                        break;
                    }
                    if (sel < 1 || sel > limit) {
                        System.out.println("Selectie invalida.");
                        break;
                    }

                    int songIdToAdd = matches.get(sel - 1).getId();
                    playlistService.addSongToPlaylist(full.getId(), songIdToAdd);
                    Audit.logInAudit("Userul "+user.getUsername()+" a adaugat in playlistul "+full.getName() + "melodia: "+matches.get(sel-1).getTitle());
                }

                case 2 -> {
                    if (songs.isEmpty()) {
                        System.out.println("Playlist-ul este gol, nu exista melodii de sters.");
                        break;
                    }
                    System.out.print("Introdu numarul melodiei de sters (1–" + songs.size() + "): ");
                    int idx;
                    try {
                        idx = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Numar invalid.");
                        break;
                    }
                    if (idx < 1 || idx > songs.size()) {
                        System.out.println("Numar in afara intervalului.");
                    } else {
                        int songIdToRemove = songs.get(idx - 1).getId();
                        Audit.logInAudit("Userul "+user.getUsername()+" a sters din playlistul "+full.getName() + "melodia: "+songs.get(idx - 1).getTitle());

                        playlistService.removeSongFromPlaylist(full.getId(), songIdToRemove);
                    }
                }
                case 3 -> {
                    System.out.print("Esti sigur ca vrei sa stergi acest playlist? (DA/NU): ");
                    String confirm = scanner.nextLine().toLowerCase().trim();
                    if (confirm.equals("da")) {
                        Audit.logInAudit("Userul "+user.getUsername()+" a sters playlistul: "+full.getName());
                        playlistDAO.delete(full.getId());

                        System.out.println("Playlist-ul a fost sters.");

                        return; // revenim la meniul anterior
                    }
                }
                case 4 -> {
                    return; // ies din playlistMenu
                }
                default -> System.out.println("Optiune invalida. Incearca din nou.");
            }
        }
    }



    private void createNewPlaylist(User user, Connection connection) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Alege un nume pentru playlist-ul nou: ");
        String playlistName = scanner.nextLine().trim();
        if (playlistName.isEmpty()) {
            System.out.println("Numele playlist-ului nu poate fi gol.");
            return;
        }

        Playlist newPlaylist = new Playlist(
                0,                     // de id se ocupa bd
                playlistName,
                new LinkedHashSet<>(), // set gol de Song
                user                   // owner
        );
        Audit.logInAudit("Userul "+user.getUsername()+" a creat playlistul: "+newPlaylist.getName());

        PlaylistDAO playlistDAO = PlaylistDAO.getInstance(connection);
        playlistDAO.create(newPlaylist);

        System.out.println("Playlistul \"" + playlistName + "\" a fost creat cu succes!");
    }
}
