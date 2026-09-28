package com.campusfind.service;

import com.campusfind.dto.ConfirmMatchRequest;
import com.campusfind.dto.LostReportResponse;
import com.campusfind.dto.MatchResponse;
import com.campusfind.dto.ResponseMapper;
import com.campusfind.entity.FoundItem;
import com.campusfind.entity.FoundStatus;
import com.campusfind.entity.LostReport;
import com.campusfind.entity.LostStatus;
import com.campusfind.entity.Role;
import com.campusfind.entity.User;
import com.campusfind.exception.ApiException;
import com.campusfind.repository.FoundItemRepository;
import com.campusfind.repository.LostReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchService {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "with", "for", "from", "has", "have", "had", "was", "were", "are",
            "this", "that", "near", "item", "its", "one", "not", "some", "very", "left",
            "lost", "found", "our", "his", "her");

    private final LostReportRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final AuthService auth;

    public MatchService(LostReportRepository lostRepo, FoundItemRepository foundRepo, AuthService auth) {
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.auth = auth;
    }

    /**
     * Possible matches = OPEN lost reports x AVAILABLE found items with the SAME category
     * that share at least one keyword (description) or location word. Best score first.
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> possibleMatches(Long userId, Long lostId) {
        User user = auth.requireUser(userId);

        List<LostReport> lostReports;
        if (lostId != null) {
            LostReport lost = lostRepo.findById(lostId)
                    .orElseThrow(() -> ApiException.notFound("Lost report " + lostId + " not found"));
            if (user.getRole() == Role.STUDENT && !lost.getReportedBy().getId().equals(user.getId())) {
                throw ApiException.forbidden("You can only view matches for your own lost reports");
            }
            if (lost.getStatus() != LostStatus.OPEN) {
                throw ApiException.badRequest("Matches are only listed for OPEN lost reports (this one is " + lost.getStatus() + ")");
            }
            lostReports = List.of(lost);
        } else {
            lostReports = lostRepo.findByStatus(LostStatus.OPEN).stream()
                    .filter(l -> user.getRole() != Role.STUDENT || l.getReportedBy().getId().equals(user.getId()))
                    .toList();
        }

        Set<Long> taken = takenFoundIds();
        List<FoundItem> available = foundRepo.findByStatus(FoundStatus.AVAILABLE).stream()
                .filter(f -> !taken.contains(f.getId()))
                .toList();

        List<MatchResponse> result = new ArrayList<>();
        for (LostReport lost : lostReports) {
            Set<String> lostWords = tokens(lost.getDescription());
            Set<String> lostPlace = tokens(lost.getLocation());
            for (FoundItem found : available) {
                if (!found.getCategory().getId().equals(lost.getCategory().getId())) {
                    continue;
                }
                Set<String> keywords = shared(lostWords, tokens(found.getDescription()));
                Set<String> places = shared(lostPlace, tokens(found.getLocation()));
                int score = keywords.size() * 2 + places.size();
                if (score == 0) {
                    continue;
                }
                result.add(new MatchResponse(
                        ResponseMapper.toLost(lost),
                        ResponseMapper.toFound(found),
                        new ArrayList<>(keywords),
                        new ArrayList<>(places),
                        score));
            }
        }
        result.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return result;
    }

    /** Staff/admin confirms that a lost report and a found item belong together. */
    @Transactional
    public LostReportResponse confirm(Long userId, ConfirmMatchRequest req) {
        User user = auth.requireUser(userId);
        auth.requireRole(user, "Only staff or admin can confirm a match", Role.STAFF, Role.ADMIN);

        LostReport lost = lostRepo.findById(req.lostReportId())
                .orElseThrow(() -> ApiException.notFound("Lost report " + req.lostReportId() + " not found"));
        FoundItem found = foundRepo.findById(req.foundItemId())
                .orElseThrow(() -> ApiException.notFound("Found item " + req.foundItemId() + " not found"));

        if (lost.getStatus() != LostStatus.OPEN) {
            throw ApiException.badRequest("Lost report is already " + lost.getStatus());
        }
        if (found.getStatus() != FoundStatus.AVAILABLE) {
            throw ApiException.badRequest("Found item is not AVAILABLE (current status: " + found.getStatus() + ")");
        }
        if (!lost.getCategory().getId().equals(found.getCategory().getId())) {
            throw ApiException.badRequest("Lost report and found item must be in the same category");
        }
        if (takenFoundIds().contains(found.getId())) {
            throw ApiException.badRequest("This found item is already matched to another lost report");
        }

        lost.setStatus(LostStatus.MATCHED);
        lost.setMatchedFoundItem(found);
        return ResponseMapper.toLost(lostRepo.save(lost));
    }

    private Set<Long> takenFoundIds() {
        Set<Long> ids = new HashSet<>();
        for (LostReport l : lostRepo.findByStatus(LostStatus.MATCHED)) {
            if (l.getMatchedFoundItem() != null) {
                ids.add(l.getMatchedFoundItem().getId());
            }
        }
        return ids;
    }

    private Set<String> tokens(String text) {
        Set<String> out = new LinkedHashSet<>();
        if (text == null) {
            return out;
        }
        for (String word : text.toLowerCase().split("[^a-z0-9]+")) {
            if (word.length() >= 3 && !STOP_WORDS.contains(word)) {
                out.add(word);
            }
        }
        return out;
    }

    private Set<String> shared(Set<String> a, Set<String> b) {
        Set<String> common = new LinkedHashSet<>(a);
        common.retainAll(b);
        return common;
    }
}
