var FieldIndexManager = function (options) {
    this.options = _.cloneDeep(options);

    this.selectOption = Handlebars.getTemplate("statistics/field");

    this.indexObject = {};

    this.initEvent();
};

FieldIndexManager.prototype.initEvent = function () {
    var self = this;

    $('#evlYear-index-rank').change(function () {
        $.ajax({
            url: self.options.contextPath + '/rest/statistics/evaluationRank/year/' + $(this).val(),
            type: "GET",
            success: function (data) {
                self.indexObject = data.index;

                var select = $('#field');
                var indexSelect = $('#index');
                select.html(self.selectOption({
                    type: 'evaluationRank',
                    data: data.field
                }));

                indexSelect.html(self.selectOption({
                    type: 'evaluationRank',
                    data: data.index[data.field[0].name]
                }));
            },
            error: function (error) {
                alert(error);
            }
        })
    });

    $('#field').change(function () {
        var indexSelect = $('#index');
        var selectedField = $('#field option:selected').text();

        if(Object.keys(self.indexObject).length === 0) {
            indexSelect.html(self.selectOption({
                type: 'evaluationRank',
                data: self.options.index[selectedField]
            }));
        } else {
            indexSelect.html(self.selectOption({
                type: 'evaluationRank',
                data: self.indexObject[selectedField]
            }));
        }
    })
};

