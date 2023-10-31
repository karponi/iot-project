package org.example;

import org.eclipse.paho.client.mqttv3.MqttException;

public class App {

    public static void main(String[] args) throws MqttException {
        var sensorSimulator = new SensorSimulator();

        while(true) {
            double temperature = sensorSimulator.getTemperatureReading();
            double moistureLevel = sensorSimulator.getMoistureReading();

            System.out.printf("Reading: Moisture: %.2f, Temperature: %.2f°C%n", moistureLevel, temperature);

            sensorSimulator.publish(moistureLevel, temperature);

            try {
                // Simulate periodic readings every 2 seconds
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
