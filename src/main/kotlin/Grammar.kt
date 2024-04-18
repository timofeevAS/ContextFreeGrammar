import java.io.File

class Grammar(fileName: String) {
    private val BackusNaurLineRegex = "(<\\w+>)\\s::=\\s+((<\\w+>|`[^`]+`)\\s*)+(\\|\\s*((<\\w+>|`[^`]+`)\\s*)+)*"
    private val regex = Regex(BackusNaurLineRegex)
    private val nonTerminalMap: MutableMap<String, NonTerminalWord> = mutableMapOf()
    private val spaceTerminal: TerminalWord = TerminalWord(" ")

    init {
        readBackusNaurRules(fileName)
    }

    private fun getNonTerminalByValue(value:String): NonTerminalWord {
        return nonTerminalMap.getOrDefault(value, NonTerminalWord("", mutableListOf()))
    }
    private fun parseSequenceOfExpression(line:String): MutableList<Word> {
        var buf:String = ""
        var pushingFlag:Boolean = false
        var terminalFlag:Boolean = false

        val result:MutableList<Word> = mutableListOf()

        for (symbol in line) {
            if (symbol == '<') {
                pushingFlag = true
                terminalFlag = false
                continue
            } else if (symbol == '>') {
                pushingFlag = false
                terminalFlag = false
            } else if (symbol == '`' && !pushingFlag) {
                pushingFlag = true
                terminalFlag = true
                continue
            } else if (symbol == '`' && pushingFlag) {
                pushingFlag = false
                terminalFlag = true
            } else if (symbol == ' ' && !terminalFlag) {
                result.add(spaceTerminal)
                continue
            }

            if (pushingFlag) {
                buf += symbol
            } else {
                var word: Word
                if (terminalFlag) {
                    word = TerminalWord(buf)
                } else {
                    if (getNonTerminalByValue(buf).getWord() != "") {
                        word = getNonTerminalByValue(buf)
                    } else {
                        word = NonTerminalWord(buf, mutableListOf())
                        nonTerminalMap[buf] = word
                    }
                }
                buf = ""
                result.add(word)
            }
        }

        return result
    }
    private fun parseBackusNaurLine(line:String):Unit{
        var splitLine:MutableList<String> =line.split(" ::= ").toMutableList()

        var symbolWord = splitLine.first().substring(1,splitLine.first().length-1)
        splitLine.removeAt(0)

        val sequences:MutableList<Sequence> = mutableListOf()

        for(sequence in splitLine[0].split("|")){
            val parsedSequence:MutableList<Word> = parseSequenceOfExpression(sequence)
            val readySequence:Sequence = Sequence(parsedSequence)
            sequences.add(readySequence)
        }

        if(nonTerminalMap.containsKey(symbolWord)){
            sequences.forEach { s ->
                nonTerminalMap[symbolWord]?.addSequence(s)
            }
        }
        else {
            nonTerminalMap[symbolWord] = NonTerminalWord(symbolWord, sequences)
        }

    }
    private fun readBackusNaurRules(fileName:String){
        val file = File(fileName)
        val lines = file.readLines()

        for (line in lines){
            if (line != "\n" && regex.matches(line)){
                parseBackusNaurLine(line)
            }
        }

        for (nt in nonTerminalMap.keys){
            println("${nonTerminalMap[nt]} -> ${nonTerminalMap[nt]?.getExpressionList()}")
        }

    }
}