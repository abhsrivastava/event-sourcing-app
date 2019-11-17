package util

import akka.actor.ActorSystem
import play.api.Configuration
import akka.stream.Materializer
import akka.kafka.ConsumerSettings
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import scala.concurrent.Future
import akka.stream.scaladsl.Sink

class ServiceKafkaConsumer(
    topicNames: Set[String], 
    groupName: String, 
    actorSystem: ActorSystem, 
    configuration: Configuration, 
    handleEvent: String => Unit, 
    implicit val mat: Materializer) {
    import configuration._

    val bootstrapServers = get[String]("kafka.bootstrap.servers")
    val offsetConfig = get[String]("kafka.auto.offset.reset")
    val consumerSettings = ConsumerSettings(actorSystem, new StringDeserializer, new StringDeserializer)
                            .withBootstrapServers(bootstrapServers)
                            .withGroupId(groupName)
                            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)

    Consumer.committableSource(consumerSettings, Subscriptions.topics(topicNames)).mapAsync(1) {msg => 
        val event = msg.record.value()
        handleEvent(event)
        Future.successful(msg)
    }.mapAsync(1) {msg => 
        msg.committableOffset.commitScaladsl()
    }.runWith(Sink.ignore)
}