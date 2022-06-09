var HomeChartManager = function (options) {
    this.options = _.cloneDeep(options);

    this.colorMap = [
        am4core.color("#42a5f5"),
        am4core.color("#6f42c1"),
        am4core.color("#845EC2"),
        am4core.color("#FF9671"),
        am4core.color("#FFC75F"),
        am4core.color("#F9F871"),
        am4core.color("#2908a1"),
        am4core.color("#1976d2"),
        am4core.color("#1f6bff"),
        am4core.color("#0b70b9"),
    ]
    am4core.options.queue = true;
    // Themes begin
    //     am4core.useTheme(am4themes_animated);
    // Themes end
}

HomeChartManager.prototype.render = function (container, data) {
    var self = this;
    data.forEach((el, idx) => {
        el["color"] = this.colorMap[idx]
    })

    am4core.ready(function () {
        // Create chart instance
        var chart = am4core.create(container, am4charts.PieChart);
        // Add data
        chart.data = data;

        // Add label
        chart.innerRadius = am4core.percent(30);
        var label = chart.seriesContainer.createChild(am4core.Label);
        // label.text = "70%";
        label.adapter.add("text", function (text, target) {
            var chartValue = pieSeries.dataItem.values.value;

            return "[font-size:18px]전체\n[bold font-size:30px]" + pieSeries.dataItem.values.value.sum + "[/]";
        })
        label.horizontalCenter = "middle";
        label.verticalCenter = "middle";

        // Add and configure Series
        var pieSeries = chart.series.push(new am4charts.PieSeries());
        pieSeries.slices.template.stroke = am4core.color("#fff");
        pieSeries.slices.template.strokeWidth = 2;
        pieSeries.slices.template.strokeOpacity = 1;
        pieSeries.slices.template.cursorOverStyle = [
            {
                "property": "cursor",
                "value": "pointer"
            }
        ];

        pieSeries.dataFields.value = "count";
        pieSeries.dataFields.category = "title";
        pieSeries.slices.template.propertyFields.fill = "color";
        pieSeries.alignLabels = false;
        pieSeries.labels.template.bent = true;
        pieSeries.labels.template.radius = 3;
        pieSeries.labels.template.padding(0, 0, 0, 0);

        chart.legend = new am4charts.Legend();
        var legendContainer = am4core.create($("#" + container).parent().find(".chart-legend").attr("id"), am4core.Container);
        legendContainer.width = am4core.percent(100);
        legendContainer.height = am4core.percent(100);
        chart.legend.parent = legendContainer;

        pieSeries.labels.template.disabled = true;
        pieSeries.ticks.template.disabled = true;

        // Create a base filter effect (as if it's not there) for the hover to return to
        var shadow = pieSeries.slices.template.filters.push(new am4core.DropShadowFilter);
        shadow.opacity = 0;

// Create hover state
        var hoverState = pieSeries.slices.template.states.getKey("hover"); // normally we have to create the hover state, in this case it already exists

// Slightly shift the shadow and make it more prominent on hover
        var hoverShadow = hoverState.filters.push(new am4core.DropShadowFilter);
        hoverShadow.opacity = 0.7;
        hoverShadow.blur = 5;
    });
}

HomeChartManager.prototype.renderRadar = function (container, data, legend1, legend2) {
    var chart;
    am4core.ready(function () {
        /* Create chart instance */
        chart = am4core.create(container, am4charts.RadarChart);

        /* Add data */
        chart.data = data;

        /* Create axes */
        var categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
        categoryAxis.dataFields.category = "title";
        categoryAxis.renderer.gridType = "polygons"
        categoryAxis.renderer.labels.template.fontSize = 12;

        var valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
        valueAxis.renderer.axisFills.template.fill = chart.colors.getIndex(2);
        valueAxis.renderer.axisFills.template.fillOpacity = 0.05;
        valueAxis.renderer.gridType = "polygons"
        valueAxis.renderer.minGridDistance = 20;
        valueAxis.renderer.labels.template.fontSize = 12;
        valueAxis.min = 0

        /* Create and configure series */
        var series1 = chart.series.push(new am4charts.RadarSeries());
        series1.dataFields.valueY = "count1";
        series1.dataFields.categoryX = "title";
        series1.name = legend1;
        series1.strokeWidth = 3;

        /* Create and configure series */
        var series2 = chart.series.push(new am4charts.RadarSeries());
        series2.dataFields.valueY = "count2";
        series2.dataFields.categoryX = "title";
        series2.name = legend2;
        series2.strokeWidth = 3;

        chart.legend = new am4charts.Legend();
        chart.legend.position = "bottom"
        chart.legend.paddingTop = 20;
        chart.legend.fontSize = 12;
        // var legendContainer = am4core.create($("#" + container).parent().find(".chart-legend").attr("id"), am4core.Container);
        // legendContainer.width = am4core.percent(100);
        // legendContainer.height = am4core.percent(100);
        // chart.legend.parent = legendContainer;

    }); // end am4core.ready()
    return chart;
}

HomeChartManager.prototype.renderStack = function (container, data) {
    var chart;
    am4core.ready(function () {
// Create chart instance
        chart = am4core.create(container, am4charts.XYChart);
// Add data
        chart.data = data;
// Create axes
        var categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
        categoryAxis.dataFields.category = "institution";
        categoryAxis.renderer.labels.template.disabled = true;
        categoryAxis.renderer.grid.template.location = 0;
        categoryAxis.renderer.inversed = true;
        categoryAxis.renderer.labels.template.fontSize = 12;

        var valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
        // valueAxis.renderer.inside = true;
        // valueAxis.renderer.labels.template.disabled = true;
        valueAxis.min = 0;
        valueAxis.max = 100;
        valueAxis.strictMinMax = true;
        valueAxis.calculateTotals = true;
        valueAxis.renderer.labels.template.fontSize = 12;

// Create series
        function createSeries(field, name, stacked) {
            // Set up series
            var series = chart.series.push(new am4charts.ColumnSeries());
            series.name = name;
            series.dataFields.valueY = field;
            series.dataFields.categoryX = "institution";
            series.sequencedInterpolation = true;

            // Make it stacked
            series.stacked = stacked;

            // // Configure columns
            // series.columns.template.width = am4core.percent(60);
            // series.columns.template.tooltipText = "[bold]{name}[/]\n[font-size:14px]{categoryX}: {valueY}";
            //
            // // Add label
            // var labelBullet = series.bullets.push(new am4charts.LabelBullet());
            // // labelBullet.label.text = "{valueY}";
            // labelBullet.locationY = 0.5;
            // labelBullet.label.hideOversized = true;

            return series;
        }

        function calculateTotalsOnData(valueDataFieldsToInclude) {
            am4core.array.each(chart.data, item => {
                var total = 0;
                valueDataFieldsToInclude.forEach(valueDataField => {
                    if (item[valueDataField])
                        total += item[valueDataField];
                });
                item["total"] = total;
            });
        }

        var fields = [];
        Object.keys(data[0]).forEach(key => {
            if (!key.includes("institution") && !key.includes("total")) {
                createSeries(key, key, true)
                fields.push(key);
            }
        })
        var totalSeries = createSeries("total", "total", false);
        totalSeries.hidden = true;
        totalSeries.hiddenInLegend = true;
        categoryAxis.sortBySeries = totalSeries;

        chart.events.on('beforedatavalidated', evt => {
            calculateTotalsOnData(fields);
            chart.invalidateRawData();
        });

        chart.events.on('ready', () => {
            // workaround to get "inital" sorting right
            // chart.invalidateRawData();
            // chart.exporting.export("png");
        });

// Legend
        chart.legend = new am4charts.Legend();
        chart.legend.position = "top"
        chart.legend.paddingBottom = 20;
        chart.legend.fontSize = 12;
        chart.legend.fill = am4core.color("#FFFFFF");
        // var legendContainer = am4core.create($("#" + container).parent().find(".chart-legend").attr("id"), am4core.Container);
        // legendContainer.width = am4core.percent(100);
        // legendContainer.height = am4core.percent(100);
        // chart.legend.parent = legendContainer;

    }); // end am4core.ready()
    return chart;
}

HomeChartManager.prototype.renderColumn = function (container, data, index) {
    var chart;
    am4core.ready(function () {
        chart = am4core.create(container, am4charts.XYChart);
        chart.padding(15, 30, 55, 10); // top, right, bottom, left

        var categoryAxis = chart.yAxes.push(new am4charts.CategoryAxis());
        categoryAxis.renderer.grid.template.location = 0;
        categoryAxis.dataFields.category = "title";
        categoryAxis.renderer.minGridDistance = 1;
        categoryAxis.renderer.inversed = true;
        // categoryAxis.renderer.inside = true;
        // categoryAxis.renderer.labels.template.disabled = true;
        categoryAxis.renderer.labels.template.fontSize = 12;
        // categoryAxis.renderer.grid.template.disabled = true;

        var valueAxis = chart.xAxes.push(new am4charts.ValueAxis());
        valueAxis.min = 0;
        valueAxis.renderer.labels.template.fontSize = 12;

        var series = chart.series.push(new am4charts.ColumnSeries());
        series.dataFields.categoryY = "title";
        series.dataFields.valueX = "count";
        series.tooltipText = "[bold font-size:30px]{valueX.value}"
        series.tooltip.label.disabled = true
        series.columns.template.strokeOpacity = 0;
        series.columns.template.column.cornerRadiusBottomRight = 5;
        series.columns.template.column.cornerRadiusTopRight = 5;
        series.columns.template.propertyFields.fill = "color";

        var labelBullet = series.bullets.push(new am4charts.LabelBullet())
        labelBullet.label.horizontalCenter = "left";
        labelBullet.label.dx = 10;
        labelBullet.label.fontSize = 12
        labelBullet.label.text = "{values.valueX.workingValue.formatNumber('#.')}";
        labelBullet.locationX = 0;

        // var categoryBullet = series.bullets.push(new am4charts.LabelBullet())
        // categoryBullet.label.horizontalCenter = "right";
        // categoryBullet.label.fontSize = 12;
        // categoryBullet.label.dx = -10;
        // categoryBullet.label.fill = am4core.color("#000");
        // categoryBullet.label.text = "{title}";

        data.forEach((el, idx) => {
            if (idx === index) {
                el["color"] = am4core.color("#1f6bff");
            } else {
                el["color"] = am4core.color("#869ac0");
            }
        })
        // categoryAxis.sortBySeries = series;
        chart.data = data;

    }); // end am4core.ready()
    return chart;
}

HomeChartManager.prototype.preprocess = function (map) {
    var data = [];
    for (var key of Object.keys(map)) {
        var obj = {
            "title": key,
            "count": map[key],

        }
        data.push(obj);
    }
    return data;
}