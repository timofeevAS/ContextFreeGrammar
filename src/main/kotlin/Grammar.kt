import java.io.File

class Grammar(fileName: String) {
    private val BackusNaurLineRegex = "(<\\w+>)\\s::=\\s((<\\w+>|`[^`]+`)\\s*)+(\\|\\s*((<\\w+>|`[^`]+`)\\s*)+)*"
    private val regex = Regex(BackusNaurLineRegex)


    init {
        readBackusNaurRules(fileName)

    }

    private fun parseBackusNaurLine(line:String):List<Word>{
        var l = line.split('|')
        println(l)
        return listOf()
    }
    private fun readBackusNaurRules(fileName:String){
        val file = File(fileName)
        val lines = file.readLines()



        for (line in lines){
            if (line != "\n" && regex.matches(line)){
                parseBackusNaurLine(line)
            }
        }
    }
}