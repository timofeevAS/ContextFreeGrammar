import java.util.*

fun main(args: Array<String>) {
    println("Hello World!")

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

        inputString = scanner.nextLine()

        when (inputString) {
            "1" -> {
                println("Enter input:")
                val userInput = scanner.nextLine()
                // TODO
            }

            "2" -> {
                println("Generating string...")
                var generatedString = ""
                val generatedSentence = grammar.generateSentence()
                for (idx in generatedSentence.indices){
                    val word = generatedSentence[idx]
                    if (idx == 0){
                        generatedString += word.getWord()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
                    else
                    {
                        generatedString += word.getWord()
                    }

                    if(idx < generatedSentence.size - 2){
                        generatedString += " "
                    }

                }
                println("Generated string: $generatedString")
            }

            "skip" -> println("Exiting menu.")
            else -> println("Invalid input. Please try again.")
        }
    }
}