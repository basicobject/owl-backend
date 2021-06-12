package owl.common

trait OwlService {
  val serviceName: String
  def run(): Unit

  type Host = String
  type Port = Int
}
