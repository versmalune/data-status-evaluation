var IndexRateManager = function (options) {
    this.options = _.cloneDeep(options);
    this.$form = this.options.$form;

    this.rateTemplate = Handlebars.getTemplate("index/rate");
    this.assignIndexTemplate = Handlebars.getTemplate("account/assign-index");

    this.initEvent();
}

IndexRateManager.prototype.initEvent = function () {
    this.formSubmitBeforeEvent();
    this.initRate();
    this.addRateBtnClickEvent();
    this.changeRateEvent();
    this.removeRateBtnClickEvent();
    this.indexSelectChangeEvent();
    this.assignedIndexDeleteBtnClickEvent();
}

IndexRateManager.prototype.formSubmitBeforeEvent = function () {
    var self = this;

    self.$form.on("submit", function (e) {
        e.preventDefault();

        var hasError = false;

        var reteRows = $(".rate-row");

        reteRows.each(function (idx) {
            if (idx === reteRows.length - 1) {
                // 마지막 등급은 무조건 100으로 저장되도록
                var $inputRate = $(this).find("input.rate");
                $inputRate.val(100);
            } else {
                var preInputRate = $(reteRows[idx-1]).find("input.rate").val();
                var $inputRate = $(this).find("input.rate");
                var inputRate = $inputRate.val();

                if (preInputRate >= inputRate) {
                    hasError = true;
                    $inputRate.val("");
                    alert("이전 등급의 비율보다 큰 값을 입력해주세요.");
                    $inputRate.focus();
                    return;
               }
            }
        });

        if (!hasError) {
            self.$form[0].submit();
        }
    });
}

IndexRateManager.prototype.initRate = function () {
    var self = this;

    var reteRows = $(".rate-row");
    reteRows.each(function (idx) {
        var removeRateBtn = $(this).find(".remove-rate-btn");
        var $inputRate = $(this).find("input.rate");
        var inputRate = $inputRate.val();
        var ratePostfix = $(this).find(".rate-postfix");

        if (idx === reteRows.length - 1) {
            if (idx > 0) {
                var preInputRate = $(reteRows[idx-1]).find("input.rate").val();
                $inputRate.val(preInputRate);
                inputRate = $inputRate.val()
            }
            if (idx > 1) {
                removeRateBtn.removeClass("d-none");
            }
            ratePostfix.text("( " + inputRate + "% 초과 ~ 100.0 % 이하 )");
            $inputRate.attr("readonly", true);
        } else {
            if (idx === 0) {
                ratePostfix.text("( 0.0 % 이상 ~ " + inputRate + " % 이하 )");
            } else {
                var preInputRate = $(reteRows[idx-1]).find("input.rate").val();
                ratePostfix.text("( " + preInputRate + " % 초과 ~ " + inputRate + " % 이하 )");
            }
            $inputRate.attr("readonly", false);
            removeRateBtn.addClass("d-none");
        }
    })
}

IndexRateManager.prototype.addRateBtnClickEvent = function () {
    var self = this;

    self.$form.on("click", "#add-rate-btn", function () {
        var rateRows = $(".rate-row");
        if (rateRows.length > 0) {
            var lastRateRow = rateRows.last();
            lastRateRow.after(self.rateTemplate({
                level: lastRateRow.index() + 1,
                idx: lastRateRow.index()
            }));
            self.initRate();
        }
    })
}

IndexRateManager.prototype.changeRateEvent = function () {
    var self = this;

    self.$form.on("change", "input.rate", function (e) {
        var value = $(this).val();
        if (value.length > 0) {
            var pattern = /(^[0-9]+$)|(^[0-9]+\.[0-9]{0,1}$)/gi;
            if (!isNaN(value)) {
                var fValue = parseFloat(value);
                if (!pattern.test(value) || fValue < 0 || 100 < fValue) {
                    $(this).val("");
                    alert($(this).attr('title'));
                }
            } else {
                $(this).val("");
                alert($(this).attr('title'));
            }
        }

        self.initRate();
    });
}

IndexRateManager.prototype.removeRateBtnClickEvent = function () {
    var self = this;

    self.$form.on("click", ".remove-rate-btn", function () {
        $(this).parent().parent().remove();

        self.initRate();
    })
}

IndexRateManager.prototype.indexSelectChangeEvent = function () {
    var self = this;

    var $assignedSection = self.$form.find(".indices-assign-section");
    self.$form.on("change", "#indices-select", function () {
        if ($assignedSection.find("#index-" + $(this).val()).length < 1) {
            $assignedSection.append(self.assignIndexTemplate({
                id: $(this).val(),
                text: $(this).find("option:selected").text(),
                name: `indices[${$assignedSection.children().length}].id`,
                value: $(this).find("option:selected").val(),
            }));
            self.assignedIndexDeleteBtnClickEvent();
        }
    })
}

IndexRateManager.prototype.assignedIndexDeleteBtnClickEvent = function () {
    var self = this;

    self.$form.on("click", ".assigned-index-delete-btn", function () {
        $(this).parent().remove();

        var inputs = $(".indices-assign-section").find("input")
        for (var i = 0; i < inputs.length; i++) {
            inputs[i].name = `indices[${i}].id`
        }
    })
}