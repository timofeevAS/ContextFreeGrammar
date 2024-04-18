class TerminalWord(value: String) : Word(value){
    override fun toString(): String {
        return "`$value`"
    }
}