package com.globallogic.test.etl.tsv;

import java.time.LocalDate;
import java.util.Objects;

public class TsvItem {
    private String id;
    private String name;
    private Integer quantity;
    private LocalDate dateCreated;

    public TsvItem() {
    }

    public TsvItem(String id, String name, Integer quantity, LocalDate dateCreated) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.dateCreated = dateCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TsvItem)) return false;
        TsvItem tsvItem = (TsvItem) o;
        return Objects.equals(id, tsvItem.id) &&
                Objects.equals(name, tsvItem.name) &&
                Objects.equals(quantity, tsvItem.quantity) &&
                Objects.equals(dateCreated, tsvItem.dateCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, dateCreated);
    }

    @Override
    public String toString() {
        return "TsvItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
