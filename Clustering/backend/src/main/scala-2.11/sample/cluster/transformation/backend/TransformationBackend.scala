package sample.cluster.transformation.backend

import sample.cluster.transformation.{BackendRegistration, TransformationResult, TransformationJob}
import language.postfixOps
import scala.concurrent.duration._
import akka.actor.Actor
import akka.actor.RootActorPath
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.MemberStatus


class TransformationBackend extends Actor {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case TransformationJob(text) => {
      println("## receive TransformationJob")
      val result = text.toUpperCase
      println(s"Backend has transformed the incoming job text of '$text' into '$result'")
      sender() ! TransformationResult(text.toUpperCase)
    }
    case state: CurrentClusterState =>
      println("## receive state")
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) =>
      // 처음 Actor 실 행시 같은 보인 포함 같은 Cluter의 Member정보가 전달 됨
      println(s"## receive MemberUp : $m")
      register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend")) {
      println(s"#run register :$member")
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
    }
}