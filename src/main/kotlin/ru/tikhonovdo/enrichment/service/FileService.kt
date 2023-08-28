package ru.tikhonovdo.enrichment.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.tikhonovdo.enrichment.domain.FileType
import ru.tikhonovdo.enrichment.domain.FileType.*
import ru.tikhonovdo.enrichment.service.worker.FinancePmFileWorker
import ru.tikhonovdo.enrichment.service.worker.TinkoffFileWorker

interface FileService {
    fun store(file: MultipartFile)
    fun load() : Resource
}

interface FileServiceWorker {
    fun saveData(file: MultipartFile)
}

@Service
class FileServiceImpl(
    private val financePmFileWorker: FinancePmFileWorker,
    tinkoffFileWorker: TinkoffFileWorker
) : FileService {

    private val log = LoggerFactory.getLogger(FileServiceImpl::class.java)
    private val workers = mutableMapOf<FileType, FileServiceWorker>()

    init {
        workers[FINANCE_PM] = financePmFileWorker
        workers[TINKOFF] = tinkoffFileWorker
    }

    override fun store(file: MultipartFile) {
        val fileType = detectFileType(file)

        workers[fileType]?.let {
            it.saveData(file)
            log.info("$fileType data file was successfully saved")
        }
    }

    override fun load() = ByteArrayResource(financePmFileWorker.retrieveData())

    private fun detectFileType(file: MultipartFile): FileType {
        if (file.originalFilename?.matches(Regex("finance(.*).data")) == true) {
            return FINANCE_PM
        }
        if ((file.contentType == "application/vnd.ms-excel" || file.contentType == "text/csv") &&
            file.originalFilename?.matches(Regex("operations(.*)")) == true) {
            return TINKOFF
        }
        throw IllegalStateException("unknown file type")
    }

}