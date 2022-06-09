package kr.co.data_status_evaluation.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class Pagination implements Pageable {
    private static final int DEFAULT_GUTTER = 5;

    private int pageNum;

    private int pageSize;

    private int totalPages;

    private long totalElements;

    private String query = "";

    private Sort sort;

    private String category = null;

    public Pagination(int pageNum, int pageSize, Sort sort) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public Pagination(Pageable pageable) {
        this.pageNum = pageable.getPageNumber();
        this.pageSize = pageable.getPageSize();
        this.sort = pageable.getSort();
    }

    public void setTotalPages(int total) {
        this.totalPages = total;
    }

    public void setTotalElements(long total) {
        this.totalElements = total;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public long getIndex(int index) {
        if (this.getTotalElements() < this.getPageSize()) {
            return index + 1;
        }
//        return Math.max(1, this.getTotalElements() - ((long) this.getCurrent() * this.getPageSize()) + (index + 1));
        return Math.max(1, this.getTotalElements() - ((this.getCurrent() - 1) * this.getPageSize()) - index);
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public long getTotalElements() {
        return this.totalElements;
    }

    public int getCurrent() {
        return this.getPageNumber() + 1;
    }

    public int getBegin() {
        return Math.max(1, this.getCurrent() - 2);
    }

    public int getEnd() {
        return Math.min(this.getCurrent() + 2, this.totalPages);
    }

    public String getQuery() {
        return this.query;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return this.category;
    }

    @Override
    public int getPageNumber() {
        return this.pageNum;
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    @Override
    public long getOffset() {
        return (long) this.pageNum * (long) this.pageSize;
    }

    @Override
    public Sort getSort() {
        return this.sort;
    }

    @Override
    public Pagination next() {
        return new Pagination(this.getPageNumber() + 1, this.getPageSize(), this.getSort());
    }

    @Override
    public Pagination previousOrFirst() {
        return this.hasPrevious() ? this.previous() : this.first();
    }

    @Override
    public Pagination first() {
        return new Pagination(0, this.getPageSize(), this.getSort());
    }

    public Pagination previous() {
        return this.getPageNumber() == 0 ? this : new Pagination(this.getPageNumber() - 1, this.getPageSize(), this.getSort());
    }

    @Override
    public Pagination withPage(int pageNumber) {
        return new Pagination(pageNumber, this.getPageSize(), this.getSort());
    }

    @Override
    public boolean hasPrevious() {
        return this.pageNum > 0;
    }

    @Override
    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]", this.getPageNumber(), this.getPageSize(), this.sort);
    }
}