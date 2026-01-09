package com.example.sptdataanalysisandreportingtool;

import java.sql.*;

public class DBUtil {

    private static final String URL = "jdbc:sqlite:spt.db";

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection c = getConnection();
             Statement s = c.createStatement()) {

            s.execute("""
                CREATE TABLE IF NOT EXISTS raw_data(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                depth REAL,
                n1 INTEGER,
                n2 INTEGER,
                n3 INTEGER,
                comment TEXT
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS spt_data(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                borehole_id INTEGER,
                location_id INTEGER,
                depth REAL,
                n1 INTEGER,
                n2 INTEGER,
                n3 INTEGER,
                FOREIGN KEY(location_id) REFERENCES locations(id)
                )
            """);
            // add sample_code column if missing
            try {
                s.execute("ALTER TABLE spt_data ADD COLUMN sample_code TEXT DEFAULT ''");
            } catch (Exception ex) {
                // column already exists or other issue - ignore
            }

            s.execute("""
                CREATE TABLE IF NOT EXISTS clients(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                phone TEXT,
                password TEXT
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS locations(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                client_id INTEGER,
                location_name TEXT,
                bore_holes INTEGER,
                FOREIGN KEY(client_id) REFERENCES clients(id)
                )
            """);

            s.execute("""
                CREATE TABLE IF NOT EXISTS visual_classification(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                borehole_id INTEGER,
                location_id INTEGER,
                color_code TEXT,
                sand_percentage REAL,
                silt_percentage REAL,
                clay_percentage REAL,
                from_depth REAL,
                to_depth REAL,
                FOREIGN KEY(location_id) REFERENCES locations(id)
                )
            """);

            // Add password column to existing clients table if it doesn't exist
            try {
                s.execute("ALTER TABLE clients ADD COLUMN password TEXT DEFAULT ''");
            } catch (Exception e) {
                // Column already exists, ignore
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int insertClient(String name, String phone, String password) throws Exception {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO clients(name,phone,password) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS
             )) {
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, password);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static ResultSet fetchClients() throws Exception {
        Connection c = getConnection();
        return c.createStatement().executeQuery("SELECT * FROM clients");
    }

    public static int insertLocation(int clientId, String locationName, int holes) throws Exception {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "INSERT INTO locations(client_id,location_name,bore_holes) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS
             )) {
            p.setInt(1, clientId);
            p.setString(2, locationName);
            p.setInt(3, holes);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static ResultSet fetchLocationsByClient(int clientId) throws Exception {
        Connection c = getConnection();
        PreparedStatement p = c.prepareStatement("SELECT * FROM locations WHERE client_id=?");
        p.setInt(1, clientId);
        return p.executeQuery();
    }

    public static ResultSet fetchAll() throws Exception {
        Connection c = getConnection();
        return c.createStatement().executeQuery("SELECT * FROM raw_data");
    }

    public static void insert(double d,int n1,int n2,int n3,String cm) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "INSERT INTO raw_data(depth,n1,n2,n3,comment) VALUES(?,?,?,?,?)"
            )){
            p.setDouble(1,d);
            p.setInt(2,n1);
            p.setInt(3,n2);
            p.setInt(4,n3);
            p.setString(5,cm);
            p.execute();
        }
    }

    public static void update(int id,double d,int n1,int n2,int n3,String cm) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE raw_data SET depth=?,n1=?,n2=?,n3=?,comment=? WHERE id=?"
            )){
            p.setDouble(1,d);
            p.setInt(2,n1);
            p.setInt(3,n2);
            p.setInt(4,n3);
            p.setString(5,cm);
            p.setInt(6,id);
            p.execute();
        }
    }

    public static void delete(int id) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "DELETE FROM raw_data WHERE id=?"
            )){
            p.setInt(1,id);
            p.execute();
        }
    }

    public static void updateClient(int id, String name, String phone, String password) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE clients SET name=?, phone=?, password=? WHERE id=?"
            )){
            p.setString(1, name);
            p.setString(2, phone);
            p.setString(3, password);
            p.setInt(4, id);
            p.execute();
        }
    }

    public static void updateLocation(int id, String locationName, int holes) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE locations SET location_name=?, bore_holes=? WHERE id=?"
            )){
            p.setString(1, locationName);
            p.setInt(2, holes);
            p.setInt(3, id);
            p.execute();
        }
    }

    public static void deleteClient(int id) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "DELETE FROM clients WHERE id=?"
            )){
            p.setInt(1, id);
            p.execute();
        }
    }

    public static void deleteLocation(int id) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "DELETE FROM locations WHERE id=?"
            )){
            p.setInt(1, id);
            p.execute();
        }
    }

    // New methods for borehole-specific SPT data
    public static int insertSptData(int boreholeId, int locationId, String sampleCode, double depth, int n1, int n2, int n3) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "INSERT INTO spt_data(borehole_id,location_id,sample_code,depth,n1,n2,n3) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS
            )){
            p.setInt(1, boreholeId);
            p.setInt(2, locationId);
            p.setString(3, sampleCode);
            p.setDouble(4, depth);
            p.setInt(5, n1);
            p.setInt(6, n2);
            p.setInt(7, n3);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static ResultSet fetchSptDataByBorehole(int boreholeId, int locationId) throws Exception {
        Connection c = getConnection();
        PreparedStatement p = c.prepareStatement("SELECT * FROM spt_data WHERE borehole_id=? AND location_id=?");
        p.setInt(1, boreholeId);
        p.setInt(2, locationId);
        return p.executeQuery();
    }

    public static ResultSet fetchSptDataById(int id) throws Exception {
        Connection c = getConnection();
        PreparedStatement p = c.prepareStatement("SELECT * FROM spt_data WHERE id=?");
        p.setInt(1, id);
        return p.executeQuery();
    }

    public static void updateSptData(int id, String sampleCode, double depth, int n1, int n2, int n3) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE spt_data SET sample_code=?,depth=?,n1=?,n2=?,n3=? WHERE id=?"
            )){
            p.setString(1, sampleCode);
            p.setDouble(2, depth);
            p.setInt(3, n1);
            p.setInt(4, n2);
            p.setInt(5, n3);
            p.setInt(6, id);
            p.execute();
        }
    }

    public static void updateSptSampleCode(int id, String sampleCode) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE spt_data SET sample_code=? WHERE id=?"
            )){
            p.setString(1, sampleCode);
            p.setInt(2, id);
            p.execute();
        }
    }

    public static void deleteSptData(int id) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "DELETE FROM spt_data WHERE id=?"
            )){
            p.setInt(1, id);
            p.execute();
        }
    }

    // Visual Classification methods
    public static int insertVisualClassification(int boreholeId, int locationId, String colorCode, 
                                                   double sandPercentage, double siltPercentage, 
                                                   double clayPercentage, double fromDepth, double toDepth) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "INSERT INTO visual_classification(borehole_id,location_id,color_code,sand_percentage,silt_percentage,clay_percentage,from_depth,to_depth) VALUES(?,?,?,?,?,?,?,?)", 
                    Statement.RETURN_GENERATED_KEYS
            )){
            p.setInt(1, boreholeId);
            p.setInt(2, locationId);
            p.setString(3, colorCode);
            p.setDouble(4, sandPercentage);
            p.setDouble(5, siltPercentage);
            p.setDouble(6, clayPercentage);
            p.setDouble(7, fromDepth);
            p.setDouble(8, toDepth);
            p.executeUpdate();
            try (ResultSet rs = p.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static ResultSet fetchVisualClassificationByBorehole(int boreholeId, int locationId) throws Exception {
        Connection c = getConnection();
        PreparedStatement p = c.prepareStatement("SELECT * FROM visual_classification WHERE borehole_id=? AND location_id=?");
        p.setInt(1, boreholeId);
        p.setInt(2, locationId);
        return p.executeQuery();
    }

    public static void updateVisualClassification(int id, String colorCode, double sandPercentage, 
                                                    double siltPercentage, double clayPercentage, 
                                                    double fromDepth, double toDepth) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "UPDATE visual_classification SET color_code=?,sand_percentage=?,silt_percentage=?,clay_percentage=?,from_depth=?,to_depth=? WHERE id=?"
            )){
            p.setString(1, colorCode);
            p.setDouble(2, sandPercentage);
            p.setDouble(3, siltPercentage);
            p.setDouble(4, clayPercentage);
            p.setDouble(5, fromDepth);
            p.setDouble(6, toDepth);
            p.setInt(7, id);
            p.execute();
        }
    }

    public static void deleteVisualClassification(int id) throws Exception {
        try(Connection c=getConnection();
            PreparedStatement p=c.prepareStatement(
                    "DELETE FROM visual_classification WHERE id=?"
            )){
            p.setInt(1, id);
            p.execute();
        }
    }

    // Safely fetch a single visual classification that covers the given depth.
    // Returns null if none found. Uses try-with-resources so connections are closed.
    public static VisualClassification fetchVisualClassificationForDepth(int boreholeId, int locationId, double depth) throws Exception {
        try (Connection c = getConnection();
             PreparedStatement p = c.prepareStatement(
                     "SELECT * FROM visual_classification WHERE borehole_id=? AND location_id=? AND from_depth <= ? AND to_depth >= ? LIMIT 1"
             )) {
            p.setInt(1, boreholeId);
            p.setInt(2, locationId);
            p.setDouble(3, depth);
            p.setDouble(4, depth);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return new VisualClassification(
                            rs.getInt("id"),
                            rs.getString("color_code"),
                            rs.getDouble("sand_percentage"),
                            rs.getDouble("silt_percentage"),
                            rs.getDouble("clay_percentage"),
                            rs.getDouble("from_depth"),
                            rs.getDouble("to_depth")
                    );
                }
            }
        }
        return null;
    }

}