package project.planItAPI.services

import project.planItAPI.isCategory
import project.planItAPI.isValidSubcategory
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.InvalidPriceFormatException
import project.planItAPI.utils.InvalidSubcategoryException
import project.planItAPI.utils.InvalidTimestampFormatException
import project.planItAPI.utils.InvalidVisibilityException
import project.planItAPI.utils.Money
import project.planItAPI.utils.Success
import project.planItAPI.utils.UserIDParameterMissing

/**
 * Checks if the given timestamp is in the correct format.
 * @param timestamp The timestamp to check.
 * @return True if the timestamp is in the correct format, false otherwise.
 */
fun isValidTimestampFormat(timestamp: String): Boolean {
    val regex = """^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$""".toRegex()
    return regex.matches(timestamp)
}

/**
 * Parses the price parameter from a string.
 * @param priceString The string to parse.
 * @return [Money] The parsed price. If the string is null, returns null. If the string is not in the correct format, a [Failure] is thrown.
 */
fun parsePriceParameter(priceString: String?): Either<Boolean, Money?> {
    if(priceString == null) return Success(null)
    val regex = """(\d+(\.\d+)?)(\s*([A-Za-z]+))?""".toRegex()
    val matchResult = regex.find(priceString)

    if (matchResult != null) {
        val (amountStr, _, _, currency) = matchResult.destructured
        val amount = amountStr.toDoubleOrNull() ?: return Failure(false)
        return Success(Money(amount, currency))
    }

    return Failure(false)
}

/**
 * Validates the inputs for creating or editing an event.
 * @param subcategory The subcategory of the event.
 * @param category The category of the event.
 * @param visibility The visibility of the event.
 * @param date The date of the event.
 * @param endDate The end date of the event.
 * @param price The price of the event.
 * @return A list of [HTTPCodeException] containing the errors found in the inputs.
 */
fun validateEventInputs(
    subcategory: String?,
    category: String,
    visibility: String?,
    date: String?,
    endDate: String?,
    price: Either<Boolean, Money?>,
    userID: Int
): List<HTTPCodeException> {
    val errorList = mutableListOf<HTTPCodeException>()
    if (subcategory != null) {
        val validSubCategory = isValidSubcategory(category, subcategory)
        if (validSubCategory == null) errorList.add(InvalidCategoryException())
        if (validSubCategory == false) errorList.add(InvalidSubcategoryException())
    } else if (!isCategory(category)) errorList.add(InvalidCategoryException())
    if (visibility != null && visibility != "Public" && visibility != "Private") errorList.add(
        InvalidVisibilityException()
    )
    if (date != null && !isValidTimestampFormat(date)) errorList.add(InvalidTimestampFormatException("date"))
    if (endDate != null && !isValidTimestampFormat(endDate)) errorList.add(InvalidTimestampFormatException("endDate"))
    if (price is Failure) errorList.add(InvalidPriceFormatException())
    if (userID == 0) errorList.add(UserIDParameterMissing())
    return errorList
}