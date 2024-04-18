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
}