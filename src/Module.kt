import java.nio.file.Path
import kotlin.io.path.name

interface Module {
  fun moduleRoot(root: Path): Path
  fun generateContentRoots(root: Path): List<String>
}

fun Module.imlFile(root: Path): Path {
  val moduleRoot = moduleRoot(root)
  return moduleRoot.resolve(moduleRoot.name + ".iml")
}
