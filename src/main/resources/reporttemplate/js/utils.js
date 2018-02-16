function humanizeExecutionTime(executionTime) {
    var duration = moment.duration(executionTime);
    if(duration.hours() > 0) {
        return duration.format("h [hrs], m [min]");
    } else if(duration.minutes() > 0) {
        return duration.format("m [min], s [sec]");
    } else if(duration.seconds() > 0) {
        return duration.format("s [sec]");
    } else {
        return duration.format("S [ms]");
    }
}

function getStyleClasses(element) {
    return {
        skipped: element.state === 'SKIPPED',
        failed: element.state === 'FAILED',
        passed: element.state === 'PASSED',
        'not-implemented': element.state === 'NOT_IMPLEMENTED',
        'multiple-implementations': element.state === 'MULTIPLE_IMPLEMENTATIONS'
    }
}

function processData(data) {
    data.featureReports = data.featureReports.map(function(featureReport) {
        featureReport.fullTree = [featureReport.feature.name];
        if(featureReport.beforeReport) {
            featureReport.beforeReport.fullTree = featureReport.fullTree.slice();
            featureReport.beforeReport.fullTree.push("Before feature")
        }
        if(featureReport.afterReport) {
            featureReport.afterReport.fullTree = featureReport.fullTree.slice();
            featureReport.afterReport.fullTree.push("After feature");
        }
        featureReport.scenarioReports = featureReport.scenarioReports.map(function (scenarioReport) {
            scenarioReport.fullTree = featureReport.fullTree.slice();
            scenarioReport.fullTree.push(scenarioReport.scenario.name);
            scenarioReport.stepReports = scenarioReport.stepReports.map(function (stepReport) {
                stepReport.fullTree = scenarioReport.fullTree.slice();
                stepReport.fullTree.push(stepReport.step.fullContent);
                return stepReport
            });
            if(scenarioReport.beforeReport) {
                scenarioReport.beforeReport.fullTree = scenarioReport.fullTree.slice();
                scenarioReport.beforeReport.fullTree.push("Before scenario");
            }
            if(scenarioReport.afterReport) {
                scenarioReport.afterReport.fullTree = scenarioReport.fullTree.slice();
                scenarioReport.afterReport.fullTree.push("After scenario");
            }
            return scenarioReport
        });
        return featureReport
    });
    return data
}

reportData = processData(reportData);