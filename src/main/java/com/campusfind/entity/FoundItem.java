package com.campusfind.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "found_items")
public class FoundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate dateFound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FoundStatus status = FoundStatus.AVAILABLE;

    private LocalDateTime createdAt;

    // The staff member who logged the item
    @ManyToOne(optional = false)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

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
    public LocalDate getDateFound() { return dateFound; }
    public void setDateFound(LocalDate dateFound) { this.dateFound = dateFound; }
    public FoundStatus getStatus() { return status; }
    public void setStatus(FoundStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
