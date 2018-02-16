var callbackComponent = {
    props: ['report', 'title'],
    template: '#callback-template',
    methods: {
        showLogs: function () {
            this.$emit('showlogs');
        }
    }
};

var callbackRowComponent = {
    props: ['report', 'title'],
    template: '#callback-row-template',
    methods: {
        showLogs: function () {
            this.$emit('showlogs');
        },
        showError: function () {
            this.$emit('showerror');
        }
    }
};

var errorComponent = {
    props: ['report'],
    template: '#error-template'
};

var chartComponent = {
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
};

var stepRowComponent = {
    props: ['stepreport'],
    template: '#step-row-template',
    methods: {
        showLogs: function () {
            this.$emit('showlogs');
        },
        showError: function () {
            this.$emit('showerror');
        }
    }
};

var featureStateComponent = {
    props: ['state'],
    template: '#feature-state-template',
    data: function () {
        return {
            color: this.stateToColor(this.state),
            text: this.stateToText(this.state)
        }
    },
    methods: {
        stateToColor: function (state) {
            switch(state) {
                case "PASSED": return 'green';
                case "FAILED": return 'red';
                case "SKIPPED": return 'blue';
                case "NOT_IMPLEMENTED": return 'grey';
                case "MULTIPLE_IMPLEMENTATIONS": return 'yellow';
            }
        },
        stateToText: function (state) {
            switch(state) {
                case "PASSED": return 'Passed';
                case "FAILED": return 'Failed';
                case "SKIPPED": return 'Skipped';
                case "NOT_IMPLEMENTED": return 'Not implemented';
                case "MULTIPLE_IMPLEMENTATIONS": return 'Multiple implementations';
            }
        }
    }
};


new Vue({
    el: '#app',
    data: function () {
        return {
            report: reportData,
            suiteName: reportData.suiteName,
            environment: reportData.environment,
            passed: reportData.state === "PASSED",
            startDate: moment(reportData.startTime).format("DD-MM-YYYY"),
            startTime: moment(reportData.startTime).format("HH:mm:ss"),
            executionTime: humanizeExecutionTime(reportData.executionTime),
            endDate: moment(reportData.endTime).format("DD-MM-YYYY"),
            endTime: moment(reportData.endTime).format("HH:mm:ss"),
            flat: false,
            currentReport: null
        }
    },
    methods: {
        showLogs: function (report) {
            this.currentReport = report;
            $('#logs-modal').modal('show');
        },
        showError: function (report) {
            this.currentReport = report;
            $('#error-modal').modal('show');
        }
    },
    updated: function () {
        $('#logs-modal').modal('refresh');
        $('#error-modal').modal('refresh');
    },
    mounted: function () {
        document.title = reportData.suiteName + " Report";

        $('.feature-accordion, #logs .accordion').accordion({exclusive: false, animateChildren: false});
        $('.ui.accordion.feature-accordion > .title, .ui.accordion.scenario-accordion > .title, #logs .ui.accordion > .title').click()

        var envButton = $('.environment-button');
        envButton.click(function () {
            envButton.toggleClass("active");
            $('#endpoints').slideToggle();
        });

        $('.smooth-goto').on('click', function (e) {
            console.log("click")
            $('html, body').animate({scrollTop: $(this.hash).offset().top - 99}, 500);
            $('.ui.dropdown').dropdown('hide');
            if (window.event) { // ie11
                window.event.returnValue = false;
                window.event.cancelBubble = true;
            } else if (e) {
                e.stopPropagation();
                e.preventDefault();
            }
        });

        setTimeout(function () {
            $('#preloader').fadeOut(1000);
        }, 300)

        $(window).on('scroll', function () {
            var featuresSummary = $(".report-table");
            var bottomOfFeaturesSummary = featuresSummary.offset().top + featuresSummary.outerHeight(true);
            var featuresSection = $("#features");
            var bottomOfFeaturesSection = featuresSection.offset().top + featuresSection.outerHeight(true);

            var currentPosition = $(window).scrollTop();
            var backToFeaturesSummaryButtton = $(".back-to-features-summary");
            if(currentPosition > bottomOfFeaturesSummary && currentPosition < bottomOfFeaturesSection)
                backToFeaturesSummaryButtton.fadeIn();
            else
                backToFeaturesSummaryButtton.fadeOut();

        });

        $('.ui.dropdown').dropdown();
    },
    components: {
        'callback': callbackComponent,
        'callbackrow': callbackRowComponent,
        'error': errorComponent,
        'chart': chartComponent,
        'steprow': stepRowComponent,
        'featurestate': featureStateComponent
    }
});