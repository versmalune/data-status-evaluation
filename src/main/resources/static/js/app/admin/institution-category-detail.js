var InstitutionCategoryDetailManager = function (category) {
    this.category = category;
    this.$addLevelBtn = $(".add-level-btn");
    this.categoryTemplate = Handlebars.getTemplate("category/category");
    this.initEvent();
}

InstitutionCategoryDetailManager.prototype.initEvent = function () {
    this.addLevelEvent();
    this.removeLevelEvent();
    this.initInputName();
}

InstitutionCategoryDetailManager.prototype.addLevelEvent  = function () {
    var self = this;
    this.$addLevelBtn.on("click", function () {
        $("tbody").append(self.categoryTemplate({
            categoryId: self.category.id,
        }));
        self.removeLevelEvent();
        self.initInputName();
    })
}

InstitutionCategoryDetailManager.prototype.removeLevelEvent = function () {
    var self = this;
    $(".remove-level-btn").on("click", function () {
        $(this).parent().parent().remove();
        self.initInputName();
    })
}

InstitutionCategoryDetailManager.prototype.initInputName = function () {
    var self = this;
    $('.category-detail').each(function (i) {
        var index = i;
        $(this).find("input").each(function () {
            var inputName = `detailCategories[${index}].` + $(this).attr("name").split(".")[1];
            $(this).attr("name", inputName);
            if ($(this).attr("name").split(".")[1] === 'institutionCategoryId') {
                $(this).val(self.category.id);
            }
        })
    })
}