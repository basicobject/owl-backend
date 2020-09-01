object SessionMap {
  case class Session(userId: Long)
  private var sessions: Map[Long, Session] = Map.empty[Long, Session]

  def findOrCreate(userId: Long): Session =
    sessions.getOrElse(userId, create(userId))

  private def create(userId: Long): Session = {
    val session = Session(userId)
    sessions += (userId -> session)
    session
  }
}
