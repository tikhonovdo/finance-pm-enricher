package ru.tikhonovdo.enrichment.batch.matching.transaction.base

import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.jdbc.core.JdbcTemplate

class ActualizeMatchedTransactionsTasklet(private val jdbcTemplate: JdbcTemplate): Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        val updated = jdbcTemplate.update("""
        UPDATE financepm.transaction SET 
            name = mt.name, type = mt.type, category_id = mt.category_id, date = mt.date, sum = mt.sum,
            account_id = mt.account_id, description = mt.description, event_id = mt.event_id 
        FROM matching.transaction mt
        WHERE matching_transaction_id IS NOT NULL AND matching_transaction_id = mt.id;
        """.trimIndent())
        contribution.incrementWriteCount(updated.toLong())
        return RepeatStatus.FINISHED
    }

}