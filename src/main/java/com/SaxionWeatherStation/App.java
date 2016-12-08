package com.SaxionWeatherStation;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Hello world!
 *
 */
public class App {

    MqttClient client;
    String topic        = "test";
    String content      = "Message from MqttPublish";
    int qos             = 0;
    String broker       = "tcp://staging.thethingsnetwork.org:1883";
    String clientId     = "JavaSample";
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
                }

                //@Override
                public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete
                }
            });

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(false);
            connOpts.setUserName("70B3D57ED000192C");
            connOpts.setPassword("WTeciFoKOaDP2FmoxKTnyUOwxnee5kXirtxS6ekfn5w=".toCharArray());
            client.connect(connOpts);
            client.subscribe("test");
           //MqttMessage message = new MqttMessage(content.getBytes());
           //message.setQos(qos);
           //client.publish(topic, message);
            //System.out.println("Message published");
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

}
