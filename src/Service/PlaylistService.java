package Service;

import BazaDeDate.DAOs.PlaylistDAO;
import BazaDeDate.DAOs.SongDAO;
import Model.Playlist;
import Model.Song;

import java.sql.Connection;

public class PlaylistService {
    private final PlaylistDAO playlistDAO;
    private final SongDAO songDAO;

    public PlaylistService(Connection connection) {
        this.playlistDAO = PlaylistDAO.getInstance(connection);
        this.songDAO     = SongDAO.getInstance(connection);
    }

    //functie de add melodie la playlist:
    // - se aduaga o melodie dupa id in playslist (identificat tot dupa id)
    // - daca e deja adaugata => nu o mai adaug

    public void addSongToPlaylist(int playlistId, int songId) {
        Playlist pl = playlistDAO.read(playlistId);
        if (pl == null) {
            System.out.println("Playlist-ul cu IDul " + playlistId + " nu exista");
            return;
        }
        Song s = songDAO.read(songId);
        if (s == null) {
            System.out.println("Melodia cu ID " + songId + " nu exista!!!");
            return;
        }
        //exista deja in acest playlist?

        if (pl.getSongs().stream().anyMatch(song -> song.getId() == songId)) {
            System.out.println("Melodia exista deja in playlist.");
            return;
        }
        //insert in playlist_song pt noul cantec adaugat; coloana added_at = NOW() default
        playlistDAO.addSongToPlaylist(playlistId, songId);
        System.out.println("Song: " + s.getTitle() + " added to playlist.");
    }

    public void removeSongFromPlaylist(int playlistId, int songId) {
        Playlist pl = playlistDAO.read(playlistId);
        if (pl == null) {
            System.out.println("Playlist-ul cu IDul " + playlistId + " nu exista");
            return;
        }
        if (!(pl.getSongs().stream().anyMatch(song -> song.getId() == songId)))
        {
            System.out.println("Melodia nu a fost gasita in playlist.");
            return;
        }
        playlistDAO.removeSongFromPlaylist(playlistId, songId);
        System.out.println("Song removed from playlist.");
    }

}

