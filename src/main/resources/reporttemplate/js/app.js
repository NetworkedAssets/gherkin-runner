Vue.component('callback', {
    props: ['report', 'title'],
    template: '#callback-template'
});

Vue.component('error', {
    props: ['report'],
    template: '#error-template'
});

Vue.component('log', {
    props: ['logs'],
    template: '#log-template'
});

Vue.component('chart', {
    props: ['title', 'statistics', 'icon'],
    template: '#chart-template',
    mounted: function () {
        var stepsChartCtx = $(this.$el).find("canvas")[0].getContext('2d');
        new Chart(stepsChartCtx, createChartConfig([
            this.statistics.passed,
            this.statistics.failed,
            this.statistics.skipped,
            this.statistics.notImplemented,
            this.statistics.multipleImplementatmions
        ]));
    }
});

new Vue({
    el: '#app',
    data: function data() {
        return {
            report: reportData,
            suiteName: reportData.suiteName,
            environment: reportData.environment,
            passed: reportData.state === "PASSED",
            startTime: moment(reportData.startTime).format("DD-MM-YYYY HH:mm:ss"),
            executionTime: humanizeExecutionTime(reportData.executionTime),
            endTime: moment(reportData.endTime).format("DD-MM-YYYY HH:mm:ss")
        }
    },
    mounted: function () {
        document.title = reportData.suiteName + " Report";

        $('.features-accordion, #logs .accordion').accordion({exclusive: false, animateChildren: false});

        var envButton = $('#environment').find('.massive.button');
        envButton.click(function () {
            envButton.toggleClass("active");
            $('#endpoints').slideToggle();
        });

        $('.smooth-goto').on('click', function () {
            $('html, body').animate({scrollTop: $(this.hash).offset().top - 50}, 500);
            return false;
        });
    }
});