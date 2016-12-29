#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_TSL2561_U.h>
#include <Adafruit_BMP085.h>
#include <dht.h>
#include <rn2xx3.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11); // RX, TX of rn2xx3

dht DHT;

#define DHT11_PIN 5

//Anemometer Defines
#define windPin A0
#define voltageConversionConstant .004882814 //This constant maps the value provided from the analog read function, which ranges from 0 to 1023, to actual voltage, which ranges from 0V to 5V
#define voltageMin .4 // Mininum output voltage from anemometer in mV.
#define windSpeedMin 0 // Wind speed in meters/sec corresponding to minimum voltage

#define voltageMax 2.0// Maximum output voltage from anemometer in mV.
#define windSpeedMax 32 // Wind speed in meters/sec corresponding to maximum voltage


/*Wiring
   BMP085
   Connect VCC of the BMP085 sensor to 3.3V (NOT 5.0V!)
   Connect GND to Ground
   Connect SCL to i2c clock - on '168/'328 Arduino Uno/Duemilanove/etc thats Analog 5
   Connect SDA to i2c data - on '168/'328 Arduino Uno/Duemilanove/etc thats Analog 4
   EOC is not used, it signifies an end of conversion
   XCLR is a reset pin, also not used here

   RN2xx3 -- Arduino
   Uart TX -- 10
   Uart RX -- 11
   Reset -- 12
   Vcc -- 3.3V
   Gnd -- Gnd
*/
Adafruit_TSL2561_Unified tsl = Adafruit_TSL2561_Unified(TSL2561_ADDR_FLOAT, 12345);
Adafruit_BMP085 bmp;
rn2xx3 myLora(mySerial);

void setup() {
  pinMode(13, OUTPUT);
  led_on();
  Serial.begin(57600);
  if (!bmp.begin()) {
    Serial.println("Could not find a valid BMP085 sensor, check wireing!");
    while (1) {}
  }
  if(!tsl.begin())
  {
    /* There was a problem detecting the TSL2561 ... check your connections */
    Serial.print("Ooops, no TSL2561 detected ... Check your wiring or I2C ADDR!");
    while(1);
  }
  mySerial.begin(9600);
  Serial.println("Startup lora");

  initialize_radio();

  //transmit a startup message("TTN Mapper on TTN Enschede node");
  myLora.tx("TTN Mapper on TTN Enschede node");

  /* Setup the sensor gain and integration time */
  configureSensor();
  
  led_off();
  delay(2000);
}

void configureSensor(void)
{
  /* You can also manually set the gain or enable auto-gain support */
  // tsl.setGain(TSL2561_GAIN_1X);      /* No gain ... use in bright light to avoid sensor saturation */
  // tsl.setGain(TSL2561_GAIN_16X);     /* 16x gain ... use in low light to boost sensitivity */
  tsl.enableAutoRange(true);            /* Auto-gain ... switches automatically between 1x and 16x */
  
  /* Changing the integration time gives you better sensor resolution (402ms = 16-bit data) */
  // tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_13MS);      /* fast but low resolution */
  // tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_101MS);  /* medium resolution and speed   */
   tsl.setIntegrationTime(TSL2561_INTEGRATIONTIME_402MS);  /* 16-bit data but slowest conversions */

  /* Update these values depending on what you've set above! */  
  Serial.println("------------------------------------");
  Serial.print  ("Gain:         "); Serial.println("Auto");
  Serial.print  ("Timing:       "); Serial.println("402 ms");
  Serial.println("------------------------------------");
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
  while (hweui.length() != 16)
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

  //OTAA: initOTAA(String AppEUI, String AppKey);
  join_result = myLora.initOTAA("70B3D57ED00018F6", "C3485F16C6EFFF94FE9B95AB8E7EDAAE");

  while (!join_result)
  {
    Serial.println("Unable to join. Are your keys correct, and do you have TTN coverage?");
    delay(60000); //delay a minute before retry
    Serial.println("Trying to join TTN");
    join_result = myLora.init();

  }
  Serial.println("Successfully joined TTN");

}

//Function that measures windspeed over 10 seconds and takes the average
int getWindSpeed() {
  int windSpeed = 0;
  for (byte i = 0; i < 20; i++) {
    int sensorVoltage = analogRead(A0) * voltageConversionConstant;
    if (sensorVoltage <= voltageMin) {//If sensor voltage is lower than the Min. output of the sensor the windspeed is 0
      windSpeed += 0;
    }
    else {
      windSpeed += (sensorVoltage - voltageMin) * windSpeedMax / (voltageMax - voltageMin);
    }
    Serial.println(windSpeed);
    delay(500);
  }
  windSpeed /= 20;
  return(windSpeed);
}

void loop() {
  led_on();
  /* Get a new Luxsensor event */ 
  sensors_event_t luxEvent;
  tsl.getEvent(&luxEvent);
  int windSpeed = getWindSpeed();
  int chk = DHT.read11(DHT11_PIN);
  String temp = String((bmp.readTemperature() + DHT.temperature) / 2);
  String pressure = String(bmp.readPressure());
  String humidity = String(DHT.humidity, 0);
  String lux = String(luxEvent.light);
  String sending = "/" + temp + "/" + pressure + "/" + humidity + "/" + windSpeed + "/" + lux;

  Serial.println(sending);
  Serial.println("TXing");
  myLora.tx(sending); //one byte, blocking function

  led_off();
  delay(1000);
}

void led_on() {
  digitalWrite(13, 1);
}
void led_off() {
  digitalWrite(13, 0);
}
