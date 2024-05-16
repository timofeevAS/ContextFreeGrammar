import java.util.*

fun terminalsSequenceToString(sequence:List<Word>):String{
    var generatedString = ""
    for (idx in sequence.indices){
        val word = sequence[idx]
        if (idx == 0){
            generatedString += word.getWord()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        else
        {
            generatedString += word.getWord()
        }

        if(idx < sequence.size - 2){
            generatedString += " "
        }


    }

    if(generatedString.endsWith(" s?") || generatedString.endsWith(" s.")){
        generatedString = generatedString.replace(" s?", "s?")
        generatedString = generatedString.replace(" s.", "s.")

    }

    return generatedString
}
fun main(args: Array<String>) {
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    val grammar = Grammar("src/main/resources/lab5","src/main/resources/lab5.semantic")

    // inf loop
    val scanner = Scanner(System.`in`)
    var inputString: String? = null

    while (inputString != "skip") {
        println("Enter menu-item or \"skip\"")
        println("1. recognize input")
        println("2. generate string")
        println("3. show alphabet")
        println("4. show grammar rules")
        println("5. many generations...")
        println("6. FIRST, FOLLOW")

        if(!scanner.hasNext()){
            continue
        }

        inputString = scanner.nextLine()

        when (inputString) {
            "1" -> {
                println("Enter input:")
                val userInput = scanner.nextLine()

                if(userInput.isEmpty()){
                    println("Dont enter empty strings.")
                    continue
                }

                val sequence:MutableList<TerminalWord> = mutableListOf()
                var word = ""
                var idx = 0
                var errorflag = ""

                for (symbol in userInput){
                    val s = grammar.getCanonilizeWord(symbol.toString())
                    if(s != null && s.size == 1){
                        sequence.add(TerminalWord(symbol.toString()))
                    }
                    else
                    {
                        errorflag = symbol.toString()
                        break
                    }
                }

                if (errorflag.isNotEmpty()){
                    println("Symbol $errorflag doesn't contains in grammar")
                }
                else {
                   val semantics = grammar.ll1semantic(sequence)
                   if (semantics != null){
                        println("${userInput.toString()} -> correct")
                        println("Semantic: ${semantics}")
                    }
                    else {
                        println("${userInput.toString()} -> incorrect")
                    }
                }
            }

            "2" -> {
                println("Generating string...")
                val generatedSentence = grammar.generateSentence()

                val tmp:MutableList<TerminalWord> = mutableListOf()

                for(word in generatedSentence){
                    tmp.add(word as TerminalWord)
                }

                var output = ""

                for(symbol in generatedSentence){
                    output+= symbol.getWord()
                }
                println("Generated string: $output")
                println("Semantic: ${grammar.ll1semantic(tmp)}")
            }
            "skip" -> println("Exiting menu.")
            "3" -> {
                val alphabet = grammar.getAlphabet()
                for (idx in alphabet.indices){
                    val word = alphabet[idx]
                    println("$idx. ${word.getWord()}")
                }
            }
            "4" -> {
                val nonTerminalMap = grammar.getNonTerminalMap()
                for (nt in nonTerminalMap.keys){
                    println("${nonTerminalMap[nt]} -> ${nonTerminalMap[nt]?.getExpressionList()}")
                }
            }
            "5" -> {
                println("Enter amount of generations:")
                var amount = 0
                while (true) {
                    if (scanner.hasNextInt()) {
                        val userInput = scanner.nextInt()
                        if (userInput >= 0) {
                            amount = userInput
                            break
                        } else {
                            println("Please, enter a non-negative integer. Integer should be in range (1,200)")
                        }
                    } else {
                        println("Please, enter a valid integer.")
                        scanner.next() // Discard invalid input
                    }
                }

                for (i in 1..amount){
                    val generated = grammar.generateSentence()
                    val ll1list:MutableList<TerminalWord> = mutableListOf()

                    generated.forEach { word -> ll1list.add(word as TerminalWord) }
                    if (ll1list.contains(TerminalWord("s"))){
                        println("stop")
                    }
                    println("$i. ${generated.toString()}    =>    ${grammar.ll1(ll1list)}")
                }

            }
            "6" -> {
                println("First:")
                println(grammar.getFIRSTasString())
                println("Follow:")
                println(grammar.getFOLLOWasString())
            }
            else -> println("Invalid input. Please try again.")
        }
    }
}