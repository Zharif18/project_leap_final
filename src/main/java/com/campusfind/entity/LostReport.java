package com.campusfind.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_reports")
public class LostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate dateLost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LostStatus status = LostStatus.OPEN;

    private LocalDateTime createdAt;

    // Many lost reports belong to one user (the reporter)
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    // Many lost reports belong to one category
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    // Set when staff/admin confirms a match with a found item
    @ManyToOne
    @JoinColumn(name = "matched_found_item_id")
    private FoundItem matchedFoundItem;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getDateLost() { return dateLost; }
    public void setDateLost(LocalDate dateLost) { this.dateLost = dateLost; }
    public LostStatus getStatus() { return status; }
    public void setStatus(LostStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public FoundItem getMatchedFoundItem() { return matchedFoundItem; }
    public void setMatchedFoundItem(FoundItem matchedFoundItem) { this.matchedFoundItem = matchedFoundItem; }
}
