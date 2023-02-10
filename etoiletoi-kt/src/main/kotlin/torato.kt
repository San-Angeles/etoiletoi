import java.lang.StringBuilder

fun String.height() = groupBy { it }.values.maxOf { it.size }

class AdjacentMap : HashMap<Char, Set<Char>>() {

    fun input(data: String) {
        data.dropLast(1).forEachIndexed { index, c ->
            set(c, (get(c) ?: setOf()) + data[index + 1])
        }
    }

    fun nextBy(first: Char): Char? {
        return get(first)?.random()
    }

    companion object {
        const val MAX_ADJACENT_LENGTH = 2
    }

}

fun AdjacentMap.generateBy(first: Char, length: Int): String {
    val buffer = StringBuilder()
    var curr = first
    var residue = length
    do {
        buffer.append(curr)
        curr = nextBy(curr) ?: break
        residue--
    } while (residue > 0)

    val result = buffer.toString()
    return if (result.height() <= AdjacentMap.MAX_ADJACENT_LENGTH)
        result else generateBy(first, length)
}

fun AdjacentMap.generate(length: Int) = generateBy(keys.random(), length)

fun main() {

//    println("étoile et toi".height())
    val adjacent = AdjacentMap()
    adjacent.input("apple")
//    println(adjacent)
    for (i in 1..5)
        println(adjacent.generate(7))
}
