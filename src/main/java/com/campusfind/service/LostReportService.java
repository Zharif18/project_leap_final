package com.campusfind.service;

import com.campusfind.dto.LostReportRequest;
import com.campusfind.dto.LostReportResponse;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.entity.Category;
import com.campusfind.entity.LostReport;
import com.campusfind.entity.LostStatus;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.exception.ApiException;
import com.campusfind.repository.CategoryRepository;
import com.campusfind.repository.LostReportRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LostReportService {

    private final LostReportRepository lostRepo;
    private final CategoryRepository categoryRepo;
    private final AuthService auth;

    public LostReportService(LostReportRepository lostRepo, CategoryRepository categoryRepo, AuthService auth) {
        this.lostRepo = lostRepo;
        this.categoryRepo = categoryRepo;
        this.auth = auth;
    }

    public LostReportResponse create(Long userId, LostReportRequest req) {
        User user = auth.requireUser(userId);
        Category category = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> ApiException.notFound("Category " + req.categoryId() + " does not exist"));

        LostReport report = new LostReport();
        report.setReportedBy(user);
        report.setCategory(category);
        report.setDescription(req.description().trim());
        report.setLocation(req.location().trim());
        report.setDateLost(req.dateLost());
        report.setStatus(LostStatus.OPEN);
        return ResponseMapper.toLost(lostRepo.save(report));
    }

    /** Students see only their own reports; staff and admin see everything. */
    public List<LostReportResponse> list(Long userId, Long categoryId, LostStatus status, LocalDate date) {
        User user = auth.requireUser(userId);
        return lostRepo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(l -> user.getRole() != Role.STUDENT || l.getReportedBy().getId().equals(user.getId()))
                .filter(l -> categoryId == null || l.getCategory().getId().equals(categoryId))
                .filter(l -> status == null || l.getStatus() == status)
                .filter(l -> date == null || l.getDateLost().equals(date))
                .map(ResponseMapper::toLost)
                .toList();
    }

    public LostReportResponse get(Long userId, Long id) {
        User user = auth.requireUser(userId);
        LostReport report = find(id);
        if (user.getRole() == Role.STUDENT && !report.getReportedBy().getId().equals(user.getId())) {
            throw ApiException.forbidden("You can only view your own lost reports");
        }
        return ResponseMapper.toLost(report);
    }

    public void delete(Long userId, Long id) {
        User user = auth.requireUser(userId);
        LostReport report = find(id);
        boolean owner = report.getReportedBy().getId().equals(user.getId());
        if (user.getRole() != Role.ADMIN && !owner) {
            throw ApiException.forbidden("Only the reporter or an admin can delete this lost report");
        }
        if (report.getStatus() != LostStatus.OPEN) {
            throw ApiException.badRequest("Only OPEN lost reports can be deleted (this one is " + report.getStatus() + ")");
        }
        lostRepo.delete(report);
    }

    private LostReport find(Long id) {
        return lostRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Lost report " + id + " not found"));
    }
}
