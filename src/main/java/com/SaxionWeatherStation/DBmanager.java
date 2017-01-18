/**
 * Created by Dave Ouds & Rene Potgieter on 28-10-2016.
 */
package com.SaxionWeatherStation;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton class that manages the connection with database
 */
public class DBmanager {
    private static DBmanager uniqueInstance=null;
    private static Connection con = null ;

    /**
     * private constructor
     */
    private DBmanager(){
        if(!dbExists())
        {
            System.err.println("the database doesn’t exist....") ;
        } else {
            System.out.println("connected to the database");
        }
    }

    /**
     * returns the unique instance of the connection
     * @return unique instance
     */
    public static synchronized DBmanager getInstance() {
        if (uniqueInstance==null) {
            uniqueInstance = new DBmanager();
        }
        return uniqueInstance;
    }

    /**
     * checks if it is possible to connect to the database. If possible then makes a connection
     * @return true if connected to the database
     */
    private Boolean dbExists() {
        Boolean exists = false ;
        Boolean fileloaded = false;
        try {
            //define a Properties object to read in the properties from a properties file
            Properties props = new Properties();
            try {
                //read in the properties by using a FileInputStream and the Properties object
                //don’t forget to close your properties file
                FileInputStream in = new FileInputStream("database.properties");
                props.load(in);
                in.close();
                fileloaded = true;
            } catch (IOException ioex) {
                fileloaded = false;
                System.out.println("can't connect to the database");
            }
            if (fileloaded) {
                //read in your properties by using your properties object
                //make a connection
                String drivers = props.getProperty("jdbc.drivers");
                if (drivers != null) System.setProperty("jdbc.drivers", drivers);
                String url = props.getProperty("jdbc.url");
                String username = props.getProperty("jdbc.username");
                String password = props.getProperty("jdbc.password");
                con = DriverManager.getConnection(url, username,password);
                exists = true;
            }
        } catch (SQLException ex) {
        //handle SQLExeption
            ex.printStackTrace();
        }
        return(exists) ;
    }

    /**
     * closes the connection
     */
    public void close() {
        try {
            //close connection
            con.close();
            uniqueInstance=null;
            con=null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * return the connection
     * @return connection
     */
    public Connection getConnection() {
        return con;
    }
}