package org.na.gherkin.runner.report

import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class ReportInstaller private constructor(private val target: Path, private val source: Path) : SimpleFileVisitor<Path>() {

    private fun resolve(path: Path): Path {
        return target.resolve(source.relativize(path).toString())
    }

    @Throws(IOException::class)
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        val dst = resolve(dir)
        Files.createDirectories(dst)
        return super.preVisitDirectory(dir, attrs)
    }

    @Throws(IOException::class)
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        val dst = resolve(file)
        Files.copy(Files.newInputStream(file), dst, StandardCopyOption.REPLACE_EXISTING)
        return super.visitFile(file, attrs)
    }

    companion object {

        @Throws(URISyntaxException::class, IOException::class)
        fun installResources(dst: Path, cls: Class<*>, root: String) {
            val location = cls.protectionDomain.codeSource.location
            if (location.protocol == "file") {
                val path = Paths.get(location.toURI())
                if (location.path.endsWith(".jar")) {
                    FileSystems.newFileSystem(path, null).use { fs -> installResources(dst, fs.getPath("/" + root)) }
                } else {
                    installResources(dst, Paths.get(this::class.java.getResource("/$root").toURI()))
                }
            } else {
                throw IllegalArgumentException("Not supported: " + location)
            }
        }

        @Throws(IOException::class)
        private fun installResources(dst: Path, src: Path) {
            Files.walkFileTree(src, ReportInstaller(dst, src))
        }
    }
}