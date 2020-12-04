package helpers

import net.nprod.lotus.helpers.tryCount
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


internal class RetryRunTest {
    class ExpectedError : Exception()
    class UnexpectedError : Exception()

    @Test
    fun `should fail immediately if it is not the checked exception`() {
        var counter = 0
        assertThrows(UnexpectedError::class.java) {
            tryCount<ExpectedError, Unit> {
                counter += 1
                throw UnexpectedError()
            }
        }
        assert(counter == 1)
    }

    @Test
    fun `should fail after 3 runs if it is the checked exception`() {
        var counter = 0
        assertThrows(ExpectedError::class.java) {
            tryCount<ExpectedError, Unit> {
                counter += 1
                throw ExpectedError()
            }
        }
        assert(counter == 3)
    }

    @Test
    fun `we get the output of the function`() {
        var touched = false

        touched = tryCount<ExpectedError, Boolean> {
            true
        }

        assert(touched)
    }
}