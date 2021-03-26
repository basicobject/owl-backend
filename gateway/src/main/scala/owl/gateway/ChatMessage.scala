package owl.gateway

import java.util.UUID

case class ChatMessage(messageId: UUID, text: String, to: Long)
