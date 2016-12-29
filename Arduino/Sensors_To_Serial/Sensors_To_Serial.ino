#include <Wire.h>
#include <Adafruit_BMP085.h>
#include <dht.h>

dht DHT;

#define DHT11_PIN 5
// Connect VCC of the BMP085 sensor to 3.3V (NOT 5.0V!)
// Connect GND to Ground
// Connect SCL to i2c clock - on '168/'328 Arduino Uno/Duemilanove/etc thats Analog 5
// Connect SDA to i2c data - on '168/'328 Arduino Uno/Duemilanove/etc thats Analog 4
// EOC is not used, it signifies an end of conversion
// XCLR is a reset pin, also not used here
Adafruit_BMP085 bmp;

//Anemometer Defines
#define windPin A0
#define voltageConversionConstant .004882814 //This constant maps the value provided from the analog read function, which ranges from 0 to 1023, to actual voltage, which ranges from 0V to 5V
#define voltageMin .4 // Mininum output voltage from anemometer in mV.
#define windSpeedMin 0 // Wind speed in meters/sec corresponding to minimum voltage

#define voltageMax 2.0// Maximum output voltage from anemometer in mV.
#define windSpeedMax 32 // Wind speed in meters/sec corresponding to maximum voltage


void setup() {
  Serial.begin(9600);
  if (!bmp.begin()) {
    Serial.println("Could not find a valid BMP085 sensor, check wireing!");
    while (1) {}
  }
}

int getWindSpeed() {
  int windSpeed = 0;
  for (byte i = 0; i < 20; i++) {
    int sensorVoltage = analogRead(A0) * voltageConversionConstant;
    Serial.print("sensorV: ");
    Serial.println(analogRead(A0));
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
  Serial.println();
  return(windSpeed);
}

void loop() {
  Serial.print("BMP Temperature = ");//BMP085 is not the best way of temperature measuring since it's accuracy is 2*C although it does output decimals
  Serial.print(bmp.readTemperature());
  Serial.println(" C");

  Serial.print("Pressure = ");
  Serial.print(bmp.readPressure());
  Serial.println(" Pa");

  int chk = DHT.read11(DHT11_PIN);//The DHT11 has an humidity accuracy of 5%
  Serial.print("Humidity = ");
  Serial.println(DHT.humidity, 1);

  Serial.print("DHT Temperature = ");//DHT11 sensor is rather bad for temperature measuring since it's accuracy is 2*C and doesn't output decimals
  Serial.println(DHT.temperature, 1);
  Serial.println();

  Serial.println("AVG Windspeed:");
  Serial.println(getWindSpeed());
  Serial.println();
  
  String temp = String((bmp.readTemperature() + DHT.temperature)/2);
  String pressure = String(bmp.readPressure());
  String humidity = String(DHT.humidity, 0);
  String sending = "t" + temp + "p" + pressure + "h" + humidity;
  Serial.println(sending);
  Serial.println(temp);
  delay(1000);
}
