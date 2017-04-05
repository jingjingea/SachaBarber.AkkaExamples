package sample.cluster.transformation.frontend

import language.postfixOps
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import akka.cluster.client.ClusterClientReceptionist



object TransformationFrontendApp {

  def main(args: Array[String]): Unit = {

    // Override the configuration of the port when specified as program argument
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val frontend = system.actorOf(Props[TransformationFrontend], name = "frontend")
    // ClusterClient들이 접속 할 수 있도록 Cluster 중의 하나인 ClusterClientReceptionist을 생성 해 둔다.
    ClusterClientReceptionist(system).registerService(frontend)
  }

}