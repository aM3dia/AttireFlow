package com._404s.attireflow.inventory.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com._404s.attireflow.inventory.model.Delivery;
import com._404s.attireflow.inventory.model.Variant;
import com._404s.attireflow.inventory.repo.VariantRepository;
import com._404s.attireflow.inventory.service.DeliveryService;

@Controller
@RequestMapping("/deliveries")
public class DeliveryController {
    
    @Autowired
    private DeliveryService deliveryService;
    
    @Autowired
    private VariantRepository variantRepository;

    @GetMapping
    public String listDeliveries(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "id,asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        if (status != null && status.trim().isEmpty()) status = null;
        if (type != null && type.trim().isEmpty()) type = null;
        if (location != null && location.trim().isEmpty()) location = null;

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1]) 
                                   ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortBy = Sort.by(direction, sortField);
 
        Pageable pageable = PageRequest.of(page, size, sortBy);
        
        Page<Delivery> deliveriesPage;
        if (status != null || type != null || location != null) {
            deliveriesPage = deliveryService.filterDeliveries(status, type, location, pageable);
        } else {
            deliveriesPage = deliveryService.getAllDeliveries(pageable);
        }
        
        model.addAttribute("deliveriesPage", deliveriesPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        if (!model.containsAttribute("delivery")) {
            model.addAttribute("delivery", new Delivery());
        }
        if (!model.containsAttribute("selectedVariantId")) {
            model.addAttribute("selectedVariantId", null);
        }
        if (!model.containsAttribute("openModal")) {
            model.addAttribute("openModal", "");
        }

        model.addAttribute("variants", variantRepository.findAll());
        
        return "deliveries";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "redirect:/deliveries";
    }

    @PostMapping("/save")
    public String saveDelivery(
            @RequestParam String deliveryType,
            @RequestParam String location,
            @RequestParam String deliveryDate,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Integer quantity,
            RedirectAttributes redirectAttributes) {

        Delivery delivery = new Delivery();
        delivery.setDeliveryType(deliveryType);
        delivery.setLocation(location);
        delivery.setQuantity(quantity);

        redirectAttributes.addFlashAttribute("delivery", delivery);
        redirectAttributes.addFlashAttribute("selectedVariantId", variantId);
        redirectAttributes.addFlashAttribute("openModal", "newDeliveryModal");

        try {
            if (deliveryType == null || deliveryType.trim().isEmpty()) {
                throw new IllegalArgumentException("Delivery type is required.");
            }
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("Location is required.");
            }
            if (deliveryDate == null || deliveryDate.trim().isEmpty()) {
                throw new IllegalArgumentException("Delivery date is required.");
            }
            if (variantId == null) {
                throw new IllegalArgumentException("Item is required.");
            }
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1.");
            }

            Variant variant = variantRepository.findById(variantId).orElse(null);
            if (variant == null) {
                throw new IllegalArgumentException("Selected item was not found.");
            }

            delivery.setDeliveryDate(java.time.LocalDate.parse(deliveryDate));
            delivery.setVariant(variant);

            deliveryService.saveDelivery(delivery);
            redirectAttributes.addFlashAttribute("successMessage", "Delivery created successfully.");
            return "redirect:/deliveries";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("enteredDeliveryDate", deliveryDate);
            return "redirect:/deliveries";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to create delivery. Please try again.");
            redirectAttributes.addFlashAttribute("enteredDeliveryDate", deliveryDate);
            return "redirect:/deliveries";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        deliveryService.updateDeliveryStatus(id, status);
        return "redirect:/deliveries";
    }
}