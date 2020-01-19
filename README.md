<h1 align="center"><a href="#logo-usage"><img src="https://upload.wikimedia.org/wikipedia/commons/5/56/Bic_logo.svg" alt="Société Bic S.A. Logo" width="160" height="61"></a></h1>
<p align="center">ISO 9362:2014 — Business Identifier Code</p>

**BIC** stands for **Business Identifier Code** (aka. **Bank Identifier Code**)
and is an international standard defined through the [ISO] in [ISO 9362]. A BIC
specifies the elements and structure of a universal identifier (UID) for
financial and non-financial institutions, for which such an international
identifier is required to facilitate automated processing of information for
financial services. The BIC is used for addressing messages, routing business
transactions, and identifying business parties. [SWIFT] acts as the registration
authority (RA) for BICs and is responsible for its implementation.

This Kotlin (and Java) implementation adheres to the latest revision,
[ISO 9362:2014], of the standard in which a few things have changed compared to
previous iterations. You can find additional information regarding the changes
in the [SWIFT White Paper] and [SWIFT Information Paper]. The following
description will not include information on how it was in the past but rather
explain how it is now.

A BIC is made up of 11 characters of which the last 3 are optional. The
required 8 characters are the **Business Party Identifier** (**BPI**) which is
further separated into three parts, the **Business Party Prefix**, **Country
Code**, and **Business Party Suffix**. The _prefix_ and _suffix_ are chosen by
SWIFT and the _country_ corresponds to the country where the business resides
(following [ISO 3166-1 alpha-2]). The optional last 3 characters are called the
**Branch Code** which identifies a specific location, department, service, or
unit of the business party.

The following [ABNF] specifies the syntactic requirements:

```
business-identifier-code  := business-party-identifier [ branch-identifier ]

business-party-identifier := business-party-prefix country-code business-party-suffix
business-party-prefix     := 4alnum
country-code              := 2alpha
business-party-suffix     := 2alnum
branch-identifier         := 3alnum

alnum := alpha / digit
alpha := %x41-5A ; A-Z
digit := %x30-39 ; 0-9
```

A corresponding regular expression would look as follows:

```regexp
/^(?<business_identifier_code>
    (?<business_party_identifier>
        (?<business_party_prefix>[A-Z0-9]{4})
        (?<country_code>[A-Z]{2})
        (?<business_party_suffix>[A-Z0-9]{2})
    )
    (?<branch_code>[A-Z0-9]{3})?
)$/x
```

Please refer to the [class documentation] for examples and all the gory details
of the actual implementation.

## Installation

The module is available from [Bintray] only. It will become available via
JCenter and Maven Central once 1.0.0 is out.

Add the following to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.fleshgrinder.kotlin:bic:0.1.0")
}

repositories {
    maven("https://dl.bintray.com/fleshgrinder/com.fleshgrinder.kotlin")
}
```

Please refer to [Bintray] for instructions on how to add the module to Maven or
Ivy.

## Project Info

* Please read [CONTRIBUTING.md] if you want to contribute, which would be very
  welcome.
* We use [Semantic Versioning] and [Keep a Changelog], available versions and
  changes are listed on our [releases] page and in the [CHANGELOG.md].
* This module is licensed under the [Unlicense], see [UNLICENSE.md] for details.

## Logo Usage

This non-commercial project uses the logo of the [Société Bic S.A.] company in a
parodied context because of the naming similarity of the standards abbreviation
_BIC_ and the common name the company is known by _BiC_. It is believed that
this usage falls under [Fair Use] and that [no permission is required to use the
logo](https://www.upcounsel.com/permission-to-use-logo). However, it is
important to note that this project is not associated with Société Bic S.A. nor
is it endorsed by them. In fact, they are probably not even aware of this
project in the first place.

The logo itself is not distributed along with this project but included from
[https://en.wikipedia.org/wiki/File:BIC_logo.svg](https://en.wikipedia.org/wiki/File:Bic_logo.svg).
It should be obvious that the project’s [Unlicense] doesn’t apply to the logo
which is owned by Société Bic S.A.

[ISO]: https://en.wikipedia.org/wiki/International_Organization_for_Standardization
[ISO 9362]: https://www.iso9362.org/
[ISO 9362:2014]: https://www.iso.org/standard/60390.html
[SWIFT]: https://en.wikipedia.org/wiki/Society_for_Worldwide_Interbank_Financial_Telecommunication
[SWIFT White Paper]: https://www.swift.com/node/14256
[SWIFT Information Paper]: https://www.swift.com/resource/information-paper-iso-93622014-bic-implementation
[ABNF]: https://tools.ietf.org/html/rfc5234
[ISO 3166-1 alpha-2]: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

[Bintray]: https://bintray.com/fleshgrinder/com.fleshgrinder.kotlin/bic
[class documentation]: https://fleshgrinder.github.io/kotlin-bic/com.fleshgrinder.commons/-b-i-c/index.html
[CONTRIBUTING.md]: https://github.com/Fleshgrinder/kotlin-bic/blob/master/CONTRIBUTING.md
[CHANGELOG.md]: https://github.com/Fleshgrinder/kotlin-bic/blob/master/CHANGELOG.md
[UNLICENSE.md]: https://github.com/Fleshgrinder/kotlin-bic/blob/master/UNLICENSE.md
[Semantic Versioning]: http://semver.org/
[Keep a Changelog]: https://keepachangelog.com/
[releases]: https://github.com/Fleshgrinder/kotlin-bic/releases
[Unlicense]: https://unlicense.org/

[Société Bic S.A.]: https://en.wikipedia.org/wiki/Soci%C3%A9t%C3%A9_Bic
[Fair Use]: https://en.wikipedia.org/wiki/Fair_use
