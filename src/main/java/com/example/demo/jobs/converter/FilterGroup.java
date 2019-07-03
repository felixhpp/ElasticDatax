package com.example.demo.jobs.converter;

/**
 * 过滤条件组合
 * @author felix
 */
public final class FilterGroup {
    // must 组合
    private FilterBean[] must;
    // should 组合
    private FilterBean[] should;
    // not 组合
    private FilterBean[] not;

    public FilterBean[] getMust() {
        return must;
    }

    public void setMust(FilterBean[] must) {
        this.must = must;
    }

    public FilterBean[] getShould() {
        return should;
    }

    public void setShould(FilterBean[] should) {
        this.should = should;
    }

    public FilterBean[] getNot() {
        return not;
    }

    public void setNot(FilterBean[] not) {
        this.not = not;
    }
}
