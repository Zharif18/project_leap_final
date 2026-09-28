package com.campusfind.service;

import com.campusfind.dto.FoundItemRequest;
import com.campusfind.dto.FoundItemResponse;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.entity.Category;
import com.campusfind.entity.FoundItem;
import com.campusfind.entity.FoundStatus;
import com.campusfind.entity.LostReport;
import com.campusfind.entity.LostStatus;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.exception.ApiException;
import com.campusfind.repository.CategoryRepository;
import com.campusfind.repository.FoundItemRepository;
import com.campusfind.repository.LostReportRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FoundItemService {

    private final FoundItemRepository foundRepo;
    private final LostReportRepository lostRepo;
    private final CategoryRepository categoryRepo;
    private final AuthService auth;

    public FoundItemService(FoundItemRepository foundRepo,
                            LostReportRepository lostRepo,
                            CategoryRepository categoryRepo,
                            AuthService auth) {
        this.foundRepo = foundRepo;
        this.lostRepo = lostRepo;
        this.categoryRepo = categoryRepo;
        this.auth = auth;
    }

    public FoundItemResponse create(Long userId, FoundItemRequest req) {
        User user = auth.requireUser(userId);
        auth.requireRole(user, "Only staff or admin can log found items", Role.STAFF, Role.ADMIN);
        Category category = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> ApiException.notFound("Category " + req.categoryId() + " does not exist"));

        FoundItem item = new FoundItem();
        item.setReportedBy(user);
        item.setCategory(category);
        item.setDescription(req.description().trim());
        item.setLocation(req.location().trim());
        item.setDateFound(req.dateFound());
        item.setStatus(FoundStatus.AVAILABLE);
        return ResponseMapper.toFound(foundRepo.save(item));
    }

    public List<FoundItemResponse> list(Long userId, Long categoryId, FoundStatus status, LocalDate date) {
        auth.requireUser(userId);
        return foundRepo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(f -> categoryId == null || f.getCategory().getId().equals(categoryId))
                .filter(f -> status == null || f.getStatus() == status)
                .filter(f -> date == null || f.getDateFound().equals(date))
                .map(ResponseMapper::toFound)
                .toList();
    }

    public FoundItemResponse get(Long userId, Long id) {
        auth.requireUser(userId);
        return ResponseMapper.toFound(find(id));
    }

    /**
     * BUSINESS RULES (checked BEFORE saving):
     *  1. Only an admin, or the staff member who logged the item, can change its status.
     *  2. An item cannot become RETURNED unless it is CLAIMED first.
     */
    @Transactional
    public FoundItemResponse updateStatus(Long userId, Long itemId, FoundStatus newStatus) {
        User actor = auth.requireUser(userId);
        FoundItem item = find(itemId);

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isReportingStaff = actor.getRole() == Role.STAFF
                && item.getReportedBy().getId().equals(actor.getId());
        if (!isAdmin && !isReportingStaff) {
            throw ApiException.forbidden("Only an admin or the staff member who reported this item can change its status");
        }

        FoundStatus current = item.getStatus();
        if (current == newStatus) {
            throw ApiException.badRequest("Item is already " + current);
        }
        if (current == FoundStatus.RETURNED) {
            throw ApiException.badRequest("A RETURNED item can no longer be changed");
        }
        if (newStatus == FoundStatus.RETURNED && current != FoundStatus.CLAIMED) {
            throw ApiException.badRequest("A found item cannot be marked RETURNED unless it is first marked CLAIMED (current status: " + current + ")");
        }

        item.setStatus(newStatus);
        FoundItem saved = foundRepo.save(item);

        // When an item goes back to its owner, close the lost report that was matched to it.
        if (newStatus == FoundStatus.RETURNED) {
            for (LostReport lost : lostRepo.findByMatchedFoundItemId(itemId)) {
                lost.setStatus(LostStatus.RETURNED);
                lostRepo.save(lost);
            }
        }
        return ResponseMapper.toFound(saved);
    }

    public void delete(Long userId, Long id) {
        User user = auth.requireUser(userId);
        FoundItem item = find(id);
        boolean owner = item.getReportedBy().getId().equals(user.getId());
        if (user.getRole() != Role.ADMIN && !(user.getRole() == Role.STAFF && owner)) {
            throw ApiException.forbidden("Only an admin or the staff member who reported this item can delete it");
        }
        if (item.getStatus() != FoundStatus.AVAILABLE) {
            throw ApiException.badRequest("Only AVAILABLE items can be deleted (this one is " + item.getStatus() + ")");
        }
        if (!lostRepo.findByMatchedFoundItemId(id).isEmpty()) {
            throw ApiException.badRequest("This item is matched to a lost report and cannot be deleted");
        }
        foundRepo.delete(item);
    }

    private FoundItem find(Long id) {
        return foundRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Found item " + id + " not found"));
    }
}
