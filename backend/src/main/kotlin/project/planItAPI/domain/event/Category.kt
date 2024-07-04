package project.planItAPI.domain.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidValueException
import project.planItAPI.utils.Success
import java.io.File
import java.lang.Exception

const val CATEGORIES_FILE_PATH = "categories.json"

typealias CategoryResult = Either<HTTPCodeException, Category>

class Category private constructor(val name: String) {
    companion object {
        operator fun invoke(name: String): CategoryResult {
            val isCategory = isCategory(name)
            return if (isCategory) Success(Category(name)) else Failure(InvalidValueException("category"))
        }
    }
}

/**
 * Reads the categories file and returns the categories as a map.
 * @return A map containing the categories and their subcategories.
 */
fun readCategories(): List<String> {
    val mapper = jacksonObjectMapper()
    val file = Category::class.java.classLoader.getResource(CATEGORIES_FILE_PATH)
    return if (file != null) {
        mapper.readValue(File(file.file))
    }
    else {
        throw Exception("Categories file not found.")
    }
}

/**
 * Checks if a category is valid.
 * @param name The name of the category to check.
 * @return True if the category is valid, false if it is not.
 */
fun isCategory(name: String): Boolean {
    val categories = readCategories().find { it.lowercase() == name.lowercase() }
    return categories != null
}
