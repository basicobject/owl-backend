package owl.common

trait OwlService {
  val service: String
  def run(): Unit

  type Host = String
  type Port = Int
}
