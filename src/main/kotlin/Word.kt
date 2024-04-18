open class Word(protected val value: String){

    fun getWord():String{
        return value;
    }

    override fun toString(): String {
        return "$value"
    }
}