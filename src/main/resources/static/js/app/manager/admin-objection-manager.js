var adminObjectionManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = options.contextPath;

    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");

    this.initEvent();

}

adminObjectionManager.prototype.initEvent = function () {
    var self = this;

    $(".objection-save-btn").on("click", function (e) {
        if (confirm("저장하시겠습니까?")) {
            var $container = $(this).parent().parent();
            var resultId = $container.find("#resultId").val();
            var objectionReview = $container.find("#objectionReview").val();
            $(":disabled").attr("disabled", false);

            $.ajax({
                url: self.contextPath + "/admin/objections/update",
                data: {
                    id: resultId,
                    objectionReview: objectionReview,
                },
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(self.header, self.token);
                },
                type: "POST",
                async: false,
                success: function (res) {
                    alert("저장이 완료되었습니다.");
                    location.reload();
                },
                error: function (err) {
                    window.alert("저장 중 에러가 발생했습니다.");
                    console.log(err);
                }
            });
        }
    });

}