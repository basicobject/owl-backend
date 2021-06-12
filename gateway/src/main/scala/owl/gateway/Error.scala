package owl.gateway

sealed trait Error extends RuntimeException {
  def message: String = this.getClass.getSimpleName
}

class BadCommandError(msg: String) extends Error {
  override def message: String = s"${super.message} $msg"
}
