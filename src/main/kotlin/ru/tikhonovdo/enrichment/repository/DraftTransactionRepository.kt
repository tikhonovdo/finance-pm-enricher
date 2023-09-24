package ru.tikhonovdo.enrichment.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.tikhonovdo.enrichment.domain.Bank
import ru.tikhonovdo.enrichment.domain.enitity.CategoryMatching
import ru.tikhonovdo.enrichment.domain.enitity.DraftTransaction
import java.time.LocalDateTime

interface DraftTransactionRepository : JpaRepository<DraftTransaction, Long>,
    BatchRepository<DraftTransaction>, CustomDraftTransactionRepository {
    fun findAllByBankIdAndDate(bankId: Long, date: LocalDateTime): List<DraftTransaction>
}

interface CustomDraftTransactionRepository {
    fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching>

    fun findAllByBankId(bankId: Long): List<DraftTransaction>
}
@Repository
class DraftTransactionRepositoryImpl(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate
): CustomDraftTransactionRepository, AbstractBatchRepository<DraftTransaction>(
    namedParameterJdbcTemplate,
    "INSERT INTO matching.draft_transaction (bank_id, date, sum, data) VALUES (:bankId, :date, :sum, :data::json)"
) {
    override fun findAllCategoryMatchingCandidates(bank: Bank): List<CategoryMatching> {
        return namedParameterJdbcTemplate.query("""
            SELECT DISTINCT ON (items.item->>'category', items.item->>'mcc')
                items.item->>'category' as bankCategoryName,
                items.item->>'mcc' as mcc,
                items.item->>'description' as pattern
            FROM
                 (SELECT jsonb_array_elements(data) item
                  FROM matching.draft_transaction
                  WHERE bank_id = :bankId) items;
            """.trimIndent(),
            MapSqlParameterSource(mapOf("bankId" to bank.id))
        ) { rs, _ ->
                CategoryMatching(
                    bankId = bank.id,
                    bankCategoryName = rs.getString("bankCategoryName"),
                    mcc = rs.getInt("mcc").toString(),
                    pattern = rs.getString("pattern"),
                    categoryId = null
                )
        }
    }

    override fun findAllByBankId(bankId: Long): List<DraftTransaction> {
        return namedParameterJdbcTemplate.query(
            """
                SELECT date, sum, data
                FROM matching.draft_transaction
                WHERE bank_id = :bankId""".trimIndent(),
            MapSqlParameterSource(mapOf("bankId" to bankId))
        ) { rs, _ ->
            DraftTransaction(
                bankId = bankId,
                date = rs.getTimestamp("date").toLocalDateTime(),
                sum = rs.getString("sum"),
                data = rs.getString("data")
            )
        }
    }

}
