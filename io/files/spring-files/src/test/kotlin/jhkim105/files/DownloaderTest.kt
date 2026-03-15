package jhkim105.files

import kotlin.test.Test

class DownloaderTest {


    @Test
    fun download() {
        Downloader.downloadFileFromUrl("https://www.remotemeeting.com/public/contents/src/img/main/img-reasonable-price-moneystack.png", "build/temp")
    }
}