package jhkim105.tutorials.batch.domain.port.`in`

import jhkim105.tutorials.batch.domain.model.Stock

interface GetStockListUseCase {

    fun getStockList(page: Int, pageSize: Int): List<Stock>
}