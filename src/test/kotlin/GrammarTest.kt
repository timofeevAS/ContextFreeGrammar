import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class GrammarTest {

    @Test
    fun testValidParsingGrammarRulesTerminals(){
        val grammar:Grammar = Grammar("src/test/resources/test_grammar")

        val names = mutableListOf("Alexey", "Mikhail")
        val except:MutableList<Sequence> = mutableListOf()

        names.forEach { name -> except.add(Sequence(mutableListOf(TerminalWord(name)))) }

        val exceptWord = NonTerminalWord("Name",except)
        val result = grammar.getNonTerminalByValue("Name")
        assertEquals(exceptWord,result)

    }
}