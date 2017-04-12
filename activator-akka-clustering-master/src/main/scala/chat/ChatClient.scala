package chat

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe}

object ChatClient {
  def props(name: String): Props = Props(classOf[ChatClient], name)

  case class Publish(msg: String)
  case class Message(from: String, text: String)
}

class ChatClient(name: String) extends Actor with ActorLogging{
  val mediator = DistributedPubSub(context.system).mediator
  val topic = "chatroom"
  mediator ! Subscribe(topic, self)
  log.info(s"## $name joined chat room")

  def receive = {
    case ChatClient.Publish(msg) =>
      log.info(s"## ChatClient.Publis : {}", msg)
      mediator ! Publish(topic, ChatClient.Message(name, msg))

    case ChatClient.Message(from, text) =>
      val direction = if (sender == self) ">>>>" else s"<< $from:"
      log.info(s"## $name $direction $text")
  }

}