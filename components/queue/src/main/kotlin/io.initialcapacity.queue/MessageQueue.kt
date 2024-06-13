package io.initialcapacity.queue

import io.initialcapacity.rabbitsupport.*
import java.net.URI

class MessageQueue(
        rabbitUrl: URI,
        exchangeName: String,
        queueName: String,
) {
    private val connectionFactory = buildConnectionFactory(rabbitUrl)
    private val exchange = RabbitExchange(
            name = exchangeName,
            type = "direct",
            routingKeyGenerator = { _: String -> "42" },
            bindingKey = "42",
    )
    private val queue = RabbitQueue(queueName)

    init {
        connectionFactory.declareAndBind(exchange, queue)
    }

    val publishMessage = publish(connectionFactory, exchange)

    fun listenToMessages(handler: suspend (String) -> Unit) {
        val channel = connectionFactory.newConnection().createChannel()
        listen(queue = queue, channel = channel) {
            handler(it)
        }
    }
}