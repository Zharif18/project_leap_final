package com.campusfind.dto;

/**
 * pendingItems  = lost reports still OPEN (waiting for a match)
 * matchedItems  = lost reports that have been MATCHED with a found item
 * returnedItems = found items that were RETURNED to their owner
 */
public record DashboardResponse(
        long totalUsers,
        long totalLostReports,
        long totalFoundItems,
        long pendingItems,
        long matchedItems,
        long returnedItems,
        long foundAvailable,
        long foundClaimed) {
}
