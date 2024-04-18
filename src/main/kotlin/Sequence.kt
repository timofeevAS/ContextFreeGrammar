class Sequence(private val sequence:MutableList<Word>) {
    override fun toString(): String {
        return sequence.joinToString("")
    }
}