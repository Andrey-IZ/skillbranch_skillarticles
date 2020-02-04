package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(query: String): List<Int> {

    val pattern = query.replace("(\\W|\\S)", "")
    if (pattern.isEmpty()) return emptyList()

    val re = Regex(
        pattern,
        setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)
    )
    val results = this?.let {
        re.findAll(it)
            .map { matchResult ->  matchResult.range.first }
            .toList()
    }

    return results ?: emptyList()
}
