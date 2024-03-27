package project.planItAPI.repository.transaction

import project.planItAPI.utils.Either

/**
 * Interface for managing transactions involving repository interactions.
 */
interface TransactionManager {
    /**
     * Runs a transactional block of code, providing a [Transaction] context.
     *
     * @param block The block of code to be executed within a transactional context.
     * @return The result of the transactional block.
     */
    fun <R> run(block: (Transaction) -> R): Either<Exception, R>
}