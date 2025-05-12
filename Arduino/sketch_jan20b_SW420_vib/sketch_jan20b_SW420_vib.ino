/*//==============================================================================//
 * SW-420 Vibration Sensor Module Interfacing with Arduino
 * Author: Microcontrollerslab.com
 */ //=============================================================================//
#include <Arduino.h>
#include <stdio.h>

//define on/off logic symbols with name ON and OFF
#define ON HIGH
#define OFF LOW

#define Sensor_Out_Pin A5
#define LED_Pin  13
 
int present_condition = 0;
int previous_condition = 0;

void setup() {
pinMode(Sensor_Out_Pin, INPUT);
pinMode(LED_Pin, OUTPUT);
}
 

void LED_Pin_blink(void);
 
void loop() {
previous_condition = present_condition;
present_condition = digitalRead(Sensor_Out_Pin); // Reading digital data from the A5 Pin of the Arduino.
 
if (previous_condition != present_condition) {
LED_Pin_blink();
 
} else {
digitalWrite(LED_Pin, OFF);
}
}
 
void LED_Pin_blink(void) {
digitalWrite(LED_Pin, ON);
delay(250);
digitalWrite(LED_Pin, OFF);
delay(250);
digitalWrite(LED_Pin, ON);
delay(250);
digitalWrite(LED_Pin, OFF);
delay(250);
}
