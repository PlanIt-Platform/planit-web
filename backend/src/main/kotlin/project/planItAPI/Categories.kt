package project.planItAPI

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

const val CATEGORIES_FILE_PATH = "backend/src/main/kotlin/project/planItAPI/categories.json"

/**
 * Reads the categories file and returns the categories as a map.
 * @return A map containing the categories and their subcategories.
 */
fun getCategories(): Map<String, List<String>> {
    val mapper = jacksonObjectMapper()
    return mapper.readValue(File(CATEGORIES_FILE_PATH))
}

/**
 * Checks if a category is valid.
 * @param name The name of the category to check.
 * @return True if the category is valid, false if it is not.
 */
fun isCategory(name: String): Boolean {
    val categories = getCategories().keys.filter { it.lowercase() == name.lowercase() }
    return categories.size == 1
}

/**
 * Checks if a subcategory is valid for a given category.
 * @param category The category to check.
 * @param subcategory The subcategory to check.
 * @return True if the subcategory is valid for the category, false if it is not, and null if the category does not exist.

 */
fun isValidSubcategory(category: String, subcategory: String): Boolean? {
    val categories = getCategories()
    if (isCategory(category)) {
        return categories[category]?.any { it.contains(subcategory) } ?: false
    }
    return null
}

fun getSubCategories(category: String): List<String>? {
    if (!isCategory(category)) {
        return null
    }
    return getCategories()[category]
}

fun getSubCategoriesLowerCase(category: String): List<String>? {
    if (!isCategory(category)) {
        return null
    }
    return getCategories().filter{ it.key.lowercase() == category.lowercase() }.values.first()
}