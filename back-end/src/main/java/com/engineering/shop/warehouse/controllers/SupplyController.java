package com.engineering.shop.warehouse.controllers;

import com.engineering.shop.warehouse.models.StockAmount;
import com.engineering.shop.warehouse.models.Supplier;
import com.engineering.shop.warehouse.models.Supply;
import com.engineering.shop.warehouse.repositories.StockAmountRepository;
import com.engineering.shop.warehouse.repositories.SupplierRepository;
import com.engineering.shop.warehouse.repositories.SupplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path = "/supplies")
public class SupplyController {

    private SupplyRepository supplyRepository;
    private SupplierRepository supplierRepository;
    private StockAmountRepository stockAmountRepository;

    @Autowired
    public SupplyController(SupplyRepository supplyRepository, SupplierRepository supplierRepository, StockAmountRepository stockAmountRepository) {
        this.supplyRepository = supplyRepository;
        this.supplierRepository = supplierRepository;
        this.stockAmountRepository = stockAmountRepository;
    }

    @GetMapping(path = "/all")
    public Iterable<Supply> getAll() {
        return supplyRepository.findAll();
    }

    @GetMapping(path = "/{SupplyId}")
    public Supply getBySupplyId(@PathVariable Integer SupplyId) {
        Supply supply = supplyRepository.findBySupplyId(SupplyId).orElseThrow(() -> new ResourceNotFoundException("Supply not found with provided supply id"));
        return supply;
    }

    @PostMapping(path = "/add")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody
    String addSupply(@RequestBody Supply Supply) {
        supplyRepository.save(Supply);
        return "Saved";
    }

    @PostMapping(path = "/update")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public @ResponseBody
    String updateSupply(@RequestBody Supply Supply) {
        supplyRepository.save(Supply);
        return "Updated";
    }

    // ---

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(path = "/accept_delivery")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody
    String addAllSupplies(@RequestBody Supply supply) {
        Supplier supplier = supply.getSupplier();
        supplierRepository.save(supplier);

        List<StockAmount> stockAmounts = supply.getStockAmounts();
        saveStockAmounts(stockAmounts);

        supplyRepository.save(supply);
        return "Saved";
    }

    private void saveStockAmounts(List<StockAmount> stockAmounts) {
        for (StockAmount stockAmount : stockAmounts) {
            stockAmount.setAvailable(stockAmount.getAmount() > 0.0 ? true : false);
            if (stockAmountRepository.existsStockAmountByProductId(stockAmount.getProductId())) {
                updateExistingStockAmount(stockAmount);
            } else {
                stockAmountRepository.save(stockAmount);
            }
        }
    }

    private void updateExistingStockAmount(StockAmount stockAmount) {
        StockAmount existing = stockAmountRepository.findStockAmountByProductId(stockAmount.getProductId()).orElseThrow();
        existing.setAmount(existing.getAmount() + stockAmount.getAmount());
        stockAmountRepository.save(existing);
    }
}