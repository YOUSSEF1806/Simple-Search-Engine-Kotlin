package search

import java.io.File

enum class StratSearch { ALL, ANY, NONE }

fun main(args: Array<String>) {
    val file = checkValidArgsAndFile(args)
    if (file != null) {
        val dataLines = readDataLines(file)
        if (dataLines != null) {
            var exitFlag = false
            while (!exitFlag) {
                when (showMenu()) {
                    "1" -> {
                        println("\nSelect a matching strategy: ALL, ANY, NONE (default ANY)")
                        val matchingStrat = readln()
                        println("\nEnter a name or email to search all suitable people.")
                        val textToFind = readln()
                        val setOfLines = findTextInData(textToFind, invertIndex(dataLines), matchingStrat)
                        printDataLines(dataLines, setOfLines)
                    }

                    "2" -> {
                        println("\n=== List of people ===")
                        printDataLines(dataLines)
                    }

                    "0" -> {
                        println("\nBye!")
                        exitFlag = true
                    }

                    else -> println("\nIncorrect option! Try again.")
                }
            }
        }
    }
}

fun invertIndex(dataLines: List<List<String>>): Map<String, Set<Int>> {
    val indexedData: MutableMap<String, Set<Int>> = mutableMapOf()
    dataLines.forEachIndexed { index, line ->
        line.forEach {
            if (indexedData.contains(it))
                indexedData[it] = indexedData[it]!! + setOf(index)
            else
                indexedData.put(it, setOf(index))
        }
    }
    return indexedData
}

private fun findTextInData(textToFind: String, dataMap: Map<String, Set<Int>>, matchingStrat: String): Set<Int> {
    val listTextSearch = textToFind.split(" ").map { it.trim().lowercase() }
    val tempDataMap = dataMap.mapKeys { it.key.lowercase() }
    val resultSearch = when (matchingStrat) {
        StratSearch.ALL.toString() -> {
            listTextSearch
                .map { tempDataMap[it] ?: emptySet() }
                .reduceOrNull { set1, set2 -> set1 intersect set2 } ?: emptySet()
        }

        StratSearch.NONE.toString() -> {
            listTextSearch
                .map { string ->
                    // lines where "string" doesn't exist
                    tempDataMap.filterKeys { it != string }
                        .map { it.value.subtract((tempDataMap[string] ?: emptySet()).toSet()) }
                        .reduce { acc, set -> acc union set }
                }
                // lines where all the strings doesn't exist
                .reduceOrNull { set1, set2 -> set1 intersect set2 } ?: emptySet()
        }

        else -> {
            listTextSearch
                .map { tempDataMap[it] ?: emptySet() }
                .reduceOrNull { acc, set -> acc union set } ?: emptySet()
        }
    }
    return resultSearch
}

fun printDataLines(dataLines: List<List<String>>, setOfLines: Set<Int>? = null) {
    if (setOfLines == null)
        dataLines.map { it.joinToString(" ") }.forEach(::println)
    else if (setOfLines.isEmpty())
        println("No data matching the search!")
    else
        dataLines.filterIndexed { index, _ -> index in setOfLines }
            .map { it.joinToString(" ") }.forEach(::println)
}

fun readDataLines(file: File): List<List<String>>? {
    val dataLines: MutableList<List<String>> = mutableListOf()
    return try {
        file.forEachLine {
            dataLines.add(it.split(" ").map { it.trim() })
        }
        dataLines.toList()
    } catch (e: AccessDeniedException) {
        println("File access denied")
        null
    }
}

fun showMenu(): String {
    println("\n=== Menu ===")
    println("1. Find a person")
    println("2. Print all people")
    println("0. Exit")
    return readln().trim()
}

fun checkValidArgsAndFile(args: Array<String>): File? {
    if (args.size == 2 && args[0] == "--data") {
        val file = File(args[1])
        if (file.exists())
            return file
    }
    return null
}