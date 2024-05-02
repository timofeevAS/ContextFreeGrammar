import java.io.File
import kotlin.random.Random

class Grammar(fileName: String) {
    private val BackusNaurLineRegex = "(<\\w+>)\\s::=\\s+((<\\w+>|`[^`]+`)\\s*)+(\\|\\s*((<\\w+>|`[^`]+`)\\s*)+)*"
    private val regex = Regex(BackusNaurLineRegex)
    private val nonTerminalMap: MutableMap<String, NonTerminalWord> = mutableMapOf()
    private val terminalAlphabet:MutableSet<TerminalWord> = mutableSetOf()
    private val spaceTerminal: TerminalWord = TerminalWord(" ")
    private val FIRST = mutableMapOf<NonTerminalWord, MutableSet<TerminalWord>>()
    private val FOLLOW = mutableMapOf<NonTerminalWord, MutableSet<TerminalWord>>()
    private val TRUE_FIRST = mutableMapOf<Pair<NonTerminalWord,Int>,MutableSet<TerminalWord>>()
    private val LOOKUP_TABLE = mutableMapOf<Pair<NonTerminalWord, TerminalWord>,Pair<NonTerminalWord, Int>>()
    private val NULLNTW = NonTerminalWord("", mutableListOf())
    private val NULLNTWIDX = Pair(NULLNTW, -1)

    init {
        readBackusNaurRules(fileName)

        // Construct FIRST and FOLLOW sets
        var s: MutableSet<TerminalWord> = mutableSetOf()
        constructFIRST()
        constructTRUEFIRST()
        constructFOLLOW()
        consturctLOOKUPTABLE()


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
            for (sequence in nonTerminalMap[nt]?.getExpressionList()!!){
                for (word in sequence.getSequence()){
                    if (word.isTerminal()){
                        terminalAlphabet.add(word as TerminalWord)
                    }
                }
            }
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

    private fun getTerminalTrueFirstSet(ntW:NonTerminalWord, idx:Int, firstSet: MutableSet<TerminalWord>) {
        var word = ntW.getExpressionList().get(idx).getSequence().first()
        if (word.isTerminal()) {
            firstSet.add(word as TerminalWord)
        }
        else {
            getTerminalFirstSet(word as NonTerminalWord, firstSet)
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

    private fun constructTRUEFIRST(){
        for (ntA in nonTerminalMap.values){
            for (idx in ntA.getExpressionList().indices){
                val sequence = ntA.getExpressionList().get(idx)
                TRUE_FIRST[Pair(ntA,idx)] = mutableSetOf()
                TRUE_FIRST[Pair(ntA, idx)]?.let { getTerminalTrueFirstSet(ntA,idx, it) };
            }

        }
    }

    private fun findSequence(ntA: NonTerminalWord, tA:TerminalWord):Pair<NonTerminalWord, Int>{
        for (tfsKey in TRUE_FIRST.keys){
            if (tfsKey.first == ntA &&
                TRUE_FIRST[tfsKey]?.contains(tA) == true){
                return tfsKey
            }
        }
        return NULLNTWIDX
    }
    private fun consturctLOOKUPTABLE(){
        for (ntA in nonTerminalMap.values){
            for (tA in terminalAlphabet){
                LOOKUP_TABLE[Pair(ntA,tA)] = findSequence(ntA,tA)
            }
        }
    }
    public fun ll1(sequence: List<TerminalWord>):Boolean{
        val stack = java.util.ArrayDeque<Word>()
        nonTerminalMap["Sentence"]?.let { stack.push(it) }

        var inputIndex = 0
        while (stack.isNotEmpty()){
            val currentWord = stack.pop()
            when (currentWord) {
                is TerminalWord -> {
                    if (inputIndex < sequence.size && currentWord == sequence[inputIndex]) {
                        inputIndex++
                    } else {
                        return false // Неожиданный символ или конец входной строки
                    }
                }

                is NonTerminalWord -> {
                    val ltKey = Pair(currentWord, sequence[inputIndex])
                    if(LOOKUP_TABLE[ltKey] != NULLNTWIDX){
                        val ntA = LOOKUP_TABLE[ltKey]?.first
                        val idx = LOOKUP_TABLE[ltKey]?.second
                        val sequence = idx?.let { ntA?.getExpressionList()?.get(it) }
                        if (sequence != null) {
                            for(word in sequence.getSequence().reversed()){
                                stack.push(word)
                            }
                        }
                    } else{
                        return false
                    }
                }

                else -> return false // Неверный символ
            }
        }
        return inputIndex == sequence.size
    }

    private fun fullyTerminal(sequence: List<Word>):Boolean{
        for (word in sequence){
            if (!word.isTerminal()){
                return false
            }
        }
        return true
    }

    public fun generateSentence():List<Word>{
        val sequence:MutableList<Word> = mutableListOf(nonTerminalMap["Sentence"] as Word)

        while (!fullyTerminal(sequence)){
            // Find non terminal word
            var ntA = NULLNTW
            var ntI = 0
            for (word in sequence){
                if (!word.isTerminal()){
                    ntA = word as NonTerminalWord
                    break
                }
                ntI++
            }
            val rules = nonTerminalMap[ntA.getWord()]?.getExpressionList()
            val randIndex = rules?.let { Random.nextInt(0, it.size) }
            val rule = randIndex?.let { rules?.get(it) }

            sequence.removeAt(ntI)
            if (rule != null) {
                sequence.addAll(ntI, rule.getSequence())
            }
        }
        return sequence
    }

    public fun getCanonilizeWord(word:String):String?{
        val etalonword = word.lowercase()
        for(twA in terminalAlphabet){
            if (etalonword == twA.getWord().lowercase()){
                return twA.getWord()
            }
            if(twA.getWord().startsWith(etalonword)){
                return twA.getWord()
            }
        }
        return null
    }

    public fun getAlphabet():List<TerminalWord>{
        return terminalAlphabet.toList()
    }
}