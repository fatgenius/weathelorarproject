#include <Wire.h>
#include <Adafruit_BMP085.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_TSL2561_U.h>
#include <dht.h>


Adafruit_BMP085 bmp;
Adafruit_TSL2561_Unified tsl = Adafruit_TSL2561_Unified(TSL2561_ADDR_FLOAT, 12345);
dht DHT;

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

void loop() {
  int readDHT = DHT.read11(DHT11_PIN);//Reads DHT sensor
  sensors_event_t event;
  tsl.getEvent(&event);//Reads TSL sensor
  
  Serial.print("TempBMP: ");
  Serial.println(bmp.readTemperature());
  Serial.print("TempDHT: ");
  Serial.println(DHT.temperature);
  String temperature = String((bmp.readTemperature() + DHT.temperature) / 2);
  Serial.print("AVGTemp: ");
  Serial.println(temperature);
  
  Serial.print("Humidity: ");
  String humidity = String(DHT.humidity);
  Serial.println(humidity);

  Serial.print("Pressure: ");
  String pressure = String(bmp.readPressure());
  Serial.println(pressure);

  Serial.print("Lux: ");
  String lux = String(event.light);
  Serial.println(lux);

  Serial.print("Windspeed: ");
  String windspeed = String(getWindSpeed());
  Serial.println(windspeed);

  String sending = temperature + "/" + humidity + "/" + pressure + "/" + lux + "/" + windspeed;
  Serial.println(sending);
  Serial.println("-------------------");
  delay(500);
}

void ledOn(){
  digitalWrite(13, HIGH);
}

void ledOff(){
  digitalWrite(13, LOW);
}

