package jhkim105.ssecompare.loadgen.scenario

import jhkim105.ssecompare.loadgen.ResultWriter
import jhkim105.ssecompare.loadgen.ScenarioConfig
import jhkim105.ssecompare.loadgen.SseClient
import jhkim105.ssecompare.loadgen.StageResult

object S4WithDb {
    suspend fun run(
        client: SseClient,
        targetUrl: String,
        config: ScenarioConfig,
        resultWriter: ResultWriter,
        overrideStages: List<Int>? = null,
    ): Pair<List<StageResult>, Int?> {
        val dbConfig = config.copy(persist = true)
        return S1RampUp.run(client, targetUrl, dbConfig, resultWriter, overrideStages)
    }
}
