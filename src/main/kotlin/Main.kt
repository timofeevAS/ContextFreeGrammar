fun main(args: Array<String>) {
    println("Hello World!")

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    val grammar = Grammar("src/main/resources/rules")
    val ans = grammar.recognizeSequence(listOf(TerminalWord("Cool"),
                                     TerminalWord("Man"),
                                     TerminalWord("has"),
                                     TerminalWord("Ran"),
                                     TerminalWord(".")))

    println(ans)
}