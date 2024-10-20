package jhkim105.tutorials.batch.domain.service

import jhkim105.tutorials.batch.domain.port.`in`.GetPagingListUseCase
import org.springframework.stereotype.Service


//@Service
class PagingService : GetPagingListUseCase {
    override fun getPagingList(page: Int, pageSize: Int): List<Int> {
        val start = page * pageSize
        val end = start + pageSize

        return (start until end).toList()
    }
}