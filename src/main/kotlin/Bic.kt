@file:Suppress("MemberVisibilityCanBePrivate", "RedundantVisibilityModifier", "unused")

package com.fleshgrinder.commons

import org.jetbrains.annotations.TestOnly
import java.io.Serializable

/**
 * **BIC** stands for **Business Identifier Code** (aka. **Bank Identifier
 * Code**) and is an international standard defined through the
 * [ISO](https://w.wiki/4qE) in [ISO 9362](https://www.iso9362.org/). A BIC
 * specifies the elements and structure of a universal identifier (UID) for
 * financial and non-financial institutions, for which such an international
 * identifier is required to facilitate automated processing of information for
 * financial services. The BIC is used for addressing messages, routing business
 * transactions, and identifying business parties. [SWIFT](https://w.wiki/4k$)
 * acts as the registration authority (RA) for BICs and is responsible for its
 * implementation.
 *
 * This implementation adheres to the latest revision,
 * [ISO 9362:2014](https://www.iso.org/standard/60390.html), of the standard in
 * which a few things have changed compared to previous iterations. You can find
 * additional information regarding the changes in the
 * [SWIFT White Paper](https://www.swift.com/node/14256) and
 * [SWIFT Information Paper](https://www.swift.com/resource/information-paper-iso-93622014-bic-implementation).
 * The following description will not include information on how it was in the
 * past but rather explain how it is now.
 *
 * A BIC is made up of 11 characters of which the last 3 are optional. The
 * required 8 characters are the [**Business Party Identifier** (**BPI**)][id]
 * which is further separated into three parts, the [**Business Party
 * Prefix**][prefix], [**Country Code**][country], and [**Business Party
 * Suffix**][suffix]. The _prefix_ and _suffix_ are chosen by SWIFT and the
 * _country_ corresponds to the country where the business resides (following
 * [ISO 3166-1 alpha-2](https://w.wiki/4kP)). The optional last 3 characters are
 * called the [**Branch Code**][branch] which identifies a specific location,
 * department, service, or unit of the business party.
 *
 * The following [ABNF](https://tools.ietf.org/html/rfc5234) specifies the
 * syntactic requirements:
 *
 * ```
 * business-identifier-code  := business-party-identifier [ branch-identifier ]
 *
 * business-party-identifier := business-party-prefix country-code business-party-suffix
 * business-party-prefix     := 4alnum
 * country-code              := 2alpha
 * business-party-suffix     := 2alnum
 * branch-identifier         := 3alnum
 *
 * alnum := alpha / digit
 * alpha := %x41-5A ; A-Z
 * digit := %x30-39 ; 0-9
 * ```
 *
 * A corresponding regular expression would look as follows:
 *
 * ```regexp
 * /^(?<business_identifier_code>
 *     (?<business_party_identifier>
 *         (?<business_party_prefix>[A-Z0-9]{4})
 *         (?<country_code>[A-Z]{2})
 *         (?<business_party_suffix>[A-Z0-9]{2})
 *     )
 *     (?<branch_code>[A-Z0-9]{3})?
 * )$/x
 * ```
 *
 * This class implements [Comparable] as well as [Serializable] for your
 * convenience. [Kotlin serialization](https://github.com/Kotlin/kotlinx.serialization)
 * is not supported because it is not stable yet.
 *
 * @sample com.fleshgrinder.commons.BicTest.sample
 *
 * @constructor Construct new [Bic] instance by parsing the given BIC8 or BIC11.
 * @throws IllegalArgumentException if the given value is not a valid BIC8 or
 *   BIC11.
 */
public class Bic(
    /**
     * The value that was used to construct this [Bic] instance.
     *
     * This might include a [branch], or not. It might also include the
     * [primary branch][PRIMARY_BRANCH] that is not included in a [canonical]
     * BIC.
     */
    public val original: String
) : Comparable<Bic>, Serializable {
    /**
     * Construct new [Bic] instance from the given individual parts.
     *
     * @throws IllegalArgumentException if the given individual parts do not
     *   result in a valid BIC.
     */
    @JvmOverloads
    public constructor(prefix: String, country: String, suffix: String, branch: String? = null) :
        this("$prefix$country$suffix${branch ?: ""}")

    /**
     * String representation of the BIC in accordance with the specification.
     *
     * 11 characters are used only if there is [branch] information available
     * other than the [primary one][PRIMARY_BRANCH].
     */
    @Transient
    public val canonical: String = original.let {
        require(it.length == LENGTH || it.length == EXTENDED_LENGTH) {
            "BIC must be $LENGTH or $EXTENDED_LENGTH long but got '$it' which is ${it.length} long."
        }
        require(it.regionIsAlnum(PS, PE)) {
            "BIC business party prefix must consist of upper-alphanumeric ASCII chars only, got: $it"
        }
        require(it.regionIsAlpha(CS, CE)) {
            "BIC country code must consist of upper-alphabetic ASCII chars only, got: $it"
        }
        require(it.regionIsAlnum(SS, SE)) {
            "BIC business party suffix must consist of upper-alphanumeric ASCII chars only, got: $it"
        }
        require(it.length == LENGTH || it.regionIsAlnum(BS, BE)) {
            "BIC branch code must consist of upper-alphanumeric ASCII chars only, got: $it"
        }

        when (it.length == EXTENDED_LENGTH && it.endsWith(PRIMARY_BRANCH)) {
            true -> it.substring(0, LENGTH)
            else -> it
        }
    }

    /**
     * String representation of the BIC that always includes a [branch].
     *
     * The [primary branch][PRIMARY_BRANCH] is used if the [canonical] value has
     * no [branch] information.
     */
    public inline val full: String
        get() = if (hasBranch) canonical else "$canonical$PRIMARY_BRANCH"

    /**
     * Get the Business Party Identifier (BPI) part of this BIC.
     *
     * The BPI is the combination of the [prefix], [country], and [suffix] and
     * uniquely identifies a registered business (worldwide).
     */
    public inline val id: String
        get() = canonical.substring(PS, SE)

    /**
     * First part of the [Business Party Identifier (BPI)][id].
     *
     * This value has no semantic meaning and is chosen by the Registration
     * Authority (RA) [SWIFT](https://w.wiki/4k$).
     */
    public inline val prefix: String
        get() = canonical.substring(PS, PE)

    /**
     * [ISO 3166-1 alpha-2](https://w.wiki/4kP) country code where this business
     * is located.
     */
    public inline val country: String
        get() = canonical.substring(CS, CE)

    /**
     * Second part of the [Business Party Identifier (BPI)][id].
     *
     * This value has no semantic meaning and is chosen by the Registration
     * Authority (RA) [SWIFT](https://w.wiki/4k$).
     *
     * @see isTest
     */
    public inline val suffix: String
        get() = canonical.substring(SS, SE)

    /**
     * Optional branch code of the business that identifies a specific location,
     * department, service, or unit of the business party.
     *
     * This value has no semantic meaning and is chosen by the Registration
     * Authority (RA) [SWIFT](https://w.wiki/4k$).
     *
     * @see hasBranch
     * @see isPrimaryBranch
     */
    public inline val branch: String
        get() = if (hasBranch) original.substring(BS, BE) else PRIMARY_BRANCH

    /**
     * Whether [branch] information is available for this BIC, or not.
     *
     * Branch information is available only if the [branch] is not the [special
     * primary one][PRIMARY_BRANCH].
     */
    @get:JvmName("hasBranch")
    public inline val hasBranch: Boolean
        get() = !isPrimaryBranch

    /**
     * Whether this [Bic] is for the primary branch of this business, or not.
     */
    public inline val isPrimaryBranch: Boolean
        get() = canonical.length == LENGTH

    /**
     * Whether this is a Test & Training (T&T) BIC, or not.
     *
     * T&T is a specific feature of the SWIFTNet FIN service. BICs that are used
     * in T&T have a `0` at position 8 (e.g. [BANK_OF_GERMANY_TEST], [N26_TEST])
     * and are never published to the BIC directory.
     */
    public inline val isTest: Boolean
        get() = canonical[7] == '0'

    /**
     * Whether this BIC is the one of the [bank of Germany](https://w.wiki/4kN),
     * or not.
     *
     * @see BANK_OF_GERMANY
     */
    public inline val isBankOfGermany: Boolean
        get() = canonical == BANK_OF_GERMANY

    /**
     * Whether this BIC is the testing code of the [bank of
     * Germany](https://w.wiki/4kN), or not.
     *
     * @see BANK_OF_GERMANY_TEST
     */
    public inline val isBankOfGermanyTest: Boolean
        get() = canonical == BANK_OF_GERMANY_TEST

    /**
     * Whether this BIC is the one of the [N26 bank](https://w.wiki/4kM), or
     * not.
     *
     * @see N26
     */
    public inline val isN26: Boolean
        get() = canonical == N26

    /**
     * Whether this BIC is the testing code of the [N26
     * bank](https://w.wiki/4kM), or not.
     *
     * @see N26_TEST
     */
    public inline val isN26Test: Boolean
        get() = canonical == N26_TEST

    /**
     * Two [Bic] instances are compared based on their [canonical] value.
     */
    public override fun equals(other: Any?): Boolean =
        other is Bic && canonical == other.canonical

    /**
     * Returns the hash code of the [canonical] value of this [Bic].
     */
    public override fun hashCode(): Int =
        canonical.hashCode()

    /**
     * Returns a textual representation of this [Bic] instance.
     */
    public override fun toString(): String =
        "BIC(original='$original', canonical='$canonical', full='$full')"

    /**
     * Comparison is based on the [canonical] values of the [Bic]s.
     *
     * Using the [canonical] value instead of the [full] value ensures that the
     * primary branches of a business are always sorted first, no matter how
     * they are going to be displayed. This would not be the case with [full]
     * since [PRIMARY_BRANCH] would sort after almost all other branch codes.
     */
    public override fun compareTo(other: Bic): Int =
        canonical.compareTo(other.canonical)

    /**
     * We only serialize the [original] value and have to reinitialize the class
     * after deserialization so that the [canonical] value (which is transient)
     * is present again. We do that by repeating the whole validation process
     * since there is no guarantee that the data that was deserialized is
     * actually valid other than validating it again.
     */
    private fun readResolve(): Any =
        Bic(original)

    /**
     * @see Bic
     */
    companion object {
        private const val serialVersionUID = -7821354567389594033L

        // @formatter:off
        @PublishedApi internal const val PS = 0       // prefix start index
        @PublishedApi internal const val PE = PS + 4  // prefix end index
        @PublishedApi internal const val CS = PE      // country start index
        @PublishedApi internal const val CE = CS + 2  // country end index
        @PublishedApi internal const val SS = CE      // suffix start index
        @PublishedApi internal const val SE = SS + 2  // suffix end index
        @PublishedApi internal const val BS = SE      // branch start index
        @PublishedApi internal const val BE = BS + 3  // branch end index
        // @formatter:on

        /**
         * Total length of a BIC without a [branch].
         */
        public const val LENGTH: Int = SE

        /**
         * Total length of a BIC with a [branch].
         */
        public const val EXTENDED_LENGTH: Int = BE

        /**
         * Special [branch identifier][branch] for the primary department,
         * service, unit, … of a business.
         *
         * @see isPrimaryBranch
         */
        public const val PRIMARY_BRANCH: String = "XXX"

        /**
         * BIC of the [bank of Germany](https://w.wiki/4kN).
         *
         * @see isBankOfGermany
         */
        public const val BANK_OF_GERMANY: String = "MARKDEFF"

        /**
         * Testing BIC of the [bank of Germany](https://w.wiki/4kN).
         *
         * @see isBankOfGermanyTest
         */
        public const val BANK_OF_GERMANY_TEST: String = "MARKDEF0"

        /**
         * BIC of the [N26 bank](https://w.wiki/4kM).
         *
         * @see isN26
         */
        public const val N26: String = "NTSBDEB1"

        /**
         * Testing BIC of the [N26 bank](https://w.wiki/4kM).
         *
         * @see isN26Test
         */
        public const val N26_TEST: String = "NTSBDEB0"

        /**
         * BIC of the [bank of Germany](https://w.wiki/4kN).
         *
         * @see isBankOfGermany
         */
        @JvmStatic
        public fun bankOfGermany(): Bic =
            Bic(BANK_OF_GERMANY)

        /**
         * Testing BIC of the [bank of Germany](https://w.wiki/4kN).
         *
         * @see isBankOfGermanyTest
         */
        @JvmStatic
        @TestOnly
        public fun bankOfGermanyTest(): Bic =
            Bic(BANK_OF_GERMANY_TEST)

        /**
         * BIC of the [N26 bank](https://w.wiki/4kM).
         *
         * @see isN26
         */
        @JvmStatic
        public fun n26(): Bic =
            Bic(N26)

        /**
         * Testing BIC of the [N26 bank](https://w.wiki/4kM).
         *
         * @see isN26Test
         */
        @JvmStatic
        @TestOnly
        public fun n26Test(): Bic =
            Bic(N26_TEST)

        @Suppress("NOTHING_TO_INLINE")
        private inline fun String.regionIsAlpha(startIndex: Int, endIndex: Int) =
            all(startIndex, endIndex) { it in 'A'..'Z' }

        @Suppress("NOTHING_TO_INLINE")
        private inline fun String.regionIsAlnum(startIndex: Int, endIndex: Int) =
            all(startIndex, endIndex) { it in '0'..'9' || it in 'A'..'Z' }

        private inline fun String.all(startIndex: Int, endIndex: Int, predicate: (Char) -> Boolean): Boolean {
            for (i in startIndex until endIndex) {
                if (!predicate(this[i])) {
                    return false
                }
            }
            return true
        }
    }
}
