package com.programmetechie.inventoryservice.service;

import com.programmetechie.inventoryservice.dto.InventoryResponse;
import com.programmetechie.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode){
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory -> {
                            InventoryResponse inventoryResponse = new InventoryResponse();
                            inventoryResponse.setInStock(inventory.getQuantity() > 0);
                            inventoryResponse.setSkuCode(inventory.getSkuCode());
                            return inventoryResponse;
                        }).toList();
    }

}
