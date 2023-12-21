package com.programmetechie.orderservice.service;

import com.programmetechie.orderservice.dto.InventoryResponse;
import com.programmetechie.orderservice.dto.OrderLineItemsDto;
import com.programmetechie.orderservice.dto.OrderRequest;
import com.programmetechie.orderservice.model.Order;
import com.programmetechie.orderservice.model.OrderLineItems;
import com.programmetechie.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hibernate.internal.util.collections.ArrayHelper.forEach;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;


    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(orderLineItemsDto -> mapToDto(orderLineItemsDto))
                .toList());

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(orderLineItem -> orderLineItem.getSkuCode()).toList();
//        List<OrderLineItems> orderLineItems = order.getOrderLineItemsList();
//        for( OrderLineItems orderLineItem1 : orderLineItems ){
//            List<String> skuCodes = new ArrayList<>();
//            skuCodes.add(orderLineItem1.getSkuCode());
//        }


        //call inventory service and place order if product is in stock.

        InventoryResponse[] inventoryResponseArray = webClient.get()
                                    .uri("http://localhost:8082/api/inventory",
                                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                                    .retrieve()
                                    .bodyToMono(InventoryResponse[].class)
                                    .block();

        Boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(inventoryResponseArrayItem -> inventoryResponseArrayItem.isInStock());

        if(allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {

//        OrderLineItems orderLineItems1 = new OrderLineItems();
//        orderLineItems.setPrice(orderLineItemsDto.getPrice());
//        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
//        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        OrderLineItems orderLineItems = OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();

        return orderLineItems;

    }


}
