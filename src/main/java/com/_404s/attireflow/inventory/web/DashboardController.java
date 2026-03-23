package com._404s.attireflow.inventory.web;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com._404s.attireflow.inventory.service.DeliveryService;
import com._404s.attireflow.inventory.service.InventoryService;

@Controller
public class DashboardController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        var inventoryPage = inventoryService.getInventory(
                null,
                null,
                null,
                null,
                PageRequest.of(0, 1000, Sort.by("title").ascending())
        );

        var inventoryRows = inventoryPage.getContent();
        long totalProducts = inventoryPage.getTotalElements();
        long lowStockCount = inventoryRows.stream()
                .filter(row -> row.getTotalStock() < 10)
                .count();

        var allDeliveries = deliveryService.getAllDeliveries(PageRequest.of(0, 1000));
        long pendingCount = 0;
        for (var delivery : allDeliveries.getContent()) {
            if ("Pending".equals(delivery.getStatus())) {
                pendingCount++;
            }
        }

        LocalDate tenDaysAgo = LocalDate.now().minusDays(10);
        var recentDeliveries = allDeliveries.getContent().stream()
                .filter(delivery -> "Delivered".equals(delivery.getStatus()))
                .filter(delivery -> delivery.getDeliveryDate() != null)
                .filter(delivery -> !delivery.getDeliveryDate().isBefore(tenDaysAgo))
                .limit(5)
                .toList();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("pendingDeliveries", pendingCount);
        model.addAttribute("totalWarehouses", 3);
        model.addAttribute("recentDeliveries", recentDeliveries);

        var lowStockItems = inventoryRows.stream()
                .filter(row -> row.getTotalStock() < 10)
                .limit(5)
                .toList();
        model.addAttribute("lowStockItems", lowStockItems);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserLabel = (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "Not logged in";
        model.addAttribute("currentUserLabel", currentUserLabel);

        return "dashboard";
    }
}