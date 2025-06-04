// 2. PlaylistDAO.java

package BazaDeDate.DAOs;

import Model.Playlist;
import Model.Song;
import Model.User;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

public class PlaylistDAO implements DAOInterface<Playlist> {
    private static PlaylistDAO instance;
    private final Connection connection;

    private PlaylistDAO(Connection connection) {
        this.connection = connection;
    }

    public static PlaylistDAO getInstance(Connection connection) {
        if (instance == null) {
            instance = new PlaylistDAO(connection);
        }
        return instance;
    }

    public void addSongToPlaylist(int playlistId, int songId) {
        String sql = "INSERT INTO playlist_song (playlist_id, song_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void removeSongFromPlaylist(int playlistId, int songId) { //se sterge doar inreg. (playlist_id, song_id) din playlist_song
        String sql = "DELETE FROM playlist_song WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            ps.setInt(2, songId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void create(Playlist playlist) {
        // inserez doar playlist, fara id si melodii -> melodiile sunt stocate cu aj unui tabel asociativ
        String sqlInsertPlaylist = """
            INSERT INTO playlist (name, owner_id)
            VALUES (?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sqlInsertPlaylist, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playlist.getName());
            ps.setInt(2, playlist.getOwner().getId());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Nu s-a creat playlistul -0 randuri afectate");
            }
            // citesc id ul care s-a atribuit playlistului dupa insertul anterior
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    playlist.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Nu s-a obtinut id playlist - nu s-a creat playlist-ul.");
                }
            }
            // trec melodiile din LinkedHashSet<Song> in playlist_song (le leg cu playlistul caruia apartin) //M-M
            if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
                String sqlLink = """
                    INSERT INTO playlist_song (playlist_id, song_id)
                    VALUES (?, ?)
                    """;
                try (PreparedStatement psLink = connection.prepareStatement(sqlLink)) {
                    for (Song song : playlist.getSongs()) {
                        psLink.setInt(1, playlist.getId());
                        psLink.setInt(2, song.getId());
                        psLink.addBatch();
                    }
                    psLink.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Playlist read(int id) {
        String sqlSelectPlaylist = """
            SELECT id, name, owner_id
              FROM playlist
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sqlSelectPlaylist)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String name = rs.getString("name");
                int ownerId = rs.getInt("owner_id");

                // citire owner
                UserDAO userDao = UserDAO.getInstance(connection);
                User owner = userDao.read(ownerId);
                if (owner == null) {
                    throw new SQLException("Owner cu ID " + ownerId + " nu a fost găsit.");
                }

                // adaug in playlist in ordinea adaugarii lor (ord descresc dupa dded_at)
                String sqlSelectSongs = """
                    SELECT ps.song_id
                      FROM playlist_song ps
                     WHERE ps.playlist_id = ?
                     ORDER BY ps.added_at desc
                    """;
                LinkedHashSet<Song> songs = new LinkedHashSet<>();
                try (PreparedStatement psSongs = connection.prepareStatement(sqlSelectSongs)) {
                    psSongs.setInt(1, id);
                    try (ResultSet rsSongs = psSongs.executeQuery()) {
                        SongDAO songDao = SongDAO.getInstance(connection);
                        while (rsSongs.next()) {
                            int songId = rsSongs.getInt("song_id");
                            Song song = songDao.read(songId);
                            if (song != null) {
                                songs.add(song);
                            }
                        }
                    }
                }
                return new Playlist(id, name, songs, owner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Playlist> readAll() {
        List<Playlist> list = new ArrayList<>();

        String sqlSelectAll = "SELECT id FROM playlist";
        try (PreparedStatement ps = connection.prepareStatement(sqlSelectAll);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int playlistId = rs.getInt("id");
                Playlist pl = read(playlistId);
                if (pl != null) {
                    list.add(pl);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Playlist> readAllByUser(int userId) {
        List<Playlist> list = new ArrayList<>();

        String sqlSelectAllByUser = "SELECT id FROM playlist WHERE owner_id = ? order by created_at desc";
        try (PreparedStatement ps = connection.prepareStatement(sqlSelectAllByUser)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int playlistId = rs.getInt("id");
                    Playlist pl = read(playlistId);
                    if (pl != null) {
                        list.add(pl);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Playlist playlist) {
        String sqlUpdate = """
            UPDATE playlist
               SET name = ?, owner_id = ?
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sqlUpdate)) {
            ps.setString(1, playlist.getName());
            ps.setInt(2, playlist.getOwner().getId());
            ps.setInt(3, playlist.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }


        String sqlDeleteLinks = "DELETE FROM playlist_song WHERE playlist_id = ?";
        try (PreparedStatement psDel = connection.prepareStatement(sqlDeleteLinks)) {
            psDel.setInt(1, playlist.getId());
            psDel.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (playlist.getSongs() != null && !playlist.getSongs().isEmpty()) {
            String sqlLink = """
                INSERT INTO playlist_song (playlist_id, song_id)
                VALUES (?, ?)
                """;
            try (PreparedStatement psLink = connection.prepareStatement(sqlLink)) {
                for (Song song : playlist.getSongs()) {
                    psLink.setInt(1, playlist.getId());
                    psLink.setInt(2, song.getId());
                    psLink.addBatch();
                }
                psLink.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(int id) {
        // daca sterg playlistul se sterg si intrarile din playlist_Song
        String sqlDeletePlaylist = "DELETE FROM playlist WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sqlDeletePlaylist)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

