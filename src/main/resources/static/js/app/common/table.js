var Table = function (options) {
    this.options = _.cloneDeep(options);

    this.initEvent();
}

Table.prototype.forwardLink = function (url) {
    location.href = url;
}

Table.prototype.initEvent = function () {
    var self = this;

    $(".clickable-row").on("click", function(e) {
        var $td;
        for ($td = e.target; $td.nodeName != 'TD'; $td = $td.parentElement); // td 태그 찾을때까지 parent로 올라가기
        
        const cbs = $td.querySelector("input[type='radio']");
        const cbx = $td.querySelector("input[type='checkbox']");
        const btn = $td.querySelector("button");

        if ((e.target.type && e.target.type == 'radio')
            || cbs || e.target.parentNode.classList.contains('abc-radio')) return;

        if((e.target.type && e.target.type == 'checkbox')
            || cbx || e.target.parentNode.classList.contains('custom-switch')) return;

        if((e.target.type && e.target.type == 'button')
            || btn) return;

        self.forwardLink($(this).data("url"));
    })
}
