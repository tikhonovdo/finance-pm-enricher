package ru.tikhonovdo.enrichment.old.processor

import ru.tikhonovdo.enrichment.domain.dto.TinkoffRecord

class TinkoffRecordProcessor(
    private val transactionProcessor: TransactionProcessor,
    private val transfersProcessor: TransferProcessor
) {

    fun enrich(records: List<TinkoffRecord>) {
        val transfersPairs = transfersProcessor.findTransfers(records)
        transactionProcessor.performProcessing(records.without(transfersPairs))
        transfersProcessor.addTransfers(transfersPairs)
    }

    private fun List<TinkoffRecord>.without(pairs: List<Pair<TinkoffRecord, TinkoffRecord>>) =
        this.minus(pairs.flatMap { listOf(it.first, it.second) }.toSet())
}