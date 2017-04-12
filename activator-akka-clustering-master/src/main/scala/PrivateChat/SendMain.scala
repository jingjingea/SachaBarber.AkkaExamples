package PrivateChat

import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import chat.{MemberListener, RandomUser}

/**
  * Created by hana on 2017-04-12.
  */
object SendMain {
  def main(args: Array[String]): Unit = {
    val systemName = "ChatApp"
    val system1: ActorSystem = ActorSystem(systemName)
    val joinAddress: Address = Cluster(system1).selfAddress
    println("## joinAddress : {}", joinAddress)
    Cluster(system1).join(joinAddress)
    system1.actorOf(Props[Destination], "destination")

    Thread.sleep(5000)
    val system2 = ActorSystem(systemName)
    Cluster(system2).join(joinAddress)
    system2.actorOf(Props[Destination], "destination")
    val sender = system2.actorOf(Props[Sender], "sender")
    Thread.sleep(1000)
    sender ! "hello1"
    sender ! "hello2"
    sender ! "hello3"
    sender ! "hello4"
    sender ! "hello5"
    sender ! "hello6"

    sender ! 10000

  }
}