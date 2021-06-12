package owl.messaging

class Messenger {
  val channels = scala.collection.mutable.Map.empty[String, Channel]
}
