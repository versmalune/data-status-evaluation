var FieldManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = this.options.contextPath;
    this.evlYear = this.options.evlYear;
    this.container = this.options.container;

    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");
    this.$selectOption = Handlebars.getTemplate("account/field");

    this.initEvent();
};

FieldManager.prototype.initEvent = function () {
    var self = this;

    $('#evlYear-index-rank').change(function () {
        $.ajax({
            url: self.options.contextPath + '/rest/evalField/year/' + $(this).val(),
            type: "GET",
            success: function (data) {
                var select = $('#field');
                select.html(self.$selectOption({
                    type: 'evaluationRank',
                    data: data
                }));
            },
            error: function (error) {
                alert(error);
            }
        })
    });

    self.container.on("click", ".file-download-btn", function() {
        if ($(this).hasClass("btn-secondary")) {
            alert("증빙자료가 없습니다.");
            return;
        }
        var tabType = $(this).parent().data("tab"); // category / index
        var id = $(this).parent().data("id"); // categoryId / indexId
        var name = $(this).parent().data("name"); // categoryNm / indexNm

        var url = (tabType == "category") ? self.contextPath + '/file/download/zip/categoryId/' + id + '?year=' + self.evlYear
                                          : self.contextPath + '/file/download/zip/indexId/' + id;

        var fileNm = self.evlYear + "년_";
        fileNm += (tabType == "category") ? "(기관유형)_" : "(지표)_";
        fileNm += name + "_증빙자료";
        fileNm += "_" + new Date().toISOString().slice(0, 10).replaceAll("-", "");

        $.ajax({
            url: url,
            type: "GET",
            processData: false,
            contentType: false,
            beforeSend: function (xhr) {
                xhr.setRequestHeader(self.header, self.token);
                startLoading();
            },
            complete: function () {
                stopLoading();
            },
            xhrFields:{
                responseType: 'blob'
            },
            success: function (result) {
                var blob = result;
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = downloadUrl;
                a.download = fileNm + ".zip";
                document.body.appendChild(a);
                a.click();
            },
            error: function (e) {
                window.alert("다운로드중 에러가 발생했습니다.")
            }
        });
    });
};

