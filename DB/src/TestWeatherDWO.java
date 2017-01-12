/**
 * Created by Dave Ouds& Rene Potgieter on 28-10-2016.
 */

import sun.java2d.pipe.SpanShapeRenderer;

import java.sql.*;
import java.text.SimpleDateFormat;
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
            SimpleDateFormat sdft = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String time = sdft.format(cal.getTime());


            ResultSet rs = stmt.executeQuery("insert into Datas (Times,Temperature, Air_Pressure,Humidity,Windspeed,Brightness) values ('" +time +"','"+temperature +"','" +pressure +"','" +humidity +"','" +windspeed +"','"+brightness +"');"); // SQL statement to push the data in the database.

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




