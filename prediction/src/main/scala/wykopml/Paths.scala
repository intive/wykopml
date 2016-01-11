package wykopml

case object Paths {

  trait BasePaths {
    def modelPath: String

    def userMappingsPath: String
  }

  case class BasePathsIn(basePath: String) extends BasePaths {
    def modelPath = s"${basePath}/model/"

    def userMappingsPath = s"${basePath}/users"
  }

  def apply(basePath: String): BasePaths = {
    BasePathsIn(basePath)
  }

}
