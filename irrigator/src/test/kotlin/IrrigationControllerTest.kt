//import com.github.michaelbull.result.Err
//import com.github.michaelbull.result.Ok
//import com.github.michaelbull.result.unwrap
//import com.nhaarman.mockito_kotlin.mock
//import org.jetbrains.spek.api.Spek
//import org.jetbrains.spek.api.dsl.given
//import org.jetbrains.spek.api.dsl.it
//import org.jetbrains.spek.api.dsl.on
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class IrrigationControllerTest : Spek({
//    val ruleRepoMock = mock<PostgresDAO> { }
//    val controller = IrrigationController(ruleRepoMock, mock())
//
//    given("a bad IrrigationCommandBuilder") {
//        val commandBuilder = IrrigationCommandBuilder()
//        on("calling calculateDuration()") {
//            val result = controller.calculateDuration(commandBuilder, 0.01)
//            it("should result in an error") {
//                assertTrue { result is Err }
//            }
//        }
//    }
//
//    given("a good IrrigationCommandBuilder") {
//        val commandBuilder = IrrigationCommandBuilder(
//                rule = Rule(sensorId = "testID", threshold = 50, moistureToLitersRatio = 25.0, valveId = 1, target = 90),
//                reading = Reading("testId", 10.0))
//
//        on("calling calculateDuration()") {
//            val result = controller.calculateDuration(commandBuilder, 0.01)
//            it("should return a good command") {
//                assertTrue { result is Ok }
//            }
//
//            it("should return calculate the correct value") {
//                assertEquals(160, result.unwrap())
//            }
//        }
//    }
//})