package com.SaxionWeatherStation;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

/**
 * Hello world!
 *
 */
public class App {

    MqttClient client;
    String topic        = "+/devices/+/up";
    //int qos             = 0;
    String broker       = "tcp://staging.thethingsnetwork.org:1883";
    String clientId     = "saxion_station";
    MemoryPersistence persistence = new MemoryPersistence();

    public App() {
    }

    public static void main(String[] args) {
        new App().doDemo();
    }

    public void doDemo() {
        try {
            client = new MqttClient(broker, clientId, persistence);

            client.setCallback(new MqttCallback() {

                //@Override
                public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker
                }

                //@Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("new" + message.toString());
                    JSONObject jsonObject = new JSONObject(message.toString());
                    if(jsonObject.has("fields")){
                        JSONObject jsonObject2 = jsonObject.getJSONObject("fields");
                        if(jsonObject2.has("message")){
                            String str = jsonObject2.getString("message");
                            System.out.println(str);
                            dataToDatabase(str);

                        } else {
                            // It doesn't exist, do nothing
                        }
                    } else {
                        // It doesn't exist, do nothing
                    }
                    //check if needed field exists
                    //get value of the field
                }

                //@Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setUserName("70B3D57ED00018F6");
            connOpts.setPassword("r7cAAHo0pY17udmgsvIP9HvL1mlmCbzh9kWbOQGKVLs=".toCharArray());
            client.connect(connOpts);
            client.subscribe(topic);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void dataToDatabase(String str) {
        String[] parts = str.split("/");
        String part1 = parts[0];
        String part2 = parts[1];
        String part3 = parts[2];
        String part4 = parts[3];
        System.out.println(part1);
        System.out.println(part2);
        System.out.println(part3);
        System.out.println(part4);
    }
}
