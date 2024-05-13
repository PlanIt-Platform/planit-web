package project.planItAPI.domain.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.Success
import java.io.File
import java.lang.Exception

const val CATEGORIES_FILE_PATH = "categories.json"

typealias CategoryResult = Either<HTTPCodeException, Category>

class Category private constructor(val name: String) {
    companion object {
        private val categories = readCategories().keys

        operator fun invoke(name: String): CategoryResult {
            if (!categories.contains(name)) {
                return Failure(InvalidCategoryException())
            }
            return Success(Category(name))
        }
    }
}

/**
 * Reads the categories file and returns the categories as a map.
 * @return A map containing the categories and their subcategories.
 */
fun readCategories(): Map<String, List<String>> {
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
    val categories = readCategories().keys.filter { it.lowercase() == name.lowercase() }
    return categories.size == 1
}
