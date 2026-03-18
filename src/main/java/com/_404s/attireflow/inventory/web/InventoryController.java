package com._404s.attireflow.inventory.web;

import com._404s.attireflow.inventory.model.Variant;
import com._404s.attireflow.inventory.repo.VariantRepository;
import com._404s.attireflow.inventory.service.InventoryService;
import com._404s.attireflow.inventory.service.VariantDetails;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class InventoryController {

    private final InventoryService inventoryService;
    private final VariantRepository variantRepository;

    public InventoryController(InventoryService inventoryService, VariantRepository variantRepository) {
        this.inventoryService = inventoryService;
        this.variantRepository = variantRepository;
    }

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) String title,
                            @RequestParam(required = false) String size,
                            @RequestParam(required = false) String color,
                            @RequestParam(required = false) String category,
                            @RequestParam(defaultValue = "title,asc") String sort,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int pageSize,
                            Model model) {

        if (title != null && title.trim().isEmpty()) title = null;
        if (size != null && size.trim().isEmpty()) size = null;
        if (color != null && color.trim().isEmpty()) color = null;
        if (category != null && category.trim().isEmpty()) category = null;

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortBy = Sort.by(direction, sortField);

        PageRequest pageable = PageRequest.of(page, pageSize, sortBy);

        model.addAttribute("inventoryPage", inventoryService.getInventory(title, size, color, category, pageable));
        model.addAttribute("sizes", inventoryService.getSizes());
        model.addAttribute("colors", inventoryService.getColors());
        model.addAttribute("categories", inventoryService.getCategories());
        model.addAttribute("selectedTitle", title);
        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedColor", color);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);

        if (!model.containsAttribute("createVariantForm")) {
            model.addAttribute("createVariantForm", new Variant());
        }
        if (!model.containsAttribute("editVariantForm")) {
            model.addAttribute("editVariantForm", new Variant());
        }
        if (!model.containsAttribute("addStockVariant")) {
            model.addAttribute("addStockVariant", null);
        }
        if (!model.containsAttribute("selectedLocationId")) {
            model.addAttribute("selectedLocationId", null);
        }
        if (!model.containsAttribute("selectedBinLocation")) {
            model.addAttribute("selectedBinLocation", null);
        }
        if (!model.containsAttribute("enteredQuantity")) {
            model.addAttribute("enteredQuantity", null);
        }
        if (!model.containsAttribute("openModal")) {
            model.addAttribute("openModal", "");
        }

        model.addAttribute("locations", inventoryService.getAllLocations());
        model.addAttribute("binLocations", inventoryService.getAllBinLocations());

        return "inventory";
    }

    @GetMapping("/inventory/new")
    public String newVariantForm() {
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/new")
    public String createVariant(@RequestParam String title,
                                @RequestParam String category,
                                @RequestParam String size,
                                @RequestParam String color,
                                @RequestParam BigDecimal unitPrice,
                                RedirectAttributes redirectAttributes) {
        try {
            if (title.trim().isEmpty() || category.trim().isEmpty() || size.trim().isEmpty() || color.trim().isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be greater than 0.");
            }

            Variant variant = new Variant(title.trim(), category.trim(), size.trim(), color.trim(), unitPrice);
            variantRepository.save(variant);

            redirectAttributes.addFlashAttribute("successMessage", "Variant created successfully.");
            return "redirect:/inventory";
        } catch (IllegalArgumentException ex) {
            Variant variant = new Variant(title, category, size, color, unitPrice);
            redirectAttributes.addFlashAttribute("createVariantForm", variant);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("openModal", "newItemModal");
            return "redirect:/inventory";
        } catch (Exception ex) {
            Variant variant = new Variant(title, category, size, color, unitPrice);
            redirectAttributes.addFlashAttribute("createVariantForm", variant);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to create variant. Please try again.");
            redirectAttributes.addFlashAttribute("openModal", "newItemModal");
            return "redirect:/inventory";
        }
    }

    @GetMapping("/inventory/{id}")
    public String variantDetails(@PathVariable Long id, Model model) {
        VariantDetails details = inventoryService.getVariantDetails(id);
        model.addAttribute("details", details);
        return "variant-details";
    }

    @GetMapping("/inventory/{id}/stock/new")
    public String addStockForm() {
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/{id}/stock/new")
    public String addStock(@PathVariable Long id,
                           @RequestParam(required = false) Long locationId,
                           @RequestParam(required = false) Integer quantity,
                           @RequestParam(required = false) String binLocation,
                           RedirectAttributes redirectAttributes) {

        Variant variant = inventoryService.getVariant(id);

        redirectAttributes.addFlashAttribute("addStockVariant", variant);
        redirectAttributes.addFlashAttribute("selectedLocationId", locationId);
        redirectAttributes.addFlashAttribute("selectedBinLocation", binLocation);
        redirectAttributes.addFlashAttribute("enteredQuantity", quantity);

        try {
            if (locationId == null) {
                throw new IllegalArgumentException("Please select a location.");
            }
            if (quantity == null) {
                throw new IllegalArgumentException("Please enter a quantity.");
            }

            inventoryService.addStock(id, locationId, quantity, binLocation);
            redirectAttributes.addFlashAttribute("successMessage", "Stock added successfully.");
            return "redirect:/inventory";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("openModal", "addStockModal");
            return "redirect:/inventory";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to add stock. Please try again.");
            redirectAttributes.addFlashAttribute("openModal", "addStockModal");
            return "redirect:/inventory";
        }
    }

    @GetMapping("/inventory/{id}/edit")
    public String editVariant() {
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/{id}/edit")
    public String updateVariant(@PathVariable Long id,
                                @RequestParam String title,
                                @RequestParam String category,
                                @RequestParam String size,
                                @RequestParam String color,
                                @RequestParam BigDecimal unitPrice,
                                RedirectAttributes redirectAttributes) {

        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + id));

        redirectAttributes.addFlashAttribute("openModal", "editVariantModal");

        try {
            if (title.trim().isEmpty() || category.trim().isEmpty() || size.trim().isEmpty() || color.trim().isEmpty()) {
                throw new IllegalArgumentException("All fields are required.");
            }
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Unit price must be greater than 0.");
            }

            variant.setTitle(title.trim());
            variant.setCategory(category.trim());
            variant.setSize(size.trim());
            variant.setColor(color.trim());
            variant.setUnitPrice(unitPrice);

            variantRepository.save(variant);
            redirectAttributes.addFlashAttribute("successMessage", "Variant updated successfully.");
            return "redirect:/inventory";
        } catch (IllegalArgumentException ex) {
            Variant editVariantForm = new Variant(title, category, size, color, unitPrice);
            editVariantForm.setId(id);
            redirectAttributes.addFlashAttribute("editVariantForm", editVariantForm);
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/inventory";
        } catch (Exception ex) {
            Variant editVariantForm = new Variant(title, category, size, color, unitPrice);
            editVariantForm.setId(id);
            redirectAttributes.addFlashAttribute("editVariantForm", editVariantForm);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update variant. Please try again.");
            return "redirect:/inventory";
        }
    }
@PostMapping("/inventory/{id}/delete")
public String deleteVariant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        Variant variant = variantRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        
        System.err.println("DELETING VARIANT ID: " + id);
        System.err.println("Deliveries count: " + variant.getDeliveries().size());
        
        if (variant.getDeliveries() != null && !variant.getDeliveries().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Cannot delete this item because it has " + variant.getDeliveries().size() + 
                " delivery(s) linked. Please cancel deliveries first.");
            return "redirect:/inventory";
        }
        
        variantRepository.delete(variant);
        redirectAttributes.addFlashAttribute("successMessage", "Variant deleted successfully.");
        
    } catch (Exception ex) {
        redirectAttributes.addFlashAttribute("errorMessage", "Error deleting variant: " + ex.getMessage());
    }
    return "redirect:/inventory";
}}