class TerminalWord(value: String) : Word(value){
    override fun toString(): String {
        return "`$value`"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TerminalWord) return false

        return this.value == other.value
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun isTerminal(): Boolean {
        return true;
    }

}