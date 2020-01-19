package com.fleshgrinder.commons

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private class BicTest {
    @ParameterizedTest
    @ValueSource(strings = ["AAAABBC", "AAAABBCCD", "AAAABBCCDD", "AAAABBCCDDDE"])
    fun `exception is thrown if value is not 8 or 11 characters in length`(value: String) {
        assertThrows<IllegalArgumentException> { Bic(value) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["aaaaBBCC", "0000BBCC", "!!!!BBCC"])
    fun `exception is thrown if institution code contains non-uppercase ASCII letters`(value: String) {
        assertThrows<IllegalArgumentException> { Bic(value) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["AAAAbbCC", "AAAA11CC", "AAAA!!CC"])
    fun `exception is thrown if country code contains non-uppercase ASCII letters`(value: String) {
        assertThrows<IllegalArgumentException> { Bic(value) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["AAAABBcc", "AAAABB!!"])
    fun `exception is thrown if location code contains non-uppercase ASCII letters or digits`(value: String) {
        assertThrows<IllegalArgumentException> { Bic(value) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["AAAABBCCddd", "AAAABBCC!!!"])
    fun `exception is thrown if branch code contains non-uppercase ASCII letters or digits`(value: String) {
        assertThrows<IllegalArgumentException> { Bic(value) }
    }

    @Test fun `serialization success`() {
        val bic = ByteArrayOutputStream().use { data ->
            ObjectOutputStream(data).use { it.writeObject(Bic("AAAABBCCXXX")) }
            ObjectInputStream(ByteArrayInputStream(data.toByteArray())).readObject() as Bic
        }

        assertEquals("AAAABBCC", bic.canonical)
    }

    @Test fun `serialization failure`() {
        assertThrows<IllegalArgumentException> {
            ByteArrayOutputStream().use { data ->
                ObjectOutputStream(data).use { it.writeObject(Bic("AAAABBCCXXX")) }

                // We change the last value in the serialized data, which is the
                // last X from the original BIC value.
                val bytes = data.toByteArray()
                bytes[bytes.size - 1] = '_'.toByte()
                ObjectInputStream(ByteArrayInputStream(bytes)).readObject() as Bic
            }
        }
    }

    @Test fun sample() {
        // Construction from one of the static factory methods.
        val canonical = Bic.n26()

        // Construction from simple primitive string.
        val full = Bic("NTSBDEB1XXX")

        // Construction from individual parts; branch code is optional.
        val bundesbank = Bic(prefix = "MARK", country = "DE", suffix = "FF")

        // Equality works for the short and canonical version of the BIC,
        // regardless of the value that was parsed.
        assertEquals(canonical, canonical)
        assertEquals(canonical, full)

        // The original retains whatever value was passed to the constructor.
        assertEquals("NTSBDEB1", canonical.original)
        assertEquals("NTSBDEB1XXX", full.original)

        // The canonical drops the branch code if it is the special primary one.
        assertEquals("NTSBDEB1", canonical.canonical)
        assertEquals("NTSBDEB1XXX", full.canonical)

        // The full one always contains everything.
        assertEquals("NTSBDEB1XXX", canonical.full)
        assertEquals("NTSBDEB1XXX", full.full)

        // The individual parts are inlined properties that always yield the
        // part of the BIC, in other words: they are all always non-null.
        assertEquals("NTSBDEB1", canonical.id)
        assertEquals("NTSB", canonical.prefix)
        assertEquals("DE", canonical.country)
        assertEquals("B1", canonical.suffix)
        assertEquals("XXX", canonical.branch)

        // The following should be self-explanatory (otherwise check their
        // individual documentation).
        assertFalse(canonical.hasBranch)
        assertTrue(canonical.isPrimaryBranch)
        assertTrue(canonical.isN26)
        assertTrue(bundesbank.isBankOfGermany)
    }
}
