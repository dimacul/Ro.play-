package BazaDeDate.DAOs;

import Model.Artist;
import Model.Band;
import Model.SoloArtist;
import Model.Song;
import Model.Enums.MusicGenre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongDAO implements DAOInterface<Song> {
    private static SongDAO instance;
    private final Connection connection;

    private SongDAO(Connection connection) {
        this.connection = connection;
    }

    public static SongDAO getInstance(Connection connection) {
        if (instance == null) {
            instance = new SongDAO(connection);
        }
        return instance;
    }

    @Override
    public void create(Song song) {
        String sql = """
            INSERT INTO song (title, artist_id, artist_type, album, path)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, song.getTitle());

            // aflăm ce tip de Artist avem și setăm artist_id + artist_type
            Artist artist = song.getArtist();
            if (artist instanceof SoloArtist) {
                ps.setInt(2, artist.getId());
                ps.setString(3, "SOLO");
            } else if (artist instanceof Band) {
                ps.setInt(2, artist.getId());
                ps.setString(3, "BAND");
            } else {
                throw new SQLException("Artist necunoscut la salvare: " + artist.getClass().getName());
            }

            ps.setString(4, song.getAlbum());
            ps.setString(5, song.getPath());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creare Song failed, nu s-a inserat niciun row");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    song.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creare Song a esuat -> nu s-a obtinut ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Song read(int id) {
        String sql = """
            SELECT id, title, artist_id, artist_type, album, path
              FROM song
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String title = rs.getString("title");
                int artistId = rs.getInt("artist_id");
                String artistType = rs.getString("artist_type");
                String album = rs.getString("album");
                String path = rs.getString("path");

                // artist band sau solo?
                Artist artist = null;
                if ("SOLO".equals(artistType)) {
                    SoloArtistDAO soloDao = SoloArtistDAO.getInstance(connection);
                    artist = soloDao.read(artistId);
                } else if ("BAND".equals(artistType)) {
                    BandDAO bandDao = BandDAO.getInstance(connection);
                    artist = bandDao.read(artistId);
                }
                if (artist == null) {
                    throw new SQLException("Artistul cu ID " + artistId + ", tip:" + artistType + " nu a fost gasit.");
                }

                Song song = new Song(id, title, artist, album, path);
                return song;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Song> readAll() {
        List<Song> list = new ArrayList<>();
        String sql = "SELECT id FROM song";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int songId = rs.getInt("id");
                Song song = read(songId);
                if (song != null) {
                    list.add(song);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Song song) {
        String sql = """
            UPDATE song
               SET title = ?,
                   artist_id = ?,
                   artist_type = ?,
                   album = ?,
                   path = ?
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, song.getTitle());

            Artist artist = song.getArtist();
            if (artist instanceof SoloArtist) {
                ps.setInt(2, artist.getId());
                ps.setString(3, "SOLO");
            } else if (artist instanceof Band) {
                ps.setInt(2, artist.getId());
                ps.setString(3, "BAND");
            } else {
                throw new SQLException("Artist necunoscut la update: " + artist.getClass().getName());
            }

            ps.setString(4, song.getAlbum());
            ps.setString(5, song.getPath());
            ps.setInt(6, song.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM song WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
