package project.planItAPI.repository.transaction

import daw53d08.gomoku.services.utils.Either
import daw53d08.gomoku.services.utils.failure
import daw53d08.gomoku.services.utils.success
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component

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