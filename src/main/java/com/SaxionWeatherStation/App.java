package com.SaxionWeatherStation;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This application uses eclipse paho client to receive data from broker.
 * Furthermore its saves the received data to database.
 * @see <a href="https://eclipse.org/paho/">https://eclipse.org/paho/</a>
 */
public class App {
    private MqttClient client;
    private String topic        = "+/devices/+/up";
    private String broker       = "tcp://staging.thethingsnetwork.org:1883";
    private String clientId     = "saxion_statio";
    private MemoryPersistence persistence = new MemoryPersistence();

    //attributes that store weather data
    private static float temperature;
    private static float humidity;
    private static int pressure;
    private static float brightness;
    private static int windSpeed;

    //true if parameter has been initialized
    private static boolean isInitTemperature = false;
    private static boolean isInitHumidity = false;
    private static boolean isInitPressure = false;
    private static boolean isInitBrightness = false;
    private static boolean isInitWindSpeed = false;


    /**
     * default constructor
     */
    public App() {
    }

    public static void main(String[] args) {
        new App().receiveData();
    }

    /**
     * Creates new client and manages the data received by the client from broker
     */
    private void receiveData() {
        try {
            client = new MqttClient(broker, clientId, persistence);

            client.setCallback(new MqttCallback() {

                //Called when the client lost the connection to the broker
                public void connectionLost(Throwable cause) {
                    System.out.println("connection lost");
                    try {
                        connect();
                    }
                    catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                //Called when the client receives the message from the broker
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("new" + message.toString());
                    JSONObject jsonObject = new JSONObject(message.toString());
                    if(jsonObject.has("fields")) {
                        JSONObject jsonObject2 = jsonObject.getJSONObject("fields");
                        if (jsonObject2.has("message")) {
                            String str = jsonObject2.getString("message");
                            System.out.println(str);
                            parseMessage(str);

                        }
                    }
                }

                //Called when a outgoing publish is complete
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            connect();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up connection options, connects to broker and subscribes to topic
     * @throws MqttException
     */
    private void connect() throws MqttException {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName("70B3D57ED00018F6");
        connOpts.setPassword("r7cAAHo0pY17udmgsvIP9HvL1mlmCbzh9kWbOQGKVLs=".toCharArray());
        client.connect(connOpts);
        System.out.println("connected to: " + broker);
        client.subscribe(topic);
        System.out.println("subscribed to: " + topic);
    }

    /**
     * parses data from the received message. Futhermore calls method to insert parsed data into database
     * @param str strings that contains received message
     * @return debug String for unit test
     */
    static String parseMessage(String str) {
        String id;
        String value;
        id = str.substring(0, 1);
        value = str.substring(1);

        switch (id) {
            case "t":
                if (isFloat(value)) {
                    temperature = Float.parseFloat(value);
                    System.out.println("temperature: " + temperature);
                    isInitTemperature = true;
                } else {
                    System.out.println("invalid string format");
                    return "invalid string format";
                }
                break;
            case "h":
                if (isFloat(value)) {
                    humidity = Float.parseFloat(value);
                    System.out.println("humidity: " + humidity);
                    isInitHumidity = true;
                } else {
                    System.out.println("invalid string format");
                    return "invalid string format";
                }
                break;
            case "p":
                if (isInteger(value)) {
                    pressure = Integer.parseInt(value);
                    System.out.println("pressure: " + pressure);
                    isInitPressure = true;
                } else {
                    System.out.println("invalid string format");
                    return "invalid string format";
                }
                break;
            case "l":
                if (isFloat(value)) {
                    brightness = Float.parseFloat(value);
                    System.out.println("brightness: " + brightness);
                    isInitBrightness = true;
                } else {
                    System.out.println("invalid string format");
                    return "invalid string format";
                }
                break;
            case "w":
                if (isInteger(value)) {
                    windSpeed = Integer.parseInt(value);
                    System.out.println("wind speed: " + windSpeed);
                    isInitWindSpeed = true;
                } else {
                    System.out.println("invalid string format");
                    return "invalid string format";
                }
                break;
            case "!":
                if(isInitTemperature && isInitHumidity && isInitPressure && isInitBrightness && isInitWindSpeed) {
                    insertIntoDB(temperature, pressure, humidity, windSpeed, brightness);
                    System.out.println("data added to database");
                } else {
                    System.out.println("data initialization error");
                }
                break;
            default:
                System.out.println("invalid string format");
                //for test
                return "invalid string format";
        }
        // for test
        return value;
    }

    /**
     * Checks if string can be parsed to integer number
     * @param str string to be checked
     * @return returns true if string can be parsed to integer
     */
    static boolean isInteger(String str) {
        try {
            int i = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Checks if string can be parsed to float number
     * @param str string to be checked
     * @return returns true if string can be parsed to integer
     */
    static boolean isFloat(String str) {
        try {
            float f = Float.parseFloat(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Inserts received data in to database
     * @param temperature air temperature
     * @param pressure air pressure
     * @param humidity humidity
     * @param windspeed wind speed
     * @param brightness brightness
     */
    private static void insertIntoDB(float temperature, int pressure, float humidity, int windspeed, float brightness) {
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

            stmt.executeUpdate("insert into Datas (Times,Temperature, Air_Pressure,Humidity,Windspeed,Brightness) " +
                    "values ('" +time +"','"+temperature +"','" +pressure +"','" +humidity +"','"
                    +windspeed +"','"+brightness +"');"); // SQL statement to push the data in the database.

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(dbcon != null) {
                dbcon.close();
            } else {
                System.out.println("no connection");
            }
        }
    }
}
