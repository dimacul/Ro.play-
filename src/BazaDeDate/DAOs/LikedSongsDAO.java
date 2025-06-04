package BazaDeDate.DAOs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Model.Song;

public class LikedSongsDAO {
    private final Connection connection;
    private final String tableName;

    public LikedSongsDAO(Connection connection, String username) {
        this.connection = connection;
        this.tableName = "likedSongs_" + username;
    }


    public void addLike(int songId) {
        String sql = "INSERT INTO `" + tableName + "` (song_id) VALUES (?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, songId);
            ps.executeUpdate();
        } catch (SQLException e) {

            if (!e.getSQLState().equals("23000")) {// daca exista deja, i.e. cheie primara apare duplicat => ignore report spam
                e.printStackTrace();
            }
        }
    }

    public void removeLike(int songId) {
        String sql = "DELETE FROM `" + tableName + "` WHERE song_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, songId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Song> readAllLiked() {
        List<Song> result = new ArrayList<>();
        String sql = "SELECT s.id, s.title, s.artist_id, s.album, s.path "
                + "FROM `" + tableName + "` ls "
                + "JOIN song s ON ls.song_id = s.id "
                + "ORDER BY ls.added_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Song s = new Song(
                        rs.getInt("id"),
                        rs.getString("title"),
                        null,                  // Artist se poate încărca cu SongDAO.read(s.getId()) dacă e necesar
                        rs.getString("album"),
                        rs.getString("path")
                );
                result.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

//    public boolean isLiked(int songId) {
//        String sql = "SELECT 1 FROM `" + tableName + "` WHERE song_id = ? LIMIT 1";
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setInt(1, songId);
//            ResultSet rs = ps.executeQuery();
//            return rs.next();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
}

