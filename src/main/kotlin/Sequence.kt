class Sequence(private val sequence:MutableList<Word>) {
    override fun toString(): String {
        return sequence.joinToString("")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sequence

        return sequence == other.sequence
    }

    fun getSequence():MutableList<Word>{
        return sequence;
    }

    override fun hashCode(): Int {
        return sequence.hashCode()
    }


}