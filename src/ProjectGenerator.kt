import java.nio.file.Path
import kotlin.io.path.*

const val JAVA_MODULES: Int = 10
const val WEB_MODULES: Int = 5
const val CONTENT_ROOTS_PER_JAVA_MODULE: Int = 2
const val PACKAGE_JSON_COUNT = 1

class ProjectGenerator(val root: Path) {

  fun generate() {
    val modules = listOf(RootModule()) +
      (1..JAVA_MODULES).map { JavaModule(it) }
//        (1 .. WEB_MODULES).map { WebModule(it) }
    createDotIdea(modules)
    println("Java modules: $JAVA_MODULES")
    println("Content roots per module: $CONTENT_ROOTS_PER_JAVA_MODULE")
    println("package.json: $PACKAGE_JSON_COUNT")

    for (module in modules) {
      val contentRoots = module.generateContentRoots(root)
      val imlFile = module.imlFile(root)
      root.resolve(imlFile).writePrettyPrintedXml(
        $$"""
        <?xml version="1.0" encoding="UTF-8"?>
        <module type="JAVA_MODULE" version="4">
          <component name="NewModuleRootManager" inherit-compiler-output="true">
            <exclude-output />
            $${contentRoots.joinToString("\n")}
            <orderEntry type="inheritedJdk" />
            <orderEntry type="sourceFolder" forTests="false" />
          </component>
        </module>
        """
      )
    }
    println()
    createdPackageJsonInfo.forEach {
      println(it)
    }
  }

  private fun createDotIdea(modules: List<Module>) {
    val dotIdea = root.resolve(".idea").createDirectory()
    val moduleReferences = modules.map {
      val imlFile = it.imlFile(root)
      createModuleReference(root.relativize(imlFile).toString())
    }
    dotIdea.resolve("modules.xml").writePrettyPrintedXml(
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="ProjectModuleManager">
          <modules>
            ${moduleReferences.joinToString("\n")}
          </modules>
        </component>
      </project>
      """
    )
    dotIdea.resolve("misc.xml").writePrettyPrintedXml(
      $$"""
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="ProjectRootManager" version="2" project-jdk-name="21" project-jdk-type="JavaSDK">
          <output url="file://$PROJECT_DIR$/out" />
        </component>
      </project>
      """
    )
  }

  companion object {
    const val GENERATE_ROOT: String = "./generatedHugeProject"

    fun genRoot(): Path {
      val genRoot = Path.of(GENERATE_ROOT)
      if (genRoot.isAbsolute) {
        return genRoot
      }
      return Path.of(".").absolute().resolve(GENERATE_ROOT).normalize()
    }

    @OptIn(ExperimentalPathApi::class)
    @JvmStatic
    fun main(args: Array<String>) {
      val genRoot = genRoot()
      if (genRoot.exists()) {
        genRoot.deleteRecursively()
      }
      genRoot.createDirectories()
      println("Generating to $genRoot")
      ProjectGenerator(genRoot).generate()
    }
  }
}

class RootModule() : Module {
  override fun moduleRoot(root: Path): Path {
    return root
  }

  override fun generateContentRoots(root: Path): List<String> {
    val contentRootUrl = $$"file://$MODULE_DIR$"
    return listOf(
      $$"""
      <content url="$$contentRootUrl">
        <sourceFolder url="$$contentRootUrl/resources" type="java-resource" />
        <sourceFolder url="$$contentRootUrl/src" isTestSource="false" />
        <sourceFolder url="$$contentRootUrl/test" isTestSource="true" />
        <sourceFolder url="$$contentRootUrl/testResources" type="java-test-resource" />
      </content>
      """
    )
  }
}
