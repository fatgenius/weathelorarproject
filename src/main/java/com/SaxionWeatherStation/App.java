package com.SaxionWeatherStation;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

/**
 * This application uses eclipse paho client to receive data from broker.
 * Furthermore its saves the received data to database.
 * @see <a href="https://eclipse.org/paho/">https://eclipse.org/paho/</a>
 */
public class App {

    MqttClient client;
    String topic        = "+/devices/+/up";
    String broker       = "tcp://staging.thethingsnetwork.org:1883";
    String clientId     = "saxion_stationrtrggd";
    MemoryPersistence persistence = new MemoryPersistence();


    /**
     * default constructor+++
     */
    public App() {
    }

    public static void main(String[] args) {

        new App().receiveData();
    }

    /**
     * Creates new client and manages the data received by the client from broker
     */
    public void receiveData() {
        try {
            client = new MqttClient(broker, clientId, persistence);

            client.setCallback(new MqttCallback() {


                public void connectionLost(Throwable cause) {//Called when the client lost the connection to the broker
                    System.out.println("connection lost");
                    try {
                        connect();
                    }
                    catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("new" + message.toString());
                    JSONObject jsonObject = new JSONObject(message.toString());
                    if(jsonObject.has("fields")){
                        JSONObject jsonObject2 = jsonObject.getJSONObject("fields");
                        if(jsonObject2.has("message")){
                            String str = jsonObject2.getString("message");
                            System.out.println(str);
                            //String st = "445.8/33/44/5/54";
                            //System.out.println(st);
                            parseMessage(str);

                        } else {
                            // It doesn't exist, do nothing
                        }
                    } else {
                        // It doesn't exist, do nothing
                    }
                }

                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
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
        connOpts.setCleanSession(false);
        connOpts.setUserName("70B3D57ED00018F6");
        connOpts.setPassword("r7cAAHo0pY17udmgsvIP9HvL1mlmCbzh9kWbOQGKVLs=".toCharArray());
        client.connect(connOpts);
        System.out.println("connected to: " + broker);
        client.subscribe(topic);
        System.out.println("subscribed to: " + topic);
      // String st = "445.8/33.00/44/5/54";
       // parseMessage(st);
    }

    /**
     * parses data from the received message. Futhermore calls method to insert parsed data into database
     * @param str strings that contains received message
     */
    private void parseMessage(String str) {
        String[] parameters = str.split("/");
        int numberOfParameters = 5;

        if ( isParsable(parameters, numberOfParameters) ) {
            float  temperature = Float.parseFloat(parameters[0]);
            int pressure = Integer.parseInt(parameters[1]);
            int humidity = Integer.parseInt(parameters[2]);
            int windspeed = Integer.parseInt(parameters[3]);
            int brightness = Integer.parseInt(parameters[4]);

            System.out.println( "Temperature: "+temperature +"\n" +"Pressure: "+pressure +"\n"
                    +"Humidity: "+humidity +"\n" +"Windspeed: "+windspeed +"\n" +"Brightness:"+brightness);
            //futhermore insert into database
        }
    }

    /**
     * checks if the received message is in proper format
     * @param parameters array of parameters extracted from recived message (temp, humidity ...)
     * @param numberOfParameters expected number of extracted parameters
     * @return return true if the recived message is in valid format and it can parsed and added to database
     */
    private boolean isParsable(String [] parameters, int numberOfParameters) {
        if (parameters.length == numberOfParameters) {

            if (    isFloat(parameters[0]) &&
                    isInteger(parameters[1]) &&
                    isInteger(parameters[2]) &&
                    isInteger(parameters[3])  &&
                    isInteger(parameters[4]) )
            {
                return true;
            }
            else {
                System.out.println("invalid data format");
                return false;
            }
        }

        else {
            System.out.println("invalid data format");
            return false;
        }
    }

    /**
     * Checks if string can be parsed to integer number
     * @param str string to be checked
     * @return returns true if string can be parsed to integer
     */
    private static boolean isInteger(String str)
    {
        try
        {
            int i = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    /**
     * Checks if string can be parsed to float number
     * @param str string to be checked
     * @return returns true if string can be parsed to integer
     */
    private static boolean isFloat(String str)
    {
        try
        {
            float f = Float.parseFloat(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
