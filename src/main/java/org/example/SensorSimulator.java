package org.example;
import java.util.Random;
import org.eclipse.paho.client.mqttv3.*;

public class SensorSimulator {

    private static final double MIN_MOISTURE = 0.0;
    private static final double MAX_MOISTURE = 100.0;
    private static final double MIN_TEMPERATURE = 20.0;
    private static final double MAX_TEMPERATURE = 45.0;
    private static final double MAX_TEMPERATURE_FLUCTUATION = 5.0; // Maximum temperature fluctuation per reading

    private static final String WATERING_TOPIC = "plant/watering";
    private static final String SENSORS_TOPIC = "plant/sensors";

    private double currentMoisture;
    private double currentTemperature;
    private final Random random;

    private MqttClient mqttClient;

    public SensorSimulator() throws MqttException {
        random = new Random();
        currentMoisture = MAX_MOISTURE;
        currentTemperature = MIN_TEMPERATURE + (MAX_TEMPERATURE - MIN_TEMPERATURE) * random.nextDouble();
        setupMqttClient();
    }


    private void setupMqttClient() throws MqttException {
        mqttClient = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if (topic.equals(WATERING_TOPIC)) {
                    // Assuming the message payload contains the watering duration in seconds
                    int wateringDuration = Integer.parseInt(new String(mqttMessage.getPayload()));
                    System.out.println("Watering for " + wateringDuration + " seconds");
                    simulateWatering(wateringDuration);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
        });
        mqttClient.connect();
        mqttClient.subscribe(WATERING_TOPIC);
    }

    public double getMoistureReading() {
       double temperatureDecreaseRate;

        // Determine the temperature-based decrease rate
        if (currentTemperature > 35.0) {
            temperatureDecreaseRate = 0.08; // High rate for very high temperatures
        } else if (currentTemperature > 20.0) {
            temperatureDecreaseRate = 0.05; // Medium rate for average temperatures
        } else {
            temperatureDecreaseRate = 0.04; // Small rate for lower temperatures
        }

        // Calculate the moisture decrease based on the chosen rate
        double moistureDecrease = currentMoisture * temperatureDecreaseRate;

        // Calculate the new moisture level
        double newMoisture = currentMoisture - moistureDecrease;

        currentMoisture = newMoisture;
        newMoisture = Math.max(MIN_MOISTURE, Math.min(newMoisture, MAX_MOISTURE));

        return newMoisture;
    }

    // Simulate temperature reading with random fluctuations
    public double getTemperatureReading() {
        double temperatureIncrement = -MAX_TEMPERATURE_FLUCTUATION + 2 * MAX_TEMPERATURE_FLUCTUATION * random.nextDouble();
        currentTemperature = Math.max(Math.min(currentTemperature + temperatureIncrement, MAX_TEMPERATURE), MIN_TEMPERATURE);
        return currentTemperature;
    }

    // Simulate the effect of watering the plant by increasing moisture level
    private void simulateWatering(int wateringDurationInSeconds) {
        // Increment moisture by the watering duration
        double moistureIncrement = wateringDurationInSeconds / 5.0;
        currentMoisture = Math.min(currentMoisture + moistureIncrement, MAX_MOISTURE);
    }

    public void publish(double moistureLevel, double temperature) throws MqttException {
        var msg = "{\"moisture\":" + moistureLevel + ", \"temperature\":" + temperature + "}";
        this.mqttClient.publish(SENSORS_TOPIC, new MqttMessage(msg.getBytes()));
    }
}
