package jhkim105.tutorials


import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class MvcConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        // case-insensitive enum mapping (LenientStringToEnumConverterFactory)
        ApplicationConversionService.configure(registry);
    }
}

