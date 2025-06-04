package Service;

import BazaDeDate.DAOs.SongDAO;
import BazaDeDate.DAOs.LikedSongsDAO;
import Model.Song;
import BazaDeDate.DAOs.DatabaseConnection;
import Model.User;
import Audit.Audit;


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SongService {

    private Clip clip;
    private Long currentFrame;
    private String status;
    private AudioInputStream audioInputStream;
    private String filePath;

    private final SongDAO songDAO;
    private final Scanner scanner;

    public SongService(java.sql.Connection connection) {
        this.songDAO = SongDAO.getInstance(connection);
        this.scanner = new Scanner(System.in);
    }


    public void playSong(Song song, User user) {
        if (user != null){
            Audit.logInAudit("Userul "+user.getUsername() + " asculta melodia: "+song.getTitle());
        }
        Connection connection = DatabaseConnection.getConnection();
        this.filePath = song.getPath();
        LikedSongsDAO likedDAO = null;
        if(user != null){
            likedDAO = new LikedSongsDAO(connection, user.getUsername());
        }
//        LikedSongsDAO likedDAO = new LikedSongsDAO(connection, user.getUsername());

        try {
            audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            play();

            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("\n--------- Now playing: \"" + song.getTitle() + "\" ---------");
                System.out.println("1. Pauza");
                System.out.println("2. Reluare");
                System.out.println("3. Restart");
                System.out.println("4. Stop");
                System.out.println("5. Skip 5s");
                boolean isLiked = false;
                if (user !=null){
                    isLiked = likedDAO.isLiked(song.getId());
                    if (isLiked) {
                        System.out.println("6. Unlike this song");
                    } else {
                        System.out.println("6. Like this song");
                    }
                }
                System.out.println("7. Vezi detalii");

                System.out.print("Optiune: ");

                int option;
                try {
                    option = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException ex) {
                    System.out.println("Optiune invalida.");
                    continue;
                }

                if (option == 4) {
                    stop();
                    break;
                }

                switch (option) {
                    case 1 -> pause();
                    case 2 -> resumeAudio();
                    case 3 -> restart();
                    case 5 -> {
                        long currentMicros = clip.getMicrosecondPosition();
                        long jumpTo = currentMicros + 5_000_000L;
                        if (jumpTo >= clip.getMicrosecondLength()) {
                            stop();
                        } else {
                            jump(jumpTo);
                        }
                    }
                    case 6 -> {
                        if (user != null){
                            stop(); // oprește redarea înainte de like/unlike
                            if (isLiked) {
                                likedDAO.removeLike(song.getId());
                                Audit.logInAudit("Userul "+user.getUsername() + " nu mai apreciaza: "+song.getTitle());
                                System.out.println("Unliked.");
                            } else {
                                likedDAO.addLike(song.getId());
                                System.out.println("Liked.");
                                Audit.logInAudit("Userul "+user.getUsername() + " apreciaza: "+song.getTitle());
                            }
                            return;
                        }

                    }
                    case 7 -> {
                        stop();
                        System.out.println("----------------------------DESPRE MELODIE------------------------------");
                        System.out.println("    Titlu: " + song.getTitle() );
                        System.out.println("    Artist: " + song.getArtist().getName());
                        System.out.println("    Inclusa in albumul: "+ song.getAlbum());
                        System.out.println("    Gen: " + song.getArtist().getGenre());
                        System.out.println("------------------------------------------------------------------------");
                    }
                    default -> System.out.println("Optiune invalida.");
                }
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Eroare la redarea fisierului audio: " + e.getMessage());
            Audit.logInAudit("Eroare la redarea fisierului audio: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void play() {
        clip.start();
        status = "play";
    }

    private void pause() {
        if ("paused".equals(status)) {
            System.out.println("Audio este deja in pauza.");
            return;
        }
        currentFrame = clip.getMicrosecondPosition();
        clip.stop();
        status = "paused";
    }

    private void resumeAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if ("play".equals(status)) {
            System.out.println("Audio-ul este deja in redare.");
            return;
        }
        clip.close();
        resetAudioStream();
        clip.setMicrosecondPosition(currentFrame);
        play();
    }

    private void restart() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        clip.stop();
        clip.close();
        resetAudioStream();
        currentFrame = 0L;
        clip.setMicrosecondPosition(0);
        play();
    }

    private void stop() {
        currentFrame = 0L;
        clip.stop();
        clip.close();
        status = "stop";
    }

    //skip 5 secunde - jump sare la poz specificata in microsec
    private void jump(long c) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (c > 0 && c < clip.getMicrosecondLength()) {
            clip.stop();
            clip.close();
            resetAudioStream();
            currentFrame = c;
            clip.setMicrosecondPosition(c);
            play();
        } else {
            System.out.println("Timpul introdus este în afara intervalului valid.");
        }
    }

    /**
     * Reîncarcă AudioInputStream și redeschide clip-ul folosind filePath memorat, astfel încât să poți repoziționa redarea la o nouă poziție.
     */
    private void resetAudioStream() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        audioInputStream = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void shuffleAndPlayAll(User user) {
        List<Song> allSongs = songDAO.readAll();
        if (allSongs.isEmpty()) {
            System.out.println("No songs found in database");
            return;
        }

        Collections.shuffle(allSongs, new Random());

        System.out.println("Incepe redarea in mod Shuffle. Conecteaza-te pentru mai multe optiuni!");

        Scanner sc = new Scanner(System.in);
        for (Song song : allSongs) {
            playSong(song, user);

            System.out.print("Meniu shuffle: \n");
            System.out.println("1. Iesi din shuffle");
            System.out.println("2. Next random song");
            String cmd = sc.nextLine().trim();
            if (cmd.equals("1")) {
                System.out.println("Shuffle oprit.");
                break;
            } else {
                continue;

            }
        }

    }

}
