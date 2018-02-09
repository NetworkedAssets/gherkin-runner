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