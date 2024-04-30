import java.io.File

class Grammar(fileName: String) {
    private val BackusNaurLineRegex = "(<\\w+>)\\s::=\\s+((<\\w+>|`[^`]+`)\\s*)+(\\|\\s*((<\\w+>|`[^`]+`)\\s*)+)*"
    private val regex = Regex(BackusNaurLineRegex)
    private val nonTerminalMap: MutableMap<String, NonTerminalWord> = mutableMapOf()
    private val spaceTerminal: TerminalWord = TerminalWord(" ")
    private val FIRST = mutableMapOf<NonTerminalWord, MutableSet<TerminalWord>>()
    private val FOLLOW = mutableMapOf<NonTerminalWord, MutableSet<TerminalWord>>()

    init {
        readBackusNaurRules(fileName)

        // Construct FIRST and FOLLOW sets
        var s: MutableSet<TerminalWord> = mutableSetOf()
        constructFIRST()
        constructFOLLOW()
        println(FOLLOW)
    }

    fun getNonTerminalByValue(value:String): NonTerminalWord {
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
                //result.add(spaceTerminal)
                continue
            }

            if (pushingFlag) {
                buf += symbol
            } else {
                var word: Word
                if (terminalFlag) {
                    word = TerminalWord(buf)
                    terminalFlag = false
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

    private fun getTerminalFirstSet(ntW:NonTerminalWord, firstSet: MutableSet<TerminalWord>) {
        for (sequence in ntW.getExpressionList()){
            var word = sequence.getSequence().first()
            if (word.isTerminal()) {
                firstSet.add(word as TerminalWord)
            }
            else {
                getTerminalFirstSet(word as NonTerminalWord, firstSet)
            }
        }
    }

    private fun getTerminalFollowSet(ntW:NonTerminalWord, followSet: MutableSet<TerminalWord>) {
        for (ntA in nonTerminalMap.values){
            if (ntA == ntW){
                continue
            }
            for (seq in ntA.getExpressionList()) {
                var seqOfWords = seq.getSequence()
                for (index in seqOfWords.indices) {
                    val word = seqOfWords[index]
                    if (word.equals(ntW)) {
                        if (index + 1 >= seqOfWords.size) {
                            continue
                        }
                        var followWord = seqOfWords[index + 1]

                        if (followWord.isTerminal()) {
                            followSet.add(followWord as TerminalWord)
                        } else {
                            FIRST[followWord as NonTerminalWord]?.let { followSet.addAll(it) }
                        }
                    }
                }
            }
        }
    }
    private fun constructFIRST() {
        for (ntA in nonTerminalMap.values){
            FIRST[ntA] = mutableSetOf()
            FIRST[ntA]?.let { getTerminalFirstSet(ntA, it) };
        }
    }

    private fun constructFOLLOW() {
        for (ntA in nonTerminalMap.values){
            if (FOLLOW[ntA] == null){
                FOLLOW[ntA] = mutableSetOf()
            }
            FOLLOW[ntA]?.let { getTerminalFollowSet(ntA, it) };

            for(sequences in ntA.getExpressionList()){
                val lastInSeq = sequences.getSequence().last()
                if (!lastInSeq.isTerminal()){
                    val followValues = FOLLOW[ntA]
                    if (FOLLOW[lastInSeq as NonTerminalWord] == null){
                        FOLLOW[lastInSeq as NonTerminalWord] = mutableSetOf()
                    }
                    if (followValues != null) {
                        FOLLOW[lastInSeq as NonTerminalWord]?.addAll(followValues)
                    }
                }
            }
        }

    }
}