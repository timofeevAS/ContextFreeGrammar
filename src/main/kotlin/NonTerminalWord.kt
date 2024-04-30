class NonTerminalWord(value: String, private val expressionList: MutableList<Sequence>): Word(value) {
    fun getExpressionList():MutableList<Sequence>{
        return expressionList
    }

    fun addSequence(sequence: Sequence){
        expressionList.add(sequence)
    }

    override fun toString():String {
        return "<$value>"
    }

    fun listToString():String {
        return ""
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NonTerminalWord) return false

        return this.value == other.value && this.expressionList == other.expressionList
    }

    override fun isTerminal():Boolean{
        return false;
    }
}