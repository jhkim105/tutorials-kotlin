package jhkim105.tutorials.batch.adapter

import jhkim105.tutorials.batch.domain.port.`in`.GetPagingListUseCase
import jhkim105.tutorials.batch.domain.service.PagingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {

    @Bean
    fun getPagingListUseCase(): GetPagingListUseCase = PagingService()

}