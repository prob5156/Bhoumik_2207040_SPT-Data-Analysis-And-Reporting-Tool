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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
