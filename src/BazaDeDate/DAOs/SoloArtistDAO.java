package BazaDeDate.DAOs;

import Model.SoloArtist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Model.Enums.MusicGenre;


public class SoloArtistDAO implements DAOInterface<SoloArtist> {
    private static SoloArtistDAO instance;
    private final Connection connection;

    private SoloArtistDAO(Connection connection) {
        this.connection = connection;
    }

    public static SoloArtistDAO getInstance(Connection connection) {
        if (instance == null) {
            instance = new SoloArtistDAO(connection);
        }
        return instance;
    }

    @Override
    public void create(SoloArtist soloArtist) {
        String sql = """
            INSERT INTO solo_artist (name, genre, contact_email, description, country) VALUES (?, ?, ?, ?, ?)
                    """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, soloArtist.getName());
            // getGenre() returns the enum’s name as String
            ps.setString(2, soloArtist.getGenre());
            ps.setString(3, soloArtist.getContactEmail());
            ps.setString(4, soloArtist.getDescription());
            ps.setString(5, soloArtist.getCountry());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating SoloArtist failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    // set the auto-generated id back into the object
                    soloArtist.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creating SoloArtist failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SoloArtist read(int id) {
        String sql = """
        SELECT id, name, genre, contact_email, description, country
        FROM solo_artist
        WHERE id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SoloArtist(
                            rs.getInt("id"),
                            rs.getString("name"),
                            MusicGenre.valueOf(rs.getString("genre")),
                            rs.getString("contact_email"),
                            rs.getString("country"),
                            rs.getString("description")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // or throw an exception if preferred
    }

    @Override
    public List<SoloArtist> readAll() {
        List<SoloArtist> list = new ArrayList<>();
        String sql = """
        SELECT id, name, genre, contact_email, description, country
        FROM solo_artist
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SoloArtist sa = new SoloArtist(
                        rs.getInt("id"),
                        rs.getString("name"),
                        MusicGenre.valueOf(rs.getString("genre")),
                        rs.getString("contact_email"),
                        rs.getString("country"),
                        rs.getString("description")
                );
                list.add(sa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(SoloArtist soloArtist) {
        String sql = """
        UPDATE solo_artist
           SET name = ?,
               genre = ?,
               contact_email = ?,
               description = ?,
               country = ?
         WHERE id = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, soloArtist.getName());
            ps.setString(2, soloArtist.getGenre());
            ps.setString(3, soloArtist.getContactEmail());
            ps.setString(4, soloArtist.getDescription());
            ps.setString(5, soloArtist.getCountry());
            ps.setInt(6, soloArtist.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM solo_artist WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

