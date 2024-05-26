package project.planItAPI.repository.jdbi.poll

import org.jdbi.v3.core.Handle
import project.planItAPI.domain.poll.Option
import project.planItAPI.models.OptionVotesModel
import project.planItAPI.models.PollOutputModel

class JdbiPollRepository (private val handle: Handle): PollRepository {

    override fun createPoll(
        title: String,
        options: List<String>,
        duration: Int,
        eventId: Int,
        organizerId: Int
    ): Int {
        val pollId = handle.createUpdate(
            "INSERT INTO dbo.Polls (title, duration, organizer_id, event_id)" +
                    " VALUES (:title, :duration,:organizerId, :eventId)"
        )
            .bind("title", title)
            .bind("duration", duration)
            .bind("organizerId", organizerId)
            .bind("eventId", eventId)
            .executeAndReturnGeneratedKeys()
            .mapTo(Int::class.java)
            .one()


        options.forEach { option ->
            handle.createUpdate(
                "INSERT INTO dbo.Options (text, poll_id)" +
                        " VALUES (:text, :pollId)"
            )
                .bind("text", option)
                .bind("pollId", pollId)
                .execute()
        }

        return pollId
    }

    override fun getPoll(pollId: Int): PollOutputModel? {
        val pollDetails = handle.createQuery(
            """
        SELECT title, duration
        FROM dbo.Polls
        WHERE id = :pollId
        """
        )
            .bind("pollId", pollId)
            .mapToMap()
            .firstOrNull()

        val options = handle.createQuery(
            """
        SELECT o.text, COUNT(uv.user_id) as votes
        FROM dbo.Options o
        LEFT JOIN dbo.UserVotes uv ON o.id = uv.option_id
        WHERE o.poll_id = :pollId
        GROUP BY o.text
        """
        )
            .bind("pollId", pollId)
            .map { rs, _ -> OptionVotesModel(rs.getString("text"), rs.getInt("votes")) }
            .list()

        return if (pollDetails != null) {
            PollOutputModel(
                title = pollDetails["title"] as String,
                duration = pollDetails["duration"] as Int,
                options = options
            )
        } else null
    }

    override fun deletePoll(pollId: Int) {
        handle.createUpdate(
            "delete from dbo.Options where poll_id = :pollId"
        )
            .bind("pollId", pollId)
            .execute()

        handle.createUpdate(
            "delete from dbo.Polls where id = :pollId"
        )
            .bind("pollId", pollId)
            .execute()
    }

    override fun getOption(optionId: Int): String? {
        return handle.createQuery(
            """
        SELECT text
        FROM dbo.Options
        WHERE id = :optionId
        """
        )
            .bind("optionId", optionId)
            .mapTo(String::class.java)
            .firstOrNull()
    }

    override fun checkIfUserVoted(userId: Int, pollId: Int): Boolean {
        return handle.createQuery(
            """
        SELECT COUNT(*)
        FROM dbo.UserVotes
        WHERE user_id = :userId AND poll_id = :pollId
        """
        )
            .bind("userId", userId)
            .bind("pollId", pollId)
            .mapTo(Int::class.java)
            .first() > 0
    }

    override fun vote(pollId: Int, userId: Int, optionId: Int) {
        handle.createUpdate(
            """
        INSERT INTO dbo.UserVotes (user_id, poll_id, option_id)
        VALUES (:userId, :pollId, :optionId)
        """
        )
            .bind("userId", userId)
            .bind("pollId", pollId)
            .bind("optionId", optionId)
            .execute()
    }
}