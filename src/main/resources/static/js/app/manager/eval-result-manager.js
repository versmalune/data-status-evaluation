var EvalResultManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = options.contextPath;
    this.insttId = options.insttId;
    this.active = options.active;
    this.evlYear = options.evlYear;
    this.isActiveStart = options.isActiveStart;
    this.isActiveObjection = options.isActiveObjection;
    this.isScheduleFinished = options.isScheduleFinished;
    this.fldListSize = options.fldListSize;

    this.fldList = [];
    for (i=0; i<=this.fldListSize; i++) {
        this.fldList.push(i+"");
    }

    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");

    this.initEvent();
};

EvalResultManager.prototype.initEvent = function () {
    var self = this;

    self.changeBtn();

    $('.nav-link').on("click", function () {
        self.changeBtn();
        self.changeUploadFormActive($(this));
    });

    $('#evlYear').change(function () {
        startLoading();
        $('#yearForm').attr('action', self.contextPath + "/instt/eval/instt/" + self.insttId + "?evlYear=" + $(this).find(":selected").val()).submit();
    });
    $('#evlYear').attr("disabled", false);

    $('.upload-btn').on("click", function () {
        if (confirm("저장하시겠습니까?")) {
            self.uploadResult();
        }
    });

    $('.file-remove-btn').on("click", function () {
        self.deleteFile(this);
    });

    $('.prev-btn').on("click", this.showPrev);

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

    $('.form-download-btn').on("click", function () {
        var indexId = $(this).data("index-id");
        var spinnerSeq = $(this).data("spinner-seq");
        $.ajax({
            url: self.contextPath + "/instt/eval/form/"+ indexId,
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
                console.log(result);
                for (let i = 0; i < result.length; i++) {
                    var blob = result[i];
                    var downloadUrl = URL.createObjectURL(blob);
                    var a = document.createElement("a");
                    a.href = downloadUrl;
                    a.download = zipName + ".zip";
                    document.body.appendChild(a);
                    a.click();
                }
            },
            error: function (e) {
                window.alert("다운로드중 에러가 발생했습니다.");
            }
        });
    })

    if (self.isScheduleFinished == 'true') {
        $('#insttTabContent').find("input, button, select, textarea").not(".zip-download-btn, .btn-close, .btn-secondary, .index-desc-btn").attr("disabled", true);
    }

}

EvalResultManager.prototype.showPrev = function () {
    var idx = document.querySelector(".nav-link.active").getAttribute("aria-controls");
    var navLinks = document.querySelectorAll(".nav-link");
    if (idx > 0) {
        var tab = new bootstrap.Tab(navLinks[parseInt(idx) - 1]);
        tab.show();
    }
}

EvalResultManager.prototype.changeBtn = function () {
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

EvalResultManager.prototype.changeUploadFormActive = function (navLink) {
    $('.upload-form-active').val(navLink.attr('aria-controls'));
}

EvalResultManager.prototype.checkDate = function (flag) {
    return flag
}

EvalResultManager.prototype.uploadResult = function () {
    var self = this;

    // submit 이전에 disabled 해제
    $(":disabled").attr("disabled", false);

    var formList= $('.upload-form')
    var errorFlag = false;

    for (var i=0; i<formList.length; i++) {
        var indexId = formList.get(i).id.split('-')[2];
        var inputFieldId = "formFile-" + indexId;

        if (($('#' + inputFieldId)[0] != undefined && $('#' + inputFieldId)[0].files.length > 0) || $('#textarea-' + indexId).val()) {
            var form = $('#' + formList.get(i).id)[0];
            var data = new FormData(form);

            $.ajax({
                url: self.contextPath + "/instt/eval/instt/" + self.insttId + "/index/" + indexId + '/upload',
                type: "POST",
                async: false,
                enctype: 'multipart/form-data',
                contentType: false,
                processData: false,
                data : data,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(self.header, self.token);
                },
                success: function (resultObject) {
                },
                error: function (e) {
                    errorFlag = true;
                }
            });
        }

        var active = $('.nav-link.active').attr('aria-controls')
        var index = self.fldList.indexOf(active);
        
        if (i === formList.length -1) {
            console.log('저장이 완료되었습니다.');
            console.log("self.fldList : ");
            console.log(self.fldList);
            console.log("index : ");
            console.log(index);
            if (self.fldList.length - 1 ===  index) {
                --index;
            }
            alert("저장이 완료되었습니다.")
            location.href = self.contextPath + '/instt/eval/instt/' + self.insttId + "?evlYear=" + self.evlYear + '&active=' +self.fldList[index+1];
        }

        if(errorFlag) {
            console.log('저장 중 에러가 발생했습니다.');
            alert("저장 중 에러가 발생했습니다.")
            location.href = self.contextPath + '/instt/eval/instt/' + self.insttId + "?evlYear=" + self.evlYear + '&active=' +self.fldList[index];
            break;
        }
    }
};


EvalResultManager.prototype.deleteFile = function (removeBtnSelector) {
    var self = this;

    if(confirm("파일을 삭제하시겠습니까?")) {
        var id = $(removeBtnSelector).data("id");
        var resultId = $(removeBtnSelector).data("result");
        var $ul = $(removeBtnSelector).parent().parent();

        $.ajax({
            url: self.contextPath + "/instt/eval/instt/" + self.insttId + "/result/" + resultId + "/file/" + id + "/fileDelete",
            beforeSend: function (xhr) {
                xhr.setRequestHeader(self.header, self.token);
            },
            type: "POST",
            success: function () {
                $('#preview-file-' + id).remove();
                if ($($ul).find("li").length == 0) $($ul).parent().parent().find('.zip-download-btn').remove();
            },
            error: function (e) {
            }
        });
    }
};
