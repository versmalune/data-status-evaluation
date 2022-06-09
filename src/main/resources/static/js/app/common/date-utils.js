function isValidStartAndEnd(started, ended) {
    const starteIntreger = new Date(started).getTime();
    const endIntreger = new Date(ended).getTime();

    if (starteIntreger <= endIntreger) {
        return true;
    } else {
        return false;
    }
}

var DatePicker = function (options) {
    this.options = _.cloneDeep(options);

    this.initEvent();
}

DatePicker.prototype.initEvent = function () {
    $('.input-daterange input').each(function () {
        $(this).daterangepicker({
            opens: "center",
            autoUpdateInput: false,
            autoApply: true,
            locale: {
                applyLabel: "적용",
                cancelLabel: '초기화',
                daysOfWeek: ["일", "월", "화", "수", "목", "금", "토"],
                monthNames: ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"],
            }
        });
        $(this).on('apply.daterangepicker', function (ev, picker) {
            $(this).val(picker.startDate.format('YYYY년 MM월 DD일') + ' - ' + picker.endDate.format('YYYY년 MM월 DD일'));
        });
    });
}

DatePicker.prototype.setDate = function (schedules) {
    for (var i = 0; i < schedules.length; i++) {
        var selector = `#input-daterange${i} input`;
        this.setStartDate(selector, schedules[i].beginDt);
        this.setEndDate(selector, schedules[i].endDt);
    }
}

DatePicker.prototype.setStartDate = function (selector, startDate) {
    var element = selector == null ? $('.input-daterange input') : element = $(selector);
    element.data('daterangepicker').setStartDate(new Date(startDate).toLocaleDateString("en-US"));
}

DatePicker.prototype.setEndDate = function (selector, endDate) {
    var element = selector == null ? $('.input-daterange input') : element = $(selector);
    element.data('daterangepicker').setEndDate(new Date(endDate).toLocaleDateString("en-US"));
}