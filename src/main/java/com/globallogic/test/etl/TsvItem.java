package com.globallogic.test.etl;

import java.time.LocalDate;

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
    public String toString() {
        return "TsvItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
