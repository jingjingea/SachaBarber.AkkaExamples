import akka.actor.{Actor, ActorPath}
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.pattern.Patterns
import sample.cluster.transformation.{TransformationJob, TransformationResult}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class ClientJobTransformationSendingActor extends Actor {


  // akka의 remote 방식이 tcp일 경우 akka.tcp://를 사용 하며 altery 방식일 경우 akka:// 방식을 사용

  val initialContacts = Set(
    ActorPath.fromString("akka.tcp://ClusterSystem@127.0.0.1:2551/system/receptionist"))


//  ActorPath.fromString("akka.tcp://thingplug@127.0.0.1:5150/system/receptionist")

//  val initialContacts = Set(
//    ActorPath.fromString("akka://thingplug@192.168.1.74:5150/system/receptionist"),
//    ActorPath.fromString("akka://thingplug@192.168.1.73:5150/system/receptionist"),
//    ActorPath.fromString("akka://thingplug@192.168.1.72:5150/system/receptionist")
//  )



  val settings = ClusterClientSettings(context.system)
    .withInitialContacts(initialContacts)

  val c = context.system.actorOf(ClusterClient.props(settings), "demo-client")


  def receive = {
    case TransformationResult(result) => {
      println("Client response")
      println(result)
    }
    case Send(counter) => {
        val job = TransformationJob("hello-" + counter)
        implicit val timeout = Timeout(5 seconds)
        val result = Patterns.ask(c,ClusterClient.Send("/user/frontend", job, localAffinity = true), timeout)

        result.onComplete {
          case Success(transformationResult) => {
            println(s"Client saw result: $transformationResult")
            self ! transformationResult
          }
          case Failure(t) => println("An error has occured: " + t.getMessage)
        }
      }
  }
}
