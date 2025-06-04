//package BazaDeDate.DAOs;
//
//import Model.Band;
//import Model.SoloArtist;
//import Model.Enums.MusicGenre;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class BandDAO implements DAOInterface<Band> {
//    private static BandDAO instance;
//    private final Connection connection;
//
//    private BandDAO(Connection connection) {
//        this.connection = connection;
//    }
//
//    public static BandDAO getInstance(Connection connection) {
//        if (instance == null) {
//            instance = new BandDAO(connection);
//        }
//        return instance;
//    }
//
//    @Override
//    public void create(Band band) {
//        String sqlInsertBand = """
//            INSERT INTO band (name, genre, contact_email, description)
//            VALUES (?, ?, ?, ?)
//            """;
//        try (PreparedStatement ps = connection.prepareStatement(sqlInsertBand, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setString(1, band.getName());
//            ps.setString(2, band.getGenre());
//            ps.setString(3, band.getContactEmail());
//            ps.setString(4, band.getDescription());
//
//            int affected = ps.executeUpdate();
//            if (affected == 0) {
//                throw new SQLException("Creating Band failed, no rows affected.");
//            }
//            try (ResultSet keys = ps.getGeneratedKeys()) {
//                if (keys.next()) {
//                    band.setId(keys.getInt(1));
//                } else {
//                    throw new SQLException("Creating Band failed, no ID obtained.");
//                }
//            }
//
//            // Link members in band_member table
//            if (band.getMembers() != null) {
//                String sqlLink = "INSERT INTO band_member (band_id, solo_artist_id) VALUES (?, ?)";
//                try (PreparedStatement psLink = connection.prepareStatement(sqlLink)) {
//                    for (SoloArtist member : band.getMembers()) {
//                        psLink.setInt(1, band.getId());
//                        psLink.setInt(2, member.getId());
//                        psLink.addBatch();
//                    }
//                    psLink.executeBatch();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public Band read(int id) {
//        String sqlSelectBand = """
//            SELECT id, name, genre, contact_email, description
//            FROM band
//            WHERE id = ?
//            """;
//        String sqlSelectMembers = """
//            SELECT sa.id, sa.name, sa.genre, sa.contact_email, sa.description, sa.country
//            FROM solo_artist sa
//            JOIN band_member bm ON sa.id = bm.solo_artist_id
//            WHERE bm.band_id = ?
//            """;
//        try (PreparedStatement psBand = connection.prepareStatement(sqlSelectBand)) {
//            psBand.setInt(1, id);
//            try (ResultSet rsBand = psBand.executeQuery()) {
//                if (!rsBand.next()) {
//                    return null;
//                }
//                Band band = new Band(
//                        rsBand.getInt("id"),
//                        rsBand.getString("name"),
//                        MusicGenre.valueOf(rsBand.getString("genre")),
//                        rsBand.getString("contact_email"),
//                        new ArrayList<>(),
//                        rsBand.getString("description")
//                );
//
//                try (PreparedStatement psMembers = connection.prepareStatement(sqlSelectMembers)) {
//                    psMembers.setInt(1, id);
//                    try (ResultSet rsMem = psMembers.executeQuery()) {
//                        List<SoloArtist> members = new ArrayList<>();
//                        while (rsMem.next()) {
//                            SoloArtist sa = new SoloArtist(
//                                    rsMem.getInt("id"),
//                                    rsMem.getString("name"),
//                                    MusicGenre.valueOf(rsMem.getString("genre")),
//                                    rsMem.getString("contact_email"),
//                                    rsMem.getString("country"),
//                                    rsMem.getString("description")
//                            );
//                            members.add(sa);
//                        }
//                        band.setMembers(members);
//                    }
//                }
//                return band;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    public List<Band> readAll() {
//        List<Band> list = new ArrayList<>();
//        String sqlSelectAll = "SELECT id FROM band";
//        try (PreparedStatement ps = connection.prepareStatement(sqlSelectAll);
//             ResultSet rs = ps.executeQuery()) {
//            while (rs.next()) {
//                int bandId = rs.getInt("id");
//                Band band = read(bandId);
//                if (band != null) {
//                    list.add(band);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    @Override
//    public void update(Band band) {
//        String sqlUpdateBand = """
//            UPDATE band
//               SET name = ?,
//                   genre = ?,
//                   contact_email = ?,
//                   description = ?
//             WHERE id = ?
//            """;
//        try (PreparedStatement ps = connection.prepareStatement(sqlUpdateBand)) {
//            ps.setString(1, band.getName());
//            ps.setString(2, band.getGenre());
//            ps.setString(3, band.getContactEmail());
//            ps.setString(4, band.getDescription());
//            ps.setInt(5, band.getId());
//            ps.executeUpdate();
//
//            // Update members: delete existing links and re-insert
//            String sqlDeleteLinks = "DELETE FROM band_member WHERE band_id = ?";
//            try (PreparedStatement psDel = connection.prepareStatement(sqlDeleteLinks)) {
//                psDel.setInt(1, band.getId());
//                psDel.executeUpdate();
//            }
//
//            if (band.getMembers() != null) {
//                String sqlLink = "INSERT INTO band_member (band_id, solo_artist_id) VALUES (?, ?)";
//                try (PreparedStatement psLink = connection.prepareStatement(sqlLink)) {
//                    for (SoloArtist member : band.getMembers()) {
//                        psLink.setInt(1, band.getId());
//                        psLink.setInt(2, member.getId());
//                        psLink.addBatch();
//                    }
//                    psLink.executeBatch();
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void delete(int id) {
//        String sqlDeleteLinks = "DELETE FROM band_member WHERE band_id = ?";
//        String sqlDeleteBand = "DELETE FROM band WHERE id = ?";
//        try (PreparedStatement psDelLinks = connection.prepareStatement(sqlDeleteLinks)) {
//            psDelLinks.setInt(1, id);
//            psDelLinks.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        try (PreparedStatement psDelBand = connection.prepareStatement(sqlDeleteBand)) {
//            psDelBand.setInt(1, id);
//            psDelBand.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}

package BazaDeDate.DAOs;

import Model.Band;
import Model.SoloArtist;
import Model.Enums.MusicGenre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BandDAO implements DAOInterface<Band> {
    private static BandDAO instance;
    private final Connection connection;

    private BandDAO(Connection connection) {
        this.connection = connection;
    }

    public static BandDAO getInstance(Connection connection) {
        if (instance == null) {
            instance = new BandDAO(connection);
        }
        return instance;
    }

    @Override
    public void create(Band band) {
        String sqlBand = """
            INSERT INTO band (name, genre, contact_email, description)
            VALUES (?, ?, ?, ?)
            """;
        try (PreparedStatement psBand = connection.prepareStatement(sqlBand, Statement.RETURN_GENERATED_KEYS)) {
            psBand.setString(1, band.getName());
            psBand.setString(2, band.getGenre());
            psBand.setString(3, band.getContactEmail());
            psBand.setString(4, band.getDescription());
            int affected = psBand.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creare Band eșuată, niciun rând afectat.");
            }
            try (ResultSet keys = psBand.getGeneratedKeys()) {
                if (keys.next()) {
                    band.setId(keys.getInt(1));
                } else {
                    throw new SQLException("Creare Band eșuată, nu s-a obținut ID.");
                }
            }
            // inserăm membrii în tabelul asociativ
            String sqlMember = "INSERT INTO band_member (band_id, solo_artist_id) VALUES (?, ?)";
            try (PreparedStatement psMember = connection.prepareStatement(sqlMember)) {
                for (SoloArtist member : band.getMembers()) {
                    psMember.setInt(1, band.getId());
                    psMember.setInt(2, member.getId());
                    psMember.addBatch();
                }
                psMember.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Band read(int id) {
        String sqlBand = """
            SELECT id, name, genre, contact_email, description
              FROM band
             WHERE id = ?
            """;
        try (PreparedStatement psBand = connection.prepareStatement(sqlBand)) {
            psBand.setInt(1, id);
            try (ResultSet rsBand = psBand.executeQuery()) {
                if (rsBand.next()) {
                    String name = rsBand.getString("name");
                    MusicGenre genre = MusicGenre.valueOf(rsBand.getString("genre"));
                    String contact = rsBand.getString("contact_email");
                    String desc = rsBand.getString("description");
                    // citim membrii din tabelul asociativ
                    String sqlMembers = "SELECT solo_artist_id FROM band_member WHERE band_id = ?";
                    List<SoloArtist> members = new ArrayList<>();
                    try (PreparedStatement psMem = connection.prepareStatement(sqlMembers)) {
                        psMem.setInt(1, id);
                        try (ResultSet rsMem = psMem.executeQuery()) {
                            while (rsMem.next()) {
                                int soloId = rsMem.getInt("solo_artist_id");
                                SoloArtist member = SoloArtistDAO.getInstance(connection).read(soloId);
                                if (member != null) {
                                    members.add(member);
                                }
                            }
                        }
                    }
                    return new Band(id, name, genre, contact, members, desc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Band> readAll() {
        List<Band> list = new ArrayList<>();
        String sqlAll = "SELECT id FROM band";
        try (PreparedStatement psAll = connection.prepareStatement(sqlAll);
             ResultSet rsAll = psAll.executeQuery()) {
            while (rsAll.next()) {
                int id = rsAll.getInt("id");
                Band band = read(id);
                if (band != null) {
                    list.add(band);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Band band) {
        String sqlBand = """
            UPDATE band SET
                name = ?,
                genre = ?,
                contact_email = ?,
                description = ?
            WHERE id = ?
            """;
        try (PreparedStatement psBand = connection.prepareStatement(sqlBand)) {
            psBand.setString(1, band.getName());
            psBand.setString(2, band.getGenre());
            psBand.setString(3, band.getContactEmail());
            psBand.setString(4, band.getDescription());
            psBand.setInt(5, band.getId());
            psBand.executeUpdate();
            // actualizăm membrii: ștergem înregistrările vechi și reinserăm
            String sqlDelete = "DELETE FROM band_member WHERE band_id = ?";
            try (PreparedStatement psDel = connection.prepareStatement(sqlDelete)) {
                psDel.setInt(1, band.getId());
                psDel.executeUpdate();
            }
            String sqlInsert = "INSERT INTO band_member (band_id, solo_artist_id) VALUES (?, ?)";
            try (PreparedStatement psIns = connection.prepareStatement(sqlInsert)) {
                for (SoloArtist member : band.getMembers()) {
                    psIns.setInt(1, band.getId());
                    psIns.setInt(2, member.getId());
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {
        String sqlDeleteMembers = "DELETE FROM band_member WHERE band_id = ?";
        try (PreparedStatement psDelMem = connection.prepareStatement(sqlDeleteMembers)) {
            psDelMem.setInt(1, id);
            psDelMem.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sqlDeleteBand = "DELETE FROM band WHERE id = ?";
        try (PreparedStatement psDelBand = connection.prepareStatement(sqlDeleteBand)) {
            psDelBand.setInt(1, id);
            psDelBand.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMember(int bandId, int soloArtistId) {
        String sql = "INSERT INTO band_member (band_id, solo_artist_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bandId);
            ps.setInt(2, soloArtistId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMember(int bandId, int soloArtistId) {
        String sql = "DELETE FROM band_member WHERE band_id = ? AND solo_artist_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bandId);
            ps.setInt(2, soloArtistId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
