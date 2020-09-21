package messaging

case class Session(userId: Long,
                   nickname: String,
                   state: String = Session.ACTIVE)

object Session {
  final val ACTIVE = "ACTIVE"
  final val INACTIVE = "INACTIVE"
}
