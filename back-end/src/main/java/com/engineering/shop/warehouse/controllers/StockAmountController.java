package com.engineering.shop.warehouse.controllers;

import com.engineering.shop.products.Product;
import com.engineering.shop.products.ProductsRepo;
import com.engineering.shop.warehouse.exceptions.UnprocessableEntityException;
import com.engineering.shop.warehouse.models.Measure;
import com.engineering.shop.warehouse.models.StockAmount;
import com.engineering.shop.warehouse.repositories.StockAmountRepository;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(path = "/stock_amounts")
public class StockAmountController {

    private StockAmountRepository stockAmountRepository;
    private ProductsRepo productsRepo;

    @Autowired
    public StockAmountController(StockAmountRepository stockAmountRepository,
                                 ProductsRepo productsRepo) {
        this.stockAmountRepository = stockAmountRepository;
        this.productsRepo = productsRepo;
    }

    @GetMapping(path = "/all")
    public Iterable<StockAmount> getAll() {
        Iterable<StockAmount> stockAmounts = stockAmountRepository.findAll();
        if (Iterables.size(stockAmounts) == 0) {
            throw new ResourceNotFoundException("There are not any stock amounts");
        }
        return stockAmounts;
    }

    @GetMapping(path = "/{stockAmountId}")
    public Optional<StockAmount> getByStockAmountId(@PathVariable Integer stockAmountId) {
        Optional<StockAmount> optionalStockAmount = stockAmountRepository.findStockAmountByStockAmountId(stockAmountId);
        optionalStockAmount.orElseThrow(() -> new ResourceNotFoundException("Stock amount not found with provided stock amount id"));
        return optionalStockAmount;
    }

    @GetMapping(path = "/products/amount/{productId}")
    public Double getAmountByProductId(@PathVariable Integer productId) {
        StockAmount stockAmount = stockAmountRepository.findStockAmountByProductId(productId).
                orElseThrow(() -> new ResourceNotFoundException("Stock amount not found with provided product id"));
        Double amount = stockAmount.getAmount();
        if (amount == null) {
            throw new ResourceNotFoundException("Amount of stock amount with provided product id equals null");
        }
        return amount;
    }

    @GetMapping(path = "/products/avail/{productId}")
    public Boolean isAvailableByProductId(@PathVariable Integer productId) {
        StockAmount stockAmount = stockAmountRepository.findStockAmountByProductId(productId).
                orElseThrow(() -> new ResourceNotFoundException("Stock amount not found with provided product id"));
        Boolean available = stockAmount.isAvailable();
        if (available == null) {
            throw new ResourceNotFoundException("Available of stock amount with provided product id equals null");
        }
        return available;
    }

    @GetMapping(path = "/avail/{available}")
    public Iterable<StockAmount> getAllByAvailable(@PathVariable Boolean available) {
        Iterable<StockAmount> stockAmounts = stockAmountRepository.findAllByAvailable(available);
        if (Iterables.size(stockAmounts) == 0) {
            throw new ResourceNotFoundException(
                    available ? "There are not any available stock amounts" : "There are not any unavailable stock amounts");
        }
        return stockAmounts;
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(path = "/add")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody
    String addStockAmount(@RequestBody StockAmount stockAmount) {
        if (stockAmount.getProductId() == null || stockAmount.getMeasure() == null || stockAmount.getAmount() == null) {
            throw new ResourceNotFoundException("Product id or measure or amount is/are not provided");
        }
        if (stockAmountRepository.existsStockAmountByProductId(stockAmount.getProductId())) {
            throw new ResourceNotFoundException("Stock amount for product with product id provided already exists");
        }
        if (stockAmount.getAmount() > 0.0) {
            stockAmount.setAvailable(true);
        } else {
            stockAmount.setAvailable(false);
        }
        stockAmountRepository.save(stockAmount);
        return "Saved";
    }

    // "metodę przyjmującą id produktu i na tej podstawie tworzył stan magazynowy"
    @PostMapping(path = "/add_empty")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody
    String addEmptyStockAmount(@RequestBody StockAmount stockAmount) {
        if (stockAmount.getAmount() != null) {
            throw new ResourceNotFoundException("Amount is not null");
        }
        if (!stockAmountRepository.existsStockAmountByProductId(stockAmount.getProductId())) {
            stockAmount.setAvailable(false);
            stockAmountRepository.save(stockAmount);
        } else {
            throw new ResourceNotFoundException("Stock amount with provided product id exists");
        }
        return "Saved";
    }

    @PatchMapping("/increase")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public
    String increaseAmount(@RequestBody StockAmount newStockAmount) {
        if (newStockAmount.getProductId() == null || newStockAmount.getAmount() == null) {
            throw new ResourceNotFoundException("Product id or amount is/are not provided");
        }
        StockAmount stockAmount = stockAmountRepository.findStockAmountByProductId(newStockAmount.getProductId()).
                orElseThrow(() -> new ResourceNotFoundException("StockAmount not found with product id"));

        Double newAmount = newStockAmount.getAmount();
        Double oldAmount = stockAmount.getAmount();
        if (oldAmount == null) {
            oldAmount = 0.0;
        }
        if (newAmount > 0.0) {
            stockAmount.setAmount(oldAmount + newAmount);
            if (!stockAmount.isAvailable()) {
                stockAmount.setAvailable(true);
            }
        } else {
            throw new UnprocessableEntityException("Amount is less than or equal 0");
        }
        stockAmountRepository.save(stockAmount);
        return "Increased";
    }

    @PatchMapping("/decrease")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public
    String decreaseAmount(@RequestBody StockAmount newStockAmount) {
        if (newStockAmount.getProductId() == null || newStockAmount.getAmount() == null) {
            throw new ResourceNotFoundException("Product id or amount is/are not provided");
        }
        StockAmount stockAmount = stockAmountRepository.findStockAmountByProductId(newStockAmount.getProductId()).
                orElseThrow(() -> new ResourceNotFoundException("StockAmount not found with product id"));
        Double newAmount = newStockAmount.getAmount();
        Double oldAmount = stockAmount.getAmount();
        if (oldAmount == null) {
            throw new UnprocessableEntityException("old amount is null");
        }
        if (oldAmount - newAmount >= 0.0) {
            stockAmount.setAmount(oldAmount - newAmount);
            if (stockAmount.isAvailable() && stockAmount.getAmount() == 0.0) {
                stockAmount.setAvailable(false);
            }
        } else {
            throw new UnprocessableEntityException("New amount is greater than old amount");
        }
        stockAmountRepository.save(stockAmount);
        return "Decreased";
    }

    // --- POWYZSZE DO WYWALENIA NAJPRAWDOPODOBNIEJ, NIE WSZYSTKIE

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(path = "/measures")
    public Measure[] getAllMeasures() {
        Measure[] measures = Measure.class.getEnumConstants();
        return measures;
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(path = "/last_updated")
    public List<StockAmount> getAllLastUpdated() {
        Iterable<StockAmount> iterable = stockAmountRepository.findAll();
        List<StockAmount> stocks = getIterableAsList(iterable);

        Map<Integer, List<StockAmount>> groupedStocks =
                stocks.stream().collect(Collectors.groupingBy(StockAmount::getProductId));

        return getLastUpdated(groupedStocks);
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(path = "/last_updated_with_names")
    public List<Map<String, String>> getAllLastUpdatedWithProductsNames() {
        Iterable<StockAmount> iterable = stockAmountRepository.findAll();
        List<StockAmount> stocks = getIterableAsList(iterable);
        Map<Integer, List<StockAmount>> groupedStocks =
                stocks.stream().collect(Collectors.groupingBy(StockAmount::getProductId));
        List<StockAmount> lastUpdated = getLastUpdated(groupedStocks);
        return addProductsNamesTo(lastUpdated);
    }

    private List<StockAmount> getIterableAsList(Iterable<StockAmount> iterable) {
        Iterator<StockAmount> iterator = iterable.iterator();
        List<StockAmount> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    private List<StockAmount> getLastUpdated(Map<Integer, List<StockAmount>> groupedStocks) {
        List<StockAmount> lastUpdated = new ArrayList<>();
        groupedStocks.forEach(
                (key, list) -> lastUpdated.add(
                        list
                                .stream()
                                .max(
                                        Comparator.comparingInt(
                                                StockAmount::getStockAmountId
                                        )
                                ).get()
                )
        );
        return lastUpdated;
    }

    private List<Map<String, String>> addProductsNamesTo(List<StockAmount> lastUpdated) {
        List<Map<String, String>> result = new ArrayList<>();

        for (StockAmount s : lastUpdated) {
            Map<String, String> mapValue = new HashMap<>();

            String productName = productsRepo.findById(
                    s.getProductId()).orElseThrow(
                    () -> new ResourceNotFoundException("Brak produktu dla stanu magazynowego.")
            ).getName();

            mapValue.put("stockId", Integer.toString(s.getStockAmountId()));
            mapValue.put("measure", s.getMeasure().toString());
            mapValue.put("available", Boolean.toString(s.getAvailable()));
            mapValue.put("amount", Double.toString(s.getAmount()));
            mapValue.put("productId", Integer.toString(s.getProductId()));
            mapValue.put("productName", productName);


            String dateTime = s.getDateTime().toString();
            dateTime = dateTime.replace("T", " ");
            dateTime = dateTime.substring(0, 16);
            mapValue.put("dateTime", dateTime);

            result.add(mapValue);
        }

        return result;
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping(path = "/decrease_amount")
    public Map<String, String> decrease(@RequestParam double amount, @RequestParam Integer productId) {
        Map<String, String> result = new HashMap<>();
        List<String> info = new ArrayList<>();

        Iterator<StockAmount> stocks = stockAmountRepository.findAllByProductIdOrderByStockAmountIdDesc(productId).iterator();
        if (!stocks.hasNext()) {
            result.put("status", "failed");
            result.put("info", "Nie ma takiego produktu. Nie mozna zmniejszyc ilosci.");
        } else {
            StockAmount stock = stocks.next();
            if (stock.getMeasure().toString().contentEquals("SZT") &&
                    amount % 1 != 0) {
                info.add("Liczba sztuk powinna byc calkowita.");
            }
            if (stock.getAmount() - amount < 0.0) {
                info.add("Podana ilosc przekracza ilosc produktow w magazynie.");
            }
            if (amount < 0.0) {
                info.add("Ilosc/liczba powinna byc wieksza od zera.");
            }
            if (info.size() == 0) {
                StockAmount stockAmount = new StockAmount();
                stockAmount.setAmount(stock.getAmount() - amount);
                stockAmount.setDateTime(LocalDateTime.now());
                stockAmount.setAvailable(stock.getAmount() - amount != 0.0);
                stockAmount.setMeasure(stock.getMeasure());
                stockAmount.setProductId(stock.getProductId());
                stockAmountRepository.save(stockAmount);
                result.put("status", "saved");
            } else {
                result.put("status", "failed");
                StringBuilder message = new StringBuilder("");
                for (int i = 0; i < info.size(); i++) {
                    message.append(info.get(i));
                    message.append(i != info.size() - 1 ? "," : "");
                }
                result.put("error", message.toString());
            }
        }

        return result;
    }
}