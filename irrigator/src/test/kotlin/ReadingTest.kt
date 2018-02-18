import com.beust.klaxon.Klaxon
import com.dionysus.irrigator.domain.Reading
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertEquals

class ReadingTest: Spek({
    given("A valid json string representing a reading") {
        val jsonString = """{"id": "test", "value": 1.0}"""
        on("trying to parse the json string...") {
            val reading = Klaxon().parse<Reading>(jsonString)
            it ("should result in a value of 300") {
                val expectedReading =  Reading("test", 1.0)
                assertEquals(expectedReading, reading)
            }
        }
    }
})
