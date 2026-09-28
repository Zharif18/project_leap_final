package com.campusfind.service;

import com.campusfind.dto.CategoryRequest;
import com.campusfind.dto.CategoryResponse;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.entity.Category;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.exception.ApiException;
import com.campusfind.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final AuthService auth;

    public CategoryService(CategoryRepository categoryRepo, AuthService auth) {
        this.categoryRepo = categoryRepo;
        this.auth = auth;
    }

    public List<CategoryResponse> list() {
        return categoryRepo.findAll().stream().map(ResponseMapper::toCategory).toList();
    }

    public CategoryResponse create(Long userId, CategoryRequest req) {
        User user = auth.requireUser(userId);
        auth.requireRole(user, "Only an admin can create categories", Role.ADMIN);
        String name = req.name().trim();
        if (categoryRepo.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("Category '" + name + "' already exists");
        }
        return ResponseMapper.toCategory(categoryRepo.save(new Category(name)));
    }
}
