#include <Wire.h>
#include <Adafruit_BMP085.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_TSL2561_U.h>
#include <dht.h>
#include <rn2xx3.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11); // RX, TX of rn2xx3

Adafruit_BMP085 bmp;
Adafruit_TSL2561_Unified tsl = Adafruit_TSL2561_Unified(TSL2561_ADDR_FLOAT, 12345);
dht DHT;
rn2xx3 myLora(mySerial);

#define DHT11_PIN 5

//Anemometer Defines
#define windPin A0
#define voltageConversionConstant .004882814 //This constant maps the value provided from the analog read function, which ranges from 0 to 1023, to actual voltage, which ranges from 0V to 5V
#define voltageMin .4 // Mininum output voltage from anemometer in mV.
#define windSpeedMin 0 // Wind speed in meters/sec corresponding to minimum voltage

#define voltageMax 2.0// Maximum output voltage from anemometer in mV.
#define windSpeedMax 32 // Wind speed in meters/sec corresponding to maximum voltage


void setup() {
  Serial.begin(57600);
  pinMode(13, OUTPUT);
  ledOn();
  //Looks if bmp085 is connected
  if(!bmp.begin()){
    Serial.println(F("Could not find a valid BMP085 sensor"));
    while(1){}
  }
  //Looks if TSL2561 is connected
  if(!tsl.begin()){
    Serial.println(F("Could not find a valid TSL2561 sensor"));
    while(1){}
  }
  configureSensor();

  mySerial.begin(9600);
  Serial.println("Startup Lora");
  initialize_radio();
  myLora.tx("Arduino Connected");
  

  ledOff();
}

void configureSensor(void){
  /* You can also manually set the gain or enable auto-gain support */
  // tsl.setGain(TSL2561_GAIN_1X);      /* No gain ... use in bright light to avoid sensor saturation */
  // tsl.setGain(TSL2561_GAIN_16X);     /* 16x gain ... use in low light to boost sensitivity */
  tsl.enableAutoRange(true);            /* Auto-gain ... switches automatically between 1x and 16x */
  
  /* Changing the integration time gives you better sensor resolution (402ms = 16-bit data) */
  tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_13MS);      /* fast but low resolution */
  // tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_101MS);  /* medium resolution and speed   */
  // tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_402MS);  /* 16-bit data but slowest conversions */
}

int getWindSpeed() {
  int windSpeed = 0;
  for (byte i = 0; i < 20; i++) {
    float sensorVoltage = analogRead(A0) * voltageConversionConstant;
    if (sensorVoltage <= voltageMin) {//If sensor voltage is lower than the Min. output of the sensor the windspeed is 0
      windSpeed += 0;
    }
    else {
      windSpeed += (sensorVoltage - voltageMin) * windSpeedMax / (voltageMax - voltageMin);
    }
    delay(500);
  }
  windSpeed /= 20;
  return(windSpeed);
}

void initialize_radio()
{
  //reset rn2483
  pinMode(12, OUTPUT);
  digitalWrite(12, LOW);
  delay(500);
  digitalWrite(12, HIGH);

  delay(100); //wait for the RN2xx3's startup message
  mySerial.flush();

  //Autobaud the rn2483 module to 9600. The default would otherwise be 57600.
  myLora.autobaud();

  //check communication with radio
  String hweui = myLora.hweui();
  while(hweui.length() != 16)
  {
    Serial.println("Communication with RN2xx3 unsuccesful. Power cycle the board.");
    Serial.println(hweui);
    delay(10000);
    hweui = myLora.hweui();
  }

  //print out the HWEUI so that we can register it via ttnctl
  Serial.println("When using OTAA, register this DevEUI: ");
  Serial.println(myLora.hweui());
  Serial.println("RN2xx3 firmware version:");
  Serial.println(myLora.sysver());

  //configure your keys and join the network
  Serial.println("Trying to join TTN");
  bool join_result = false;
  
  //ABP: initABP(String addr, String AppSKey, String NwkSKey);
  //join_result = myLora.initABP("02017201", "8D7FFEF938589D95AAD928C2E2E7E48F", "AE17E567AECC8787F749A62F5541D522");
  
  //OTAA: initOTAA(String AppEUI, String AppKey);
  join_result = myLora.initOTAA("70B3D57ED00018F6", "C3485F16C6EFFF94FE9B95AB8E7EDAAE");

  while(!join_result)
  {
    Serial.println("Unable to join. Are your keys correct, and do you have TTN coverage?");
    delay(60000); //delay a minute before retry
    join_result = myLora.init();
  }
  Serial.println("Successfully joined TTN");
  
}


void loop() {
  int readDHT = DHT.read11(DHT11_PIN);//Reads DHT sensor
  sensors_event_t event;
  tsl.getEvent(&event);//Reads TSL sensor
  String temperature = String((bmp.readTemperature() + DHT.temperature) / 2);
  Serial.println(temperature);
  
  String humidity = String(DHT.humidity);
  Serial.println(humidity);

  String pressure = String(bmp.readPressure());
  Serial.println(pressure);

  String lux = String(event.light);
  Serial.println(lux);

  String windspeed = String(getWindSpeed());
  Serial.println(windspeed);
  ledOn();
  myLora.tx("t" + temperature);
  delay(2000);
  myLora.tx("h" + humidity);
  delay(2000);
  myLora.tx("p" + pressure);
  delay(2000);
  myLora.tx("l" + lux);
  delay(2000);
  myLora.tx("w" + windspeed);
  delay(2000);
  myLora.tx("!");
  ledOff();
  delay(60000);
  Serial.println("Done Sending");
  
}

void ledOn(){
  digitalWrite(13, HIGH);
}

void ledOff(){
  digitalWrite(13, LOW);
}

