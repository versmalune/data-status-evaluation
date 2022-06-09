var AdminEvalResultManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = options.contextPath;
    this.insttId = options.insttId;
    this.insttNm = options.insttNm;
    this.active = options.active;
    this.evlYear = options.evlYear;
    this.isScheduleFinished = options.isScheduleFinished;

    this.fldList = ["0", "1", "2", "3", "4", "5"];
    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");

    this.initEvent();
};

AdminEvalResultManager.prototype.initEvent = function () {
    var self = this;

    var $form = $('#instt-process-form');
    $form.on("submit", function (e) {
        e.preventDefault();

       if (confirm("* " + evlYear + "년 -" + self.insttNm + "\n모든 결과의 평가진행상태가 일괄 변경됩니다.\n저장하시겠습니까?")) {
           $form[0].submit();
       }
    });

    self.changeBtn();

    $('.nav-link').on("click", function () {
        self.changeBtn();
    });

    $('.prev-btn').on("click", this.showPrev);

    $('.upload-btn').on("click", function (e) {
        if (confirm("저장하시겠습니까?")) {
            self.uploadResult(e);
        }
    });

    $('.zip-download-btn').on("click", function () {
        var spinnerSeq = $(this).data("spinner-seq");
        var insttId = self.insttId;
        var indexId = $(this).data("index-id");
        var zipName = $(this).data("zip-name");
        if (zipName != null && zipName != undefined) {
            zipName.trim().replace(/[\{\}\/?,;:|*~`!^\+<>@\#$%&\\\=\'\"]/gi, ''); // 파일명 Validation
        }

        $.ajax({
            url: self.contextPath + "/file/download/zip/insttId/"+ insttId + "/indexId/"+ indexId,
            type: "GET",
            processData: false,
            contentType: false,
            beforeSend: function (xhr) {
                xhr.setRequestHeader(self.header, self.token);
                startLoading(spinnerSeq);
            },
            complete: function () {
                stopLoading(spinnerSeq);
            },
            xhrFields:{
                responseType: 'blob'
            },
            success: function (result) {
                var blob = result;
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = downloadUrl;
                a.download = zipName + ".zip";
                document.body.appendChild(a);
                a.click();
            },
            error: function (e) {
                window.alert("다운로드중 에러가 발생했습니다.");
            }
        });
    });

    if (self.isScheduleFinished == 'true') {
        $('#insttTabContent').find("input, button, select, textarea").not(".zip-download-btn, .btn-close, .btn-secondary, .index-desc-btn").attr("disabled", true);
        $('#insttFooterContent').find("input, button, select, textarea").not(".zip-download-btn, .btn-close, .btn-secondary").attr("disabled", true);
    }
}

AdminEvalResultManager.prototype.showPrev = function () {
    var idx = document.querySelector(".nav-link.active").getAttribute("aria-controls");
    var navLinks = document.querySelectorAll(".nav-link");
    if (idx > 0) {
        var tab = new bootstrap.Tab(navLinks[parseInt(idx) - 1]);
        tab.show();
    }
}

AdminEvalResultManager.prototype.showNext = function () {
    var currentIdx = document.querySelector(".nav-link.active").getAttribute("aria-controls");
    var idx = parseInt(currentIdx);
    var navLinks = document.querySelectorAll(".nav-link");
    if (idx < navLinks.length - 1) {
        idx = idx + 1;
        var tab = new bootstrap.Tab(navLinks[idx]);
        tab.show();
    }
    return idx;
}

AdminEvalResultManager.prototype.changeBtn = function () {
    if ($("#insttTab  li:first-child button").hasClass("active")) {
        $('.prev-btn').addClass('d-none');
        $('.upload-btn').addClass('d-none');
    } else {
        $('.prev-btn').removeClass('d-none');
        $('.upload-btn').removeClass('d-none');
    }

    if ($("#insttTab  li:last-child button").hasClass("active")) {
        $('.upload-btn').html('저장');
    } else {
        $('.upload-btn').html('저장 후 다음');
    }
}

AdminEvalResultManager.prototype.checkDate = function (flag) {
    return flag
}

AdminEvalResultManager.prototype.uploadResult = function (e) {
    e.preventDefault();

    var self = this;

    // submit 이전에 disabled 해제
    $(":disabled").attr("disabled", false);

    var forms = $("form.eval-form");
    var data = [];
    for (var form of forms) {
        var datum = new FormData(form);
        data.push(datum);
    }

    $.ajax({
        url: self.contextPath + "/admin/eval/instt/" + self.insttId,
        type: "POST",
        data: forms.serializeArray(),
        beforeSend: function (xhr) {
            xhr.setRequestHeader(self.header, self.token);
            startLoading();
        },
        complete: function () {
            stopLoading();
        },
        success: function (data) {
            var nextIdx = self.showNext();
            alert("저장이 완료되었습니다.")
            location.href = this.url + `?year=${self.evlYear}&active=${nextIdx}`;
        },
        error: function (err) {
            alert(err);
        }
    });
};
