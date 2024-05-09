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
    val grammar = Grammar("src/main/resources/rules")

    // inf loop
    val scanner = Scanner(System.`in`)
    var inputString: String? = null

    while (inputString != "skip") {
        println("Enter menu-item or \"skip\"")
        println("1. recognize input")
        println("2. generate string")
        println("3. show alphabet")
        println("4. show grammar rules")
        println("5. funny tests...")
        println("6. FIRST, FOLLOW")

        if(!scanner.hasNext()){
            continue
        }

        inputString = scanner.nextLine()

        when (inputString) {
            "1" -> {
                println("Enter input:")
                val userInput = scanner.nextLine()
                val splittedInput = userInput.split(" ").toMutableList()
                var lastWord = splittedInput.removeLast()
                val lastSymbol = "${lastWord.last()}"
                lastWord = lastWord.substring(0,lastWord.lastIndex)

                val sequence:MutableList<TerminalWord> = mutableListOf()
                var flag = false
                for (word in splittedInput){
                    if(grammar.getCanonilizeWord(word) == null){
                        println("$word does not contain in alphabet.")
                        flag = true
                        break
                    }
                    grammar.getCanonilizeWord(word)?.let { TerminalWord(it) }?.let { sequence.add(it) }
                }
                if (flag){
                    continue
                }
                if (splittedInput.size == 0){
                    println("Doesn't contain in grammar.")
                    continue
                }


                grammar.getCanonilizeWord(lastWord)?.let { TerminalWord(it) }?.let { sequence.add(it) }
                grammar.getCanonilizeWord(lastSymbol)?.let { TerminalWord(it) }?.let { sequence.add(it) }


                if (grammar.ll1(sequence)){
                    println("Contains grammar")
                }
                else {
                    println("Doesn't contains grammar")
                }
            }

            "2" -> {
                println("Generating string...")
                val generatedSentence = grammar.generateSentence()


                println("Generated string: ${terminalsSequenceToString(generatedSentence.toList())}")
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
                println("Enter amount of funny tests:")
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