package com.campusfind.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash, never returned by the API

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // One user reports many lost items
    @OneToMany(mappedBy = "reportedBy")
    private List<LostReport> lostReports = new ArrayList<>();

    // One staff user logs many found items
    @OneToMany(mappedBy = "reportedBy")
    private List<FoundItem> foundItems = new ArrayList<>();

    public User() {
    }

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<LostReport> getLostReports() { return lostReports; }
    public void setLostReports(List<LostReport> lostReports) { this.lostReports = lostReports; }
    public List<FoundItem> getFoundItems() { return foundItems; }
    public void setFoundItems(List<FoundItem> foundItems) { this.foundItems = foundItems; }
}
