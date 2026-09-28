package com.campusfind.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "category")
    private List<LostReport> lostReports = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<FoundItem> foundItems = new ArrayList<>();

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<LostReport> getLostReports() { return lostReports; }
    public void setLostReports(List<LostReport> lostReports) { this.lostReports = lostReports; }
    public List<FoundItem> getFoundItems() { return foundItems; }
    public void setFoundItems(List<FoundItem> foundItems) { this.foundItems = foundItems; }
}
