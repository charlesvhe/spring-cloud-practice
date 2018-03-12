package com.github.charlesvhe.springcloud.practice.core.vo;

import java.io.Serializable;
import java.util.List;

public class PageData<T, F> implements Serializable {
    private List<T> entities;
    private PageRequest<F> next;

    public PageData() {
    }

    public PageData(List<T> entities, PageRequest<F> next) {
        this.entities = entities;
        this.next = next;
    }

    public List<T> getEntities() {
        return entities;
    }

    public void setEntities(List<T> entities) {
        this.entities = entities;
    }

    public PageRequest<F> getNext() {
        return next;
    }

    public void setNext(PageRequest<F> next) {
        this.next = next;
    }
}
