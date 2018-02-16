package com.networkedassets.gherkin.runner.report

import com.fasterxml.jackson.databind.node.ObjectNode
import com.networkedassets.gherkin.runner.annotation.ElasticSearchReporting
import com.networkedassets.gherkin.runner.report.data.FeatureReport
import com.networkedassets.gherkin.runner.report.data.Report
import com.networkedassets.gherkin.runner.report.data.ScenarioReport
import com.networkedassets.gherkin.runner.report.data.StepReport
import mu.KotlinLogging
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.cluster.ClusterName
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient
import java.net.InetAddress


object ElasticsearchReportExporter {
    val log = KotlinLogging.logger { }

    fun reportToElasticsearch(elasticSearchReporting: ElasticSearchReporting, report: Report) {
        val esAddress = elasticSearchReporting.address
        val esPort = elasticSearchReporting.port
        val esClusterName = elasticSearchReporting.clusterName
        val esIndex = elasticSearchReporting.index
        log.info { "Putting report into Elasticsaerch($esAddress:$esPort, cluster name: $esClusterName, index: $esIndex)" }
        try {
            val client = PreBuiltTransportClient(
                    Settings.builder().put(ClusterName.CLUSTER_NAME_SETTING.key, esClusterName).build()).addTransportAddress(
                    TransportAddress(InetAddress.getByName(esAddress), esPort))

            if(elasticSearchReporting.splitReport) {
                log.info { "Putting report splitted into indexes" }
                val reportResponse = client.putReport(esIndex, report.toContentBuilder())
                report.featureReports.forEach { featureReport ->
                    val featureReportResponse = client.putReport("${esIndex}_features", featureReport.toContentBuilder(reportResponse.id))
                    featureReport.scenarioReports.forEach { scenarioReport ->
                        val scenarioReportResponse = client.putReport("${esIndex}_scenarios",
                                scenarioReport.toContentBuilder(featureReportResponse.id))
                        scenarioReport.stepReports.forEach { stepReport ->
                            client.putReport("${esIndex}_steps", stepReport.toContentBuilder(scenarioReportResponse.id))
                        }
                    }
                }
            } else {
                log.info { "Putting whole report into one index" }
                client.putReport(esIndex, report.toFullContentBuilder())
            }
        } catch (t: Throwable) {
            log.error(t) { "Putting report into Elasticsearch failed" }
        }
        log.info { "Report successfully put into Elasticsearch" }
    }

    private fun Report.toFullContentBuilder(): XContentBuilder =
            this.toObjectNode().toContentBuilder()

    private fun Report.toContentBuilder(): XContentBuilder =
            this.toObjectNode("report", null, setOf("featureReports", "beforeReport", "afterReport", "log")).toContentBuilder()

    private fun FeatureReport.toContentBuilder(reportId: String): XContentBuilder =
            this.toObjectNode("featureReport", reportId, setOf("scenarioReports", "beforeReport", "afterReport", "log")).toContentBuilder()

    private fun ScenarioReport.toContentBuilder(featureReportId: String): XContentBuilder =
            this.toObjectNode("scenarioReport", featureReportId, setOf("stepReports", "beforeReport", "afterReport", "log")).toContentBuilder()

    private fun StepReport.toContentBuilder(scenarioReportId: String): XContentBuilder =
            this.toObjectNode("stepReport", scenarioReportId, setOf("beforeReport", "afterReport", "log")).toContentBuilder()

    private fun ObjectNode.toContentBuilder(): XContentBuilder {
        val contentBuilder = XContentFactory.jsonBuilder().prettyPrint()
        val parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, this.toString())
        parser.use { contentBuilder.copyCurrentStructure(it) }
        return contentBuilder
    }

    private fun Any.toObjectNode(type: String? = null, parentId: String? = null, keysToRemove: Set<String> = setOf()): ObjectNode {
        val objectNode = JSONSerializer.toObjectNode(this)
        if (parentId != null) objectNode.put("parentId", parentId)
        if (type != null) objectNode.put("type", type)
        keysToRemove.forEach { objectNode.remove(it) }
        return objectNode
    }

    private fun TransportClient.putReport(index: String, contentBuilder: XContentBuilder) =
            this.prepareIndex(index, "doc")
                    .setSource(contentBuilder)
                    .execute()
                    .actionGet()

}