package sample.cluster.transformation.frontend

import sample.cluster.transformation.{TransformationResult, BackendRegistration, JobFailed, TransformationJob}
import language.postfixOps
import scala.concurrent.Future
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.pipe
import akka.pattern.ask


class TransformationFrontend extends Actor {

  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0

  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      println(s"## Frontend saw empty TransformationJob : '$job'")
      sender() ! JobFailed("Service unavailable, try again later", job)

    case job: TransformationJob =>
      println(s"## Frontend saw TransformationJob : '$job'")
      jobCounter += 1
      implicit val timeout = Timeout(5 seconds)
      val result  = (backends(jobCounter % backends.size) ? job)
        .map(x => x.asInstanceOf[TransformationResult])
      result pipeTo sender
      //pipe(result) to sender

    case BackendRegistration if !backends.contains(sender()) =>
      println(s"## receive BackendRegistration : $sender")
      context watch sender() // "backend sender"를 감시 한다. sender가 죽을 경우 "Terminated" 이 actor context에 전달 한다.
      backends = backends :+ sender() // cluster에 접속 한 backend들 정보를 축적

    case Terminated(a) =>
      println(s"## receive Terminated : $a")
      backends = backends.filterNot(_ == a) // 제거
  }
}
