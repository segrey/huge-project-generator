import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText

class JavaModule(private val id: Int) : Module {
  override fun moduleRoot(root: Path): Path {
    return root.resolve("java-modules").resolve(bucketSubPath(id)).resolve(moduleName())
  }

  private fun moduleName(): String = "java-module-$id"

  private fun contentRoot(id: Int): String = "content-root-$id"

  override fun generateContentRoots(root: Path): List<String> {
    val xmlContentRoots = (1..CONTENT_ROOTS_PER_JAVA_MODULE).map {
      contentRootXml($$"file://$MODULE_DIR$/$${contentRoot(it)}")
    }
    val moduleRoot = moduleRoot(root)
    (1..CONTENT_ROOTS_PER_JAVA_MODULE).forEach {
      createContentRoot(root, moduleRoot, id, it)
    }
    return listOf(contentRootXml($$"file://$MODULE_DIR$")) + xmlContentRoots
  }

  private fun contentRootXml(contentRootUrl: String): String {
    return $$"""
      <content url="$$contentRootUrl">
        <sourceFolder url="$$contentRootUrl/resources" type="java-resource" />
        <sourceFolder url="$$contentRootUrl/src" isTestSource="false" />
        <sourceFolder url="$$contentRootUrl/test" isTestSource="true" />
        <sourceFolder url="$$contentRootUrl/testResources" type="java-test-resource" />
      </content>
          """
  }

  private fun createContentRoot(root: Path, moduleRoot: Path, moduleId: Int, perModuleContentRootId: Int) {
    val contentRoot = moduleRoot.resolve(contentRoot(perModuleContentRootId)).createDirectories()
    contentRoot.resolve("src").createDirectory()
    val contentRootId = (moduleId - 1) * CONTENT_ROOTS_PER_JAVA_MODULE + perModuleContentRootId
    val totalContentRoots = JAVA_MODULES * CONTENT_ROOTS_PER_JAVA_MODULE
    val step = (totalContentRoots - PACKAGE_JSON_COUNT) / (PACKAGE_JSON_COUNT + 1) + 1
    if (contentRootId % step == 0 && createdPackageJsonInfo.size < PACKAGE_JSON_COUNT) {
      writePackageJson(root, contentRoot)
    }
  }

  private fun writePackageJson(root: Path, contentRoot: Path) {
    val packageJson = contentRoot.resolve("package.json")
    packageJson.writeText("{}")
    createdPackageJsonInfo.add("#${createdPackageJsonInfo.size + 1} Created ${root.relativize(packageJson)}")
  }
}

val createdPackageJsonInfo: MutableList<String> = mutableListOf()
