package util

import akka.actor.ActorSystem
import play.api.Configuration
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.ProducerRecord

class ServiceKafkaProducer(
    topicName: String, 
    actorSystem: ActorSystem, 
    configuration: Configuration) {
        import configuration._
        val bootstrapServers = get[String]("kafka.bootstrap.servers")
        val producerSettings : ProducerSettings[String, String] = ProducerSettings(actorSystem, new StringSerializer(), new StringSerializer()).withBootstrapServers(bootstrapServers)
        val producer = producerSettings.createKafkaProducer()
        def send(logStringRecord: String) : Unit = {
            producer.send(new ProducerRecord(topicName, logStringRecord))
        }        
}