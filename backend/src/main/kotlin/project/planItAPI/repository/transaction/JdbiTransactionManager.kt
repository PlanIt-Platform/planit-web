package project.planItAPI.repository.transaction

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component
import project.planItAPI.utils.Either
import project.planItAPI.utils.failure
import project.planItAPI.utils.success

@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): Either<Exception, R> {
        return try {
            val res = jdbi.inTransaction<R, Exception> { handle ->
                val transaction = JdbiTransaction(handle)
                block(transaction)
            }
            success(res)
        } catch (e: Exception) {
            failure(e)
        }
    }
}