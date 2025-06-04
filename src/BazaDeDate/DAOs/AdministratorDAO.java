package BazaDeDate.DAOs;

import Model.Administrator;
import Model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministratorDAO implements DAOInterface<Administrator> {
    private static AdministratorDAO instance;
    private final Connection connection;

    private AdministratorDAO(Connection connection) {
        this.connection = connection;
    }

    public static AdministratorDAO getInstance(Connection connection) {
        if (instance == null) {
            instance = new AdministratorDAO(connection);
        }
        return instance;
    }

    public Administrator findByUsername(String username) {
        String sql = """
            SELECT id, username, email, password, parola_admin
              FROM administrator
             WHERE username = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Administrator a = new Administrator(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("parola_admin")
                    );
                    return a;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }
    @Override
    public void create(Administrator admin) {
        String sql = """
            INSERT INTO administrator
               (username, email, password, parola_admin)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPassword());
            ps.setString(4, admin.getParolaAdmin());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creare Administrator eșuată, niciun rând afectat.");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    admin.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creare Administrator eșuată, nu s-a obținut ID.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Administrator read(int id) {
        String sql = """
            SELECT id, username, email, password, parola_admin
              FROM administrator
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Administrator(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("parola_admin")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Administrator> readAll() {
        List<Administrator> list = new ArrayList<>();
        String sql = """
            SELECT id, username, email, password, parola_admin
              FROM administrator
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Administrator admin = new Administrator(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("parola_admin")
                );
                list.add(admin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Administrator admin) {
        String sql = """
            UPDATE administrator
               SET username    = ?,
                   email       = ?,
                   password    = ?,
                   parola_admin = ?
             WHERE id = ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPassword());
            ps.setString(4, admin.getParolaAdmin());
            ps.setInt(5, admin.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM administrator WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
