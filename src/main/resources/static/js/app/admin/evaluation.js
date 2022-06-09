var EvaluationIndexManager = function (fields, categories, index, options) {
    this.options = _.cloneDeep(options);
    this.fields = fields;
    this.categories = categories;
    this.index = index;

    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");
    this.contextPath = options.contextPath;

    this.$fieldSelect = $("#field-select");
    this.$yearSelect = $("#year-select");
    this.$indexNumber = $("#no");
    this.$quantitativeScore = $(".score-quan");
    this.$qualitativeScore = $(".score-qual");
    this.$addLevelBtn = $(".add-level-btn");
    this.scoreTemplate = Handlebars.getTemplate("index/score");
    this.addScoreSection = $(".add-score-section");
    this.initialRowspanSize = $("#score-header").attr("rowspan");
    this.$naLevelYn = $("#inputYn5");
    this.$naLevelYnLabel = $("label[for='inputYn5']");

    this.initEvent();
}

EvaluationIndexManager.prototype.initEvent = function () {
    this.changeFieldOptions();
    this.initScoreSection();
    this.changeIndexNumberBySelectedField()
    this.validateForm();
    this.addLevelEvent();
    this.removeLevelEvent();
    this.initLevel();
    this.initInputName();
}

EvaluationIndexManager.prototype.addLevelEvent = function () {
    var self = this;
    this.$addLevelBtn.on("click", function () {
        if ($(".score-quan").length > 0) {
            $("tbody").append(self.scoreTemplate({
                categories: self.categories,
            }))
        }
        self.initScoreSection();
        self.initInputName();
        self.removeLevelEvent();
        self.initLevel();
        self.validateForm();
        self.maxScoreValidate();
    })
}

EvaluationIndexManager.prototype.removeLevelEvent = function () {
    var self = this;
    $(".remove-level-btn").on("click", function () {
        $(this).parent().parent().remove();
        self.initScoreSection();
        self.initInputName();
        self.initLevel()
        self.validateForm();
    })
}

EvaluationIndexManager.prototype.initInputName = function () {
    var self = this;
    $(".score-quan").each(function (i) {
        if (i > 0) {
            var index = i;
            $(this).find("input").each(function () {
                var scoreIndex = self.categories.length * (index - 1) + $(this).data("idx");
                var inputName = `scores[${scoreIndex}].` + $(this).attr("name").split(".")[1];
                $(this).attr("name", inputName);
            })
        }
    })
}

EvaluationIndexManager.prototype.initLevel = function () {
    $(".score-quan").each(function (i) {
        if (i > 0) {
            var $self = $(this).find(".level-input");
            $self.each(function (index) {
                if (index > 0) {
                    $(this).attr("type", "hidden");
                }
            })
            $self.first().on("input", function () {
                $self.val($(this).val())
            })
        }
    })

}

EvaluationIndexManager.prototype.changeFieldOptions = function () {
    var self = this;

    this.initFieldOptions(fields, this.$yearSelect.val());
    this.$yearSelect.on("change", function () {
        self.initFieldOptions(fields, $(this).val());
    });
}

EvaluationIndexManager.prototype.changeIndexNumberBySelectedField = function () {
    var self = this;

    var preNo = this.$fieldSelect.val();
    this.$indexNumber.val(preNo + "-");
    if (this.index) {
        this.$indexNumber.val(this.index.no);
    }
    this.checkFieldType(preNo);
    this.maxScoreValidate();

    this.$fieldSelect.on("change", function () {
        preNo = $(this).val()
        self.$indexNumber.val(preNo + "-");

        self.checkFieldType(preNo);
        self.maxScoreValidate();
    })
}

EvaluationIndexManager.prototype.checkFieldType = function(no) {
    var self = this;
    var defaultType = self.index ? self.index.type : "QUALITATIVE";

    var field = self.fields.filter(function (field) {
        return field.year === self.$yearSelect.val() && field.no === parseInt(no);
    });
    if(field[0].name.includes("기타")) {
        $("label[for='plus-or-minus']").removeClass("btn-outline-secondary disabled");
        $("label[for='QUALITATIVE']").addClass("btn-outline-secondary disabled");
        $("label[for='QUANTITATION']").addClass("btn-outline-secondary disabled");
        $("#"+defaultType).attr("checked", false).change();
        $("#plus-or-minus").attr("checked", true).change();
    } else {
        $("label[for='plus-or-minus']").addClass("btn-outline-secondary disabled");
        $("label[for='QUALITATIVE']").removeClass("btn-outline-secondary disabled");
        $("label[for='QUANTITATION']").removeClass("btn-outline-secondary disabled");
        $("#plus-or-minus").attr("checked", false);
        $("#"+defaultType).attr("checked", true).change();
    }
}

EvaluationIndexManager.prototype.initScoreSection = function () {
    var self = this;

    if ($("input:radio[name=type]:checked").val() === "QUANTITATION") {
        $("#score-header").prop("rowspan", $(".score-quan").length + 1)
        self.$qualitativeScore.find("input:text").prop("value", "")
        self.$naLevelYn.attr("disabled", false);
        self.$naLevelYnLabel.removeClass("text-gray");
    } else {
        self.$quantitativeScore.find("input:text").prop("value", "")
        self.$naLevelYn.attr("disabled", true);
        self.$naLevelYnLabel.addClass("text-gray");
    }

    $("input:radio[name=type]").on("change", function () {
        if (this.value === "QUALITATIVE" || this.value === "") {
            $(".score-quan").addClass("d-none");
            $(".score-quan").find("input").prop("disabled", true)

            $(".score-qual").removeClass("d-none");
            $(".score-qual").find("input").prop("disabled", false)

            $("#score-header").prop("rowspan", self.initialRowspanSize)
            self.$naLevelYn.attr("disabled", true);
            self.$naLevelYnLabel.addClass("text-gray");
        } else {
            $(".score-qual").addClass("d-none");
            $(".score-qual").find("input").prop("disabled", true)

            $(".score-quan").removeClass("d-none");
            $(".score-quan").find("input").prop("disabled", false)

            $("#score-header").prop("rowspan", $(".score-quan").length + 1)
            self.$naLevelYn.attr("disabled", false);
            self.$naLevelYnLabel.removeClass("text-gray");
        }
    })
}

EvaluationIndexManager.prototype.validateForm = function () {
    if (this.validator) {
        this.validator.destroy();
    }
    this.validator = $("#evalIndexForm").validate({
        rules: this.insertScoreVaildateOption({
            no: {
                required: true,
                minlength: 3
            },
            name: {
                required: true,
            },
            question: {
                required: true,
            },
            dateRange: {
                required: true,
            },
        }, true),
        messages: this.insertScoreVaildateOption({
            no: {
                required: "* 번호는 필수 입력입니다",
                minlength: "* 지표번호는 필수 입력입니다"
            },
            name: {
                required: "* 지표명는 필수 입력입니다",
            },
            question: {
                required: "* 질문항목은 필수 입력입니다",
            },
            dateRange: {
                required: "* 실적대상기간은 필수 입력입니다",
            },
        }, "* 해당 항목은 필수 입력입니다"),
        errorPlacement: function (error, element) {
            error.css("color", "red")

            if (element.is("input[name='dateRange']")) {
                error.addClass("align-self-center col-12 text-center")
                element.parent().append(error)
            } else if (element.is(".input-quan")) {
                element.parent().parent().append(error)
            } else {
                element.parent().append(error)
            }
        }
    });
}

EvaluationIndexManager.prototype.filterFields = function (fields, year) {
    var selectedYear = year;
    if (!year) {
        selectedYear = new Date().getFullYear().toString();
    }

    return fields.filter(function (field) {
        return field.year === selectedYear;
    });
}

EvaluationIndexManager.prototype.initFieldOptions = function (fields, year) {
    var comboBox = $("#field-select");
    comboBox.empty();

    var filteredFields = this.filterFields(fields, year);
    for (var field of filteredFields) {
        var option = $('<option></option>').attr("value", field.no).text(field.name);
        if (this.index && this.index.no.charAt(0) === field.no.toString()) {
            option.attr("selected", true);
        }
        comboBox.append(option);
    }
}

EvaluationIndexManager.prototype.maxScoreValidate = function () {
    var self = this;

    var fields = this.filterFields(this.fields, this.$yearSelect.val());
    for (var field of fields) {
        if (field.no === parseInt(this.$fieldSelect.val())) {
            var fieldScore = field.score;
            var url = self.contextPath + "/admin/evalIndex/"+ field.id + "/indices";
            if (self.index) {
                url += "?index="+self.index.id;
            }
            $.ajax({
                url: url,
                type: "GET",
                processData: false,
                contentType: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(self.header, self.token);
                },
                success: function (data) {
                    $(".score-input").each(function (idx) {
                        var categoryId = $(this).siblings("input").val();
                        var indexTotalScore = data[parseFloat(categoryId)] || 0;
                        var maxScore = fieldScore - indexTotalScore;
                        if (maxScore < 0) {
                            maxScore = 0;
                        }
                        maxScore = Math.round(maxScore * 100) / 100
                        $(this).attr("max", maxScore);
                        $(this).attr("placeholder", `최대 (${maxScore})점까지 입력가능`);
                    })
                },
                error: function (err) {
                    alert(err);
                }
            });
        }
    }
}

EvaluationIndexManager.prototype.insertScoreVaildateOption = function (option, value) {
    var self = this;

    for (var i = 0; i < self.categories.length * ($(".score-quan").length - 1); i++) {
        var scoreKey = "scores[" + i + "].score";
        var level = "scores[" + i + "].level";

        option[scoreKey] = {required: value};
        option[level] = {required: value}
    }
    return option
}