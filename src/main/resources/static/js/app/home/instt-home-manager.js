var InsttHomeManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = options.contextPath;
    this.insttId = options.insttId;
    this.currentEvlYear = options.currentEvlYear;

    this.initEvent();
}

InsttHomeManager.prototype.initEvent = function () {
    var self = this;

    $('#evlYear').on("change", function () {
        $('#yearForm').attr('action', location.pathname + "?evlYear=" + $(this).find(":selected").val()).submit();
    });
    $('#evlYear').attr("disabled", false);

    $('.move-btn').on("click", function (e) {
        e.preventDefault();
        location.href = self.contextPath + '/instt/eval/instt/' + self.insttId + "?evlYear=" + self.currentEvlYear + '&active=' + $(this).data("active");
    });
}