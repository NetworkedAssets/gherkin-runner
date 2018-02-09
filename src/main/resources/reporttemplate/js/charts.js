function createChartConfig(chartData) {
    var chartOptions = {
        responsive: true,
        legend: {
            display: false
        },
        animation: {
            animateScale: true,
            animateRotate: true
        }
    };

    var chartLabels = [
        "Passed",
        "Failed",
        "Skipped",
        "Not implemented",
        "Multiple implemented"
    ];

    var chartColors = [
        "#21ba45",
        "#db2828",
        "#2185d0",
        "#a4a4a4",
        "#fbbd08"
    ];

    return {
        type: 'doughnut',
        data: {
            datasets: [{
                data: chartData,
                backgroundColor: chartColors
            }],
            labels: chartLabels
        },
        options: chartOptions
    };
}

function parseFeaturesStateStatistics(report) {
    return parseStateStatistics(report, "Features")
}

function parseScenariosStateStatistics(report) {
    return parseStateStatistics(report, "Scenarios")
}

function parseStepsStateStatistics(report) {
    return parseStateStatistics(report, "Steps")
}

function parseStateStatistics(report, element) {
    return {
        total: report["numberOf" + element],
        passed: report["numberOfPassed" + element],
        failed: report["numberOfFailed" + element],
        skipped: report["numberOfSkipped" + element],
        notImplemented: report["numberOfNotImplemented" + element],
        multipleImplementatmions: report["numberOfMultipleImplementations" + element]
    }
}