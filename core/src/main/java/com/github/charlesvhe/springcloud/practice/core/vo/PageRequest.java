package com.github.charlesvhe.springcloud.practice.core.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel
public class PageRequest<F> implements Serializable {
    @ApiModelProperty("查询偏移量 数据库分页offset从0开始")
    private Integer offset = 0;
    @ApiModelProperty("返回条目数量 默认值10")
    private Integer limit = 10;
    @ApiModelProperty("总条目 默认值-1 不传则从数据库查询 值正整数则不从数据库查询 APP端不关心总数传0")
    private Long total = -1L;

    private F filter;
    private String sort;

    public PageRequest buildNextPage() {
        return this.buildNextPage(this.total);
    }

    public PageRequest buildNextPage(Long total) {
        PageRequest next = new PageRequest();
        next.setOffset(this.offset + this.limit);
        next.setLimit(this.limit);
        next.setFilter(this.filter);
        next.setSort(this.sort);
        next.setTotal(total);
        return next;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public F getFilter() {
        return filter;
    }

    public void setFilter(F filter) {
        this.filter = filter;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
