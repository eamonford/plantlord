package com.dionysus.irrigator
import com.beust.klaxon.Klaxon
import com.dionysus.common.domain.Reading
import com.dionysus.irrigator.dao.RuleRepository
import com.dionysus.irrigator.domain.IrrigationCommand
import com.dionysus.common.domain.Rule
import com.github.michaelbull.result.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

data class IrrigationCommandBuilder(val rule: Rule? = null, val reading: Reading? = null, val duration: Int? = null)

class IrrigationController(val ruleRepository: RuleRepository,
                           val mqttUrl: String,
                           val sourceTopic: String,
                           val sinkTopic: String,
                           val clientId: String) : MqttCallback {

    val LitersPerSecond = 0.001

    private val mqttClient: MqttClient = MqttClient(mqttUrl, clientId)

    init {
        mqttClient.connect()
        if (mqttClient.isConnected) {
            println("Connected to MQTT")
            mqttClient.subscribe(sourceTopic)
            mqttClient.setCallback(this)
        } else println("Connection to MQTT failed")
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        println("a message has arrived")
        Klaxon().parse<Reading>(message.toString())
                .toResultOr { println("failure!"); Exception("failed to parse reading") }
                .map { reading -> println("okay, parsed"); IrrigationCommandBuilder(reading = reading) }
                .flatMap {  commandBuilder ->
                    println("trying the repo")
                    ruleRepository
                            .getBySensorId(commandBuilder.reading!!.deviceId)
                            .map { commandBuilder.copy(rule = it) }
                }
                .flatMap { commandBuilder ->
                    println("calculating")
                    calculateDuration(commandBuilder, LitersPerSecond).map { commandBuilder.copy(duration = it) }
                }
                .map { IrrigationCommand(it.rule!!.valveId, it.duration!!) }
                .flatMap { maybeSendIrrigationCommand(it, mqttClient) }
                .mapBoth(
                        { success -> {  println("Dispatched command")} },
                        { failure -> { println("it failed") } }
                )
        println("done")
    }

    fun calculateDuration(irrigationCommandBuilder: IrrigationCommandBuilder, litersPerSecond: Double): Result<Int, Throwable> =
            when {
                irrigationCommandBuilder.rule == null || irrigationCommandBuilder.reading == null ->
                    Err(Exception("Either the rule or reading is missing from the com.dionysus.irrigator.main.com.dionysus.irrigator.IrrigationCommandBuilder object!"))
                irrigationCommandBuilder.reading.value > irrigationCommandBuilder.rule.threshold ->
                    Ok(0)
                else ->
                    Ok(((irrigationCommandBuilder.rule.target - irrigationCommandBuilder.reading.value)
                            / irrigationCommandBuilder.rule.moistureToLitersRatio
                            / litersPerSecond).toInt())

            }

    fun maybeSendIrrigationCommand(command: IrrigationCommand, client: MqttClient): Result<String?, Exception> =
            Result.of {
                when {
                    command.duration > 0 -> {
                        val commandJson = Klaxon().toJsonString(command)
                        client.publish(sinkTopic, MqttMessage(commandJson.toByteArray()))
                        commandJson
                    }
                    else -> null
                }
            }

    override fun connectionLost(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}