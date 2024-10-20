package jhkim105.tutorials.batch.domain.port.`in`


interface GetPagingListUseCase {

    fun getPagingList(page: Int, pageSize: Int): List<Int>
}