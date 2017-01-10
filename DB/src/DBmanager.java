/**
 * Created by Dave Ouds & Rene Potgieter on 28-10-2016.
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBmanager {
    private static DBmanager uniqueInstance=null;
    private static Connection con = null ;

    private DBmanager(){
        if(!dbExists())
        {
            System.err.println("the database doesn’t exist....") ;
        }
    }


    public static synchronized DBmanager getInstance() {
        if (uniqueInstance==null) {
            uniqueInstance = new DBmanager();
        }
        return uniqueInstance;
    }


    private Boolean dbExists() {
        Boolean exists = false ;
        Boolean fileloaded = false;
        try {
//            define a Properties object to read in the properties from a properties file
            Properties props = new Properties();
            try {
//                read in the properties by using a FileInputStream and the Properties object
//                don’t forget to close your properties file
                FileInputStream in = new FileInputStream("src/database.properties");
                props.load(in);
                in.close();
                fileloaded = true;
            }
            catch (IOException ioex) {
                System.out.println("&quot;IO Exception:&quot; + ioex.getMessage()");
                fileloaded = false;
            }
            if (fileloaded) {
//                read in your properties by using your properties object
//                make a connection
                String drivers = props.getProperty("jdbc.drivers");
                if (drivers != null) System.setProperty("jdbc.drivers", drivers);
                String url = props.getProperty("jdbc.url");
                String username = props.getProperty("jdbc.username");
                String password = props.getProperty("jdbc.password");
                con = DriverManager.getConnection(url, username,password);
                exists = true;
            }
        }
        catch (SQLException ex) {
//             handle SQLExeption
        }
        return(exists) ;
    }


    public void close() {
        try {
            //close connection
            con.close();
            uniqueInstance=null;
            con=null;
        } catch (SQLException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return con;
    }
}