package owl.gateway

sealed trait Error extends RuntimeException {
  def message: String = this.getClass.getSimpleName
}

class InvalidChatMessageError extends Error
