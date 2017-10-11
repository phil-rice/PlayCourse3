package services

case class HostAndPorts(vogueHostAndPort: String = "http://localhost", billboardHostAndPort: String = "http://localhost", fnordHostAndPort: String = "http://localhost")

object HostAndPorts {
  implicit val localhosts = HostAndPorts()
}