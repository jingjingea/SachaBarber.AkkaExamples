package PrivateChat

import akka.actor.{Actor, ActorLogging}
import akka.cluster.pubsub.DistributedPubSub

/**
  * Created by hana on 2017-04-12.
  */
class Destination extends Actor with ActorLogging {
  import akka.cluster.pubsub.DistributedPubSubMediator.Put
  val mediator = DistributedPubSub(context.system).mediator
  // register to the path
  mediator ! Put(self)

  def receive = {
    case s: String ⇒
      log.info("Got {}", s)
  }
}