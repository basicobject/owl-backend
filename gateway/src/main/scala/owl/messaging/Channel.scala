package owl.messaging

case class Channel(name: String, subscriptions: Seq[User])
