var Pagination = function(container) {
    this.data = {};
    this.container = container;
    this.paginationTemplate = Handlebars.getTemplate("search/pagination");
}

Pagination.DEFAULT_GUTTER = 5;
Pagination.PAGING_ITEMS = 7;

Pagination.prototype.setByObject = function(obj) {
    this.data.enabled = obj.totalElements > 0;

    this.data.page = obj.page;
    this.data.current = obj.current;
    this.data.totalElements = obj.totalElements;
    this.data.perPage = obj.perPage;
    this.data.totalPages = this.getTotalPages();

    this.data.begin = Math.max(1, this.data.current - Pagination.DEFAULT_GUTTER);
    this.data.end = Math.min(this.data.begin + Pagination.PAGING_ITEMS, this.data.totalPages);

    this.data.url = obj.url;
    this.data.query = obj.query;

    this.setOptions();
    this.render();
}

Pagination.prototype.getTotalPages = function() {
    return Math.ceil(this.data.totalElements / this.data.perPage);
}

Pagination.prototype.setOptions = function () {
    this.data.hasFirstPage = this.data.begin > 1;
    this.data.hasPrevPage = this.data.begin > 2;
    this.data.prevPage = this.data.begin - 1;
    this.data.hasNextPage = this.data.end < this.data.totalPages - 1;
    this.data.nextPage = this.data.end - 1;
    this.data.hasLastPage = this.data.end < this.data.totalPages;
}

Pagination.prototype.render = function() {
    var self = this;

    var paginationHtml = self.paginationTemplate({
        pagination: self.data
    });
    $(self.container).html(paginationHtml);

    $("li.page-item").removeClass('active');
    $("li.page-item[data-page='" + self.data.current + "']").addClass('active');
}