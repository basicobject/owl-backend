package owl.session

import scala.concurrent.Future

class SessionMapper {
  val sessionMap = scala.collection.mutable.Map.empty[String, Session]

  def addSession(session: Session): Future[Unit] = {
    Future.successful(sessionMap.addOne(session.userId -> session))
  }

  def removeSession(userId: String): Future[Unit] = {
    Future.successful(sessionMap.remove(userId))
  }

  def getSession(userId: String): Future[Option[Session]] = {
    Future.successful {
      sessionMap.get(userId)
    }
  }
}
