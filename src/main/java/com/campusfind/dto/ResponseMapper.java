package com.campusfind.dto;

import com.campusfind.entity.Category;
import com.campusfind.entity.FoundItem;
import com.campusfind.entity.LostReport;
import com.campusfind.entity.User;

/** Converts entities to API responses (so passwords and lazy collections never leak out). */
public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static UserResponse toUser(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }

    public static CategoryResponse toCategory(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }

    public static LostReportResponse toLost(LostReport l) {
        return new LostReportResponse(
                l.getId(),
                l.getDescription(),
                l.getLocation(),
                l.getDateLost(),
                l.getStatus(),
                toCategory(l.getCategory()),
                toUser(l.getReportedBy()),
                l.getMatchedFoundItem() == null ? null : l.getMatchedFoundItem().getId());
    }

    public static FoundItemResponse toFound(FoundItem f) {
        return new FoundItemResponse(
                f.getId(),
                f.getDescription(),
                f.getLocation(),
                f.getDateFound(),
                f.getStatus(),
                toCategory(f.getCategory()),
                toUser(f.getReportedBy()));
    }
}
