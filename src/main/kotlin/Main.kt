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
        println("5. many generations...")
        println("6. FIRST, FOLLOW")

        if(!scanner.hasNext()){
            continue
        }

        inputString = scanner.nextLine()

        when (inputString) {
            "1" -> {
                println("Enter input:")
                val userInput = scanner.nextLine().split(" ").toMutableList()
                if(userInput.first().isEmpty()){
                    println("Sentence mustn't be empty.")
                    continue
                }
                val sequence:MutableList<TerminalWord> = mutableListOf()
                var word = ""
                var idx = 0



                val last = userInput.last().last().toString()
                val endWord = userInput.last().dropLast(1)

                userInput.removeLast()
                userInput.add(endWord)
                userInput.add(last)
                var flag = false;
                var errorflag = ""

                for (idx in userInput.indices){
                    val word = userInput[idx].lowercase()
                    val s =grammar.getCanonilizeWord(word)
                    if (s != null && s.size == 1 || (word == "has" || word == "have")){
                        if (s != null) {
                            sequence.add(TerminalWord(s.first()))
                        }
                        if(idx < userInput.size-2){
                            sequence.add(TerminalWord(" "))
                        }
                    }
                    else{
                        if(!grammar.getAlphabet().contains(TerminalWord(word))){
                            errorflag=word
                            break
                        }
                    }
                }

                if(errorflag.isNotEmpty()){
                    println("${errorflag} - doesn't contains in grammar.")
                    continue
                }

                if(flag){
                    continue
                }
                else if (grammar.ll1(sequence)){
                    println("${userInput.toString()} -> correct")
                }
                else {
                    println("${userInput.toString()} -> incorrect")
                }
            }

            "2" -> {
                println("Generating string...")
                val generatedSentence = grammar.generateSentence()
                var output = generatedSentence.first().getWord().capitalize()

                for(word in generatedSentence.subList(1, generatedSentence.size)){
                    output+=word.getWord()
                }

                println("Generated string: $output")
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
                println("Enter amount of generation:")
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