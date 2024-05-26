package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidSubcategoryException
import project.planItAPI.utils.Success

typealias SubCategoryResult = Either<HTTPCodeException, Subcategory>

class Subcategory private constructor(val name: String) {
    companion object {
        operator fun invoke(categoryName: String, subcategoryName: String?): SubCategoryResult {
            if (categoryName == "Simple Meeting") return Success(Subcategory(""))
            if (subcategoryName == null) return Success(Subcategory(""))
            val subcategories = readSubCategories(categoryName)
            if (subcategories == null || !subcategories.contains(subcategoryName)) {
                return Failure(InvalidSubcategoryException())
            }
            return Success(Subcategory(subcategoryName))
        }
    }
}

/**
 * Checks if a subcategory is valid for a given category.
 * @param category The category to check.
 * @param subcategory The subcategory to check.
 * @return True if the subcategory is valid for the category, false if it is not, and null if the category does not exist.

 */
fun isValidSubcategory(category: String, subcategory: String): Boolean? {
    val categories = readCategories()
    if (isCategory(category)) {
        return categories[category]?.any { it.contains(subcategory) } ?: false
    }
    return null
}

/**
 * Reads the category file and returns the subcategories for the given category.
 * @param category The category to read the subcategories for.
 * @return A list of subcategories for the given category, or null if the category does not exist.
 */
fun readSubCategories(category: String): List<String>? {
    if (!isCategory(category)) {
        return null
    }
    return readCategories()[category]
}
