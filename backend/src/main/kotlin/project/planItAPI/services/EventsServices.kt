package project.planItAPI.services

import org.springframework.stereotype.Service
import project.planItAPI.isCategory
import project.planItAPI.isValidSubcategory
import project.planItAPI.repository.transaction.TransactionManager
import project.planItAPI.utils.CreateEventOutputModel
import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.Success
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidCategoryException
import project.planItAPI.utils.InvalidPriceFormatException
import project.planItAPI.utils.InvalidSubcategoryException
import project.planItAPI.utils.InvalidTimestampFormatException
import project.planItAPI.utils.InvalidVisibilityException
import project.planItAPI.utils.Money
import java.sql.Timestamp

@Service
class EventsServices(
    private val transactionManager: TransactionManager
) {

    /**
     * Creates a new event with the provided information.
     * @param title The title of the new event.
     * @param description The description of the new event.
     * @param category The category of the new event.
     * @param subcategory The subcategory of the new event.
     * @param location The location of the new event.
     * @param visibility The visibility of the new event.
     * @param date The date of the new event.
     * @param endDate The end date of the new event.
     * @param price The price of the new event.
     * @param userID The ID of the user creating the event.
     * @return [CreateEventOutputModel] containing the ID of the newly created event, its title and a status message.
     * If the creation fails, a [Failure] is thrown.
     */
    fun createEvent(
        title: String,
        description: String?,
        category: String,
        subcategory: String?,
        location: String?,
        visibility: String?,
        date: String?,
        endDate: String?,
        price: String?,
        userID: Int
    ): CreateEventResult =
        transactionManager.run {
            val errorList = mutableListOf<HTTPCodeException>()

            //Validations
            if(subcategory != null) {
                val validSubCategory = isValidSubcategory(category, subcategory)
                if(validSubCategory == null) {
                    errorList.add(InvalidCategoryException())
                }
                if(validSubCategory == false) {
                    errorList.add(InvalidSubcategoryException())
                }
            } else{
                if(!isCategory(category)) {
                    errorList.add(InvalidCategoryException())
                }
            }
            if(visibility != null && visibility != "Public" && visibility != "Private") {
                errorList.add(InvalidVisibilityException())
            }
            if(date != null && !isValidTimestampFormat(date)) {
                errorList.add(InvalidTimestampFormatException("date"))
            }
            if(endDate != null &&!isValidTimestampFormat(endDate)) {
                errorList.add(InvalidTimestampFormatException("endDate"))
            }
            val priceValidation = parsePriceParameter(price)
            if(priceValidation is Failure) {
                errorList.add(InvalidPriceFormatException())
            }

            if(errorList.isNotEmpty()) {
                val message = errorList.joinToString { error -> error.message }
                throw HTTPCodeException(message, 400)
            }

            val desc = description ?: ""
            val locat = location ?: "To be Determined"
            val vis = visibility ?: "Public"

            val eventsRepository = it.eventsRepository
            val eventID = eventsRepository.createEvent(
                title,
                desc,
                category,
                subcategory,
                locat,
                vis,
                if(date!=null) Timestamp.valueOf(date+":00") else null,
                if(endDate!=null) Timestamp.valueOf(endDate+":00") else null,
                if(priceValidation is Success) priceValidation.value else null,
                userID
            )
            if(eventID == null) {
                throw HTTPCodeException("Failed to create event.", 500)
            }
            return@run CreateEventOutputModel(eventID, title, "Created with success.")
        }

    /**
     * Retrieves the event associated with the given ID.
     * @param id The ID of the event to retrieve.
     * @return [EventOutputModel] The event associated with the ID. If the event is not found, a [Failure] is thrown.
     */
    fun getEvent(id: Int): EventResult = transactionManager.run {
        val event = it.eventsRepository.getEvent(id) ?: throw HTTPCodeException("Event not found.", 404)
        return@run event
    }

    /**
     * Retrieves the users in the event associated with the given ID.
     * @param id The ID of the event to retrieve the users from.
     * @return [List]<[UserInEvent]> The users in the event associated with the ID. If the event is not found, a [Failure] is thrown.
     */
    fun getUsersInEvent(id: Int): UsersInEventResult = transactionManager.run {
        val users = it.eventsRepository.getUsersInEvent(id) ?: throw HTTPCodeException("Event not found.", 404)
        return@run users
    }

    /**
     * Checks if the given timestamp is in the correct format.
     * @param timestamp The timestamp to check.
     * @return True if the timestamp is in the correct format, false otherwise.
     */
    private fun isValidTimestampFormat(timestamp: String): Boolean {
        val regex = """^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$""".toRegex()
        return regex.matches(timestamp)
    }

    /**
     * Parses the price parameter from a string.
     * @param priceString The string to parse.
     * @return [Money] The parsed price. If the string is null, returns null. If the string is not in the correct format, a [Failure] is thrown.
     */
    private fun parsePriceParameter(priceString: String?): Either<Boolean, Money?> {
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

}