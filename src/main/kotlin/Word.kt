open class Word(protected val value: String){

    fun getWord():String{
        return value;
    }

    override fun toString(): String {
        return "$value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Word) return false

        return this.value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


}