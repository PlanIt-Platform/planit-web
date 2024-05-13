package project.planItAPI.domain.event

import project.planItAPI.utils.Either
import project.planItAPI.utils.Failure
import project.planItAPI.utils.HTTPCodeException
import project.planItAPI.utils.InvalidPriceFormatException
import project.planItAPI.utils.Success

typealias MoneyResult = Either<HTTPCodeException, Money>

class Money private constructor(
    val amount: Double,
    val currency: String
) {
    companion object {
            private val regex = """(\d+(\.\d+)?)(\s*([A-Za-z]+))""".toRegex()
            operator fun invoke(priceString: String): MoneyResult {
                val matchResult = regex.find(priceString)
                if (matchResult != null) {
                    val (amountStr, _, _, currency) = matchResult.destructured
                    val amount = amountStr.toDoubleOrNull() ?: return Failure(InvalidPriceFormatException())
                    return Success(Money(amount, currency))
                }

                return Failure(InvalidPriceFormatException())
            }
    }
}