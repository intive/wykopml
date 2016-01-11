package wykopml.storage

import com.websudos.phantom.connectors.ContactPoints

object Defaults {
  val hosts = Seq("127.0.0.1")
  val connector = ContactPoints(hosts).keySpace("wykop")
}
