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
void setup() {
  Serial.begin(9600);
  if (!bmp.begin()) {
    Serial.println("Could not find a valid BMP085 sensor, check wireing!");
    while (1) {}
  }
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
  String temp = String((bmp.readTemperature() + DHT.temperature)/2);
  String pressure = String(bmp.readPressure());
  String humidity = String(DHT.humidity, 0);
  String sending = "t" + temp + "p" + pressure + "h" + humidity;
  Serial.println(sending);
  Serial.println(temp);
  delay(1000);
}
