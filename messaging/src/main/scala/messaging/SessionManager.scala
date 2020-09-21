package messaging

object SessionManager {
  private var sessions: Map[Long, Session] = Map.empty[Long, Session]

  def getSessions: Seq[Session] = sessions.values.toSeq

  def find(userId: Long): Option[Session] = sessions.get(userId)

  def findOrCreate(nickname: String): Session = {
    val userId = generateId(nickname)
    sessions.getOrElse(userId, create(userId, nickname))
  }

  def update(session: Session): Unit =
    sessions = sessions.updated(session.userId, session)

  def authenticate(sessionId: Long): Option[Session] = find(sessionId)

  private def create(userId: Long, nickname: String): Session = {
    val session = Session(userId, nickname)
    sessions += (userId -> session)
    session
  }

  private def generateId(str: String): Long =
    scala.util.hashing.MurmurHash3.stringHash(str) & Long.MaxValue
}
