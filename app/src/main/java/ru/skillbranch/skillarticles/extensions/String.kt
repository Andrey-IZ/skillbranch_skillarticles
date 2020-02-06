package ru.skillbranch.skillarticles.extensions

fun String?.indexesOf(substr: String, ignoreCase: Boolean = true): List<Int> {

    val pattern = substr.replace("(\\W|\\S)", "")
    if (pattern.isEmpty()) return emptyList()

    val flags = mutableSetOf(RegexOption.MULTILINE)
    if (ignoreCase)
        flags.add(RegexOption.IGNORE_CASE)
    val re = Regex(pattern, flags)

    val results = this?.let {
        re.findAll(it)
            .map { matchResult -> matchResult.range.first }
            .toList()
    }

    return results ?: emptyList()
}
