// 模拟跌倒监测设备 - 通过 MQTT 发布跌倒事件
// 相当于 MQTTX 客户端的作用
const mqtt = require('mqtt')

// 使用公共 MQTT Broker（与后端配置一致）
const brokerUrl = 'mqtt://broker.emqx.io:1883'
const deviceId = process.argv[2] || 'FALL-001'

const client = mqtt.connect(brokerUrl, {
    clientId: `lekang-device-sim-${Date.now()}`,
    username: 'public',
    password: 'public',
    clean: true
})

client.on('connect', () => {
    console.log(`[模拟设备] 已连接到 MQTT Broker: ${brokerUrl}`)
    console.log(`[模拟设备] 设备ID: ${deviceId}`)

    const topic = 'device/fall/event'
    const payload = {
        deviceId: deviceId,
        type: 'FALL',
        timestamp: Date.now(),
        location: '房间内',
        severity: 'HIGH'
    }

    client.publish(topic, JSON.stringify(payload), { qos: 1 }, (err) => {
        if (err) {
            console.error('[模拟设备] 发布失败:', err)
        } else {
            console.log(`[模拟设备] 跌倒事件已发布!`)
            console.log(`  Topic: ${topic}`)
            console.log(`  Payload: ${JSON.stringify(payload)}`)
            console.log(`  时间: ${new Date(payload.timestamp).toLocaleString('zh-CN')}`)
        }
        client.end()
        setTimeout(() => process.exit(0), 500)
    })
})

client.on('error', (err) => {
    console.error('[模拟设备] 连接错误:', err.message)
    process.exit(1)
})
