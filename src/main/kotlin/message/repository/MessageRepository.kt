package message.repository

import message.model.MessageDocument
import message.model.MessageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.temporal.ChronoUnit

@Repository
class MessageRepository(
    val mongoTemplate: MongoTemplate
) {

    fun saveMessage(message: MessageRequest): MessageDocument {

        val messageToSave = MessageDocument(id = null,senderId = message.senderId, recipientId = message.recipientId,
            content = message.content, sentAt = Instant.now().toString())
       return mongoTemplate.insert(messageToSave)
    }

    fun getMessagesBySenderAndRecipientIdId(
        senderId: String,
        recipientId: String,
        limit: Boolean)
    : List<MessageDocument> {
        val getMessagesBySenderAndRecipientIdCriteria = Criteria.where(SENDER_ID).isEqualTo(senderId)
            .and(RECIPIENT_ID).isEqualTo(recipientId)
        val getMessagesBySenderAndRecipientIdQuery =
            buildQueryForLimitOrRecentMessages(limit,getMessagesBySenderAndRecipientIdCriteria)
        return mongoTemplate.find(getMessagesBySenderAndRecipientIdQuery, MessageDocument::class.java)
    }

    fun getMessagesByRecipientId(
        recipientId: String,
        limit: Boolean
    ) : List<MessageDocument> {
        val getMessagesByRecipientIdCriteria = Criteria.where(RECIPIENT_ID).isEqualTo(recipientId)

        val getMessagesByRecipientIdQuery =
            buildQueryForLimitOrRecentMessages(limit,getMessagesByRecipientIdCriteria)
        return mongoTemplate.find(getMessagesByRecipientIdQuery, MessageDocument::class.java)
    }

    fun getMessagesForEveryone(
        limit: Boolean
    ) : List<MessageDocument> {
        val messagesForEveryone = buildQueryForLimitOrRecentMessages(limit, Criteria())
        return mongoTemplate.find(messagesForEveryone, MessageDocument::class.java)
    }

    private fun buildQueryForLimitOrRecentMessages(limit: Boolean, queryCriteria: Criteria): Query {

        //If limit, set query to limits by 100 (limit_size)
        //if not limit, add to criteria to make sure we only get last month (30 days) of messages
        return if (limit) {
            Query().addCriteria(queryCriteria).limit(LIMIT_SIZE)
        } else {
            queryCriteria.and(SENT_AT)
                .gte(Instant.now().minus(DAYS_IN_MONTH,ChronoUnit.DAYS).toString())
            Query().addCriteria(queryCriteria)
        }
    }

    //Define MongoDB document field names here to use everywhere to be consistent with naming
    companion object {
        const val SENDER_ID = "sender_id"
        const val RECIPIENT_ID = "recipient_id"
        const val SENT_AT = "sent_at"
        const val CONTENT = "content"
        const val LIMIT_SIZE = 100
        const val DAYS_IN_MONTH: Long = 30
    }
}