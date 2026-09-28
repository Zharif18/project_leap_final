package com.campusfind.config;

import com.campusfind.entity.Category;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.repository.CategoryRepository;
import com.campusfind.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds starter categories and demo accounts the first time the app runs. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(CategoryRepository categoryRepo, UserRepository userRepo, PasswordEncoder encoder) {
        this.categoryRepo = categoryRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (categoryRepo.count() == 0) {
            List<String> names = List.of("ID Card", "Water Bottle", "Charger", "Calculator", "Keys",
                    "Wallet", "Books", "Bag", "Electronics", "Clothing", "Other");
            for (String name : names) {
                categoryRepo.save(new Category(name));
            }
        }
        seedUser("Campus Admin", "admin@campus.com", "admin123", Role.ADMIN);
        seedUser("Security Desk Staff", "staff@campus.com", "staff123", Role.STAFF);
        seedUser("Demo Student", "student@campus.com", "student123", Role.STUDENT);
    }

    private void seedUser(String name, String email, String rawPassword, Role role) {
        if (!userRepo.existsByEmail(email)) {
            userRepo.save(new User(name, email, encoder.encode(rawPassword), role));
        }
    }
}
