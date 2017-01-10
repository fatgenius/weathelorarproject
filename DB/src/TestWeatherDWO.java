/**
 * Created by Dave Ouds& Rene Potgieter on 28-10-2016.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class TestWeatherDWO {
    public static void main(String[] args) {
        DBmanager dbcon = null;
        Connection conn = null;
        try {
            //make a connection with the database
            dbcon = DBmanager.getInstance();
            conn = dbcon.getConnection();

            Statement stmt = conn.createStatement();
            Calendar cal = Calendar.getInstance();

            ResultSet rs = stmt.executeQuery("SELECT country.name FROM country;"); // SQL statement to push the data in the database.

            //make a Statement object
            // execute the sqlstatement the result is resultset

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            dbcon.close();
        }
    }
}




