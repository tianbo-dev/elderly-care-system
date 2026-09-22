package com.lekang.elderlycare.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lekang.elderlycare.entity.Alarm;
import com.lekang.elderlycare.entity.Device;
import com.lekang.elderlycare.entity.Resident;
import com.lekang.elderlycare.mapper.AlarmMapper;
import com.lekang.elderlycare.mapper.DeviceMapper;
import com.lekang.elderlycare.mapper.ResidentMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MqttSubscriber implements CommandLineRunner {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.qos}")
    private int qos;

    private final DeviceMapper deviceMapper;
    private final ResidentMapper residentMapper;
    private final AlarmMapper alarmMapper;
    private final ObjectMapper objectMapper;

    public MqttSubscriber(DeviceMapper deviceMapper, ResidentMapper residentMapper,
                          AlarmMapper alarmMapper, ObjectMapper objectMapper) {
        this.deviceMapper = deviceMapper;
        this.residentMapper = residentMapper;
        this.alarmMapper = alarmMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        connectAndSubscribe();
    }

    private void connectAndSubscribe() {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(60);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 连接断开，准备重连...", cause);
                    try {
                        Thread.sleep(3000);
                        connectAndSubscribe();
                    } catch (Exception e) {
                        log.error("MQTT 重连失败", e);
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            client.connect(options);
            client.subscribe(topic, qos);
            log.info("MQTT 订阅成功: topic={}", topic);
        } catch (Exception e) {
            log.error("MQTT 连接失败", e);
            log.info("请确认MQTT Broker已启动，或使用公共测试broker如 tcp://broker.emqx.io:1883");
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.info("收到MQTT消息: topic={}, payload={}", topic, payload);

            JsonNode json = objectMapper.readTree(payload);
            String deviceId = json.path("deviceId").asText();
            String alarmType = json.path("type").asText("FALL");
            long timestamp = json.path("timestamp").asLong(System.currentTimeMillis());

            Device device = deviceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Device>()
                            .eq(Device::getDeviceId, deviceId));

            if (device == null) {
                log.warn("未找到设备: {}", deviceId);
                return;
            }

            Alarm alarm = new Alarm();
            alarm.setDeviceId(deviceId);
            alarm.setAlarmType(alarmType);
            alarm.setAlarmTime(LocalDateTime.now());
            alarm.setStatus(0);

            if (device.getResidentId() != null) {
                alarm.setResidentId(device.getResidentId());
                Resident resident = residentMapper.selectById(device.getResidentId());
                if (resident != null) {
                    alarm.setResidentName(resident.getName());
                }
            }

            alarmMapper.insert(alarm);
            log.info("告警已入库: resident={}, type={}", alarm.getResidentName(), alarmType);
        } catch (Exception e) {
            log.error("处理MQTT消息失败", e);
        }
    }
}
