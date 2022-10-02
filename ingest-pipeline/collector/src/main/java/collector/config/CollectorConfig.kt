package collector.config

import java.nio.file.Paths
import java.util.Properties

data class CollectorConfig(val srcDirectory: String, val destDirectory: String, val pollRate: Int, val portNumber: Int = 8888, val isRecursiveSearch: Boolean = false) {

    lateinit var normalizedSrcDir: String
    lateinit var normalizedDestDir: String
    lateinit var srcURIScheme: String
    lateinit var destURIScheme: String
    var properties: Properties

    init {
        validateAndSplit(srcDirectory, true)
        validateAndSplit(destDirectory, false)
        properties = Properties()
        // TODO: Add Kafka properties
    }

    protected fun validateAndSplit(path: String, isSource: Boolean) {
        if (path != "") {
            if (path.contains("://")) {
                val splitString: Array<String> = path.split("://".toRegex(), limit = 2).toTypedArray()
                if (isSource) {
                    srcURIScheme = splitString[0]
                    normalizedSrcDir = Paths.get(splitString[1]).toString()
                } else {
                    destURIScheme = splitString[0]
                    normalizedDestDir = Paths.get(splitString[1]).toString()
                }
            } else {
                throw Exception("Improperly formatted directory. Missing [scheme]://")
            }
        } else {
            throw Exception("Invalid Configuration: Empty directory provided")
        }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("\n-----CollectorVerticle Configuration-----\n")
        builder.append("Source Directory: $srcDirectory\n")
        builder.append("Destination Directory: $destDirectory\n")
        builder.append("Poll rate: $pollRate\n")
        builder.append("Port Number: $portNumber\n")
        return builder.toString()
    }
}