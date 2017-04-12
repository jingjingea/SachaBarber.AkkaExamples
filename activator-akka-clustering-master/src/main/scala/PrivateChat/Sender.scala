package PrivateChat

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub

/**
  * Created by hana on 2017-04-12.
  */
class Sender extends Actor with ActorLogging{
  import akka.cluster.pubsub.DistributedPubSubMediator.Send
  import akka.cluster.pubsub.DistributedPubSubMediator.SendToAll
  // activate the extension
  val mediator = DistributedPubSub(context.system).mediator

  def receive = {
    case in: String ⇒
      log.info("test String")
      val out = in.toUpperCase
      mediator ! Send(path = "/user/destination", msg = out, localAffinity = true)
    case in : Int =>
      log.info("test Int")
      mediator ! SendToAll(path = "/user/destination", msg = in.toString)
  }
}