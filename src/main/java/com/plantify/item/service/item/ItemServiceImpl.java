package com.plantify.item.service.item;

import com.plantify.item.client.CashServiceClient;
import com.plantify.item.domain.dto.request.ItemPurchaseRequest;
import com.plantify.item.domain.dto.request.CashRequest;
import com.plantify.item.domain.dto.request.ItemRequest;
import com.plantify.item.domain.dto.response.CashResponse;
import com.plantify.item.domain.dto.response.ItemResponse;
import com.plantify.item.domain.dto.response.MyItemResponse;
import com.plantify.item.domain.entity.Category;
import com.plantify.item.domain.entity.Item;
import com.plantify.item.domain.entity.MyItem;
import com.plantify.item.global.exception.ApplicationException;
import com.plantify.item.global.exception.errorcode.CashErrorCode;
import com.plantify.item.global.exception.errorcode.ItemErrorCode;
import com.plantify.item.global.response.ApiResponse;
import com.plantify.item.repository.ItemRepository;
import com.plantify.item.repository.MyItemRepository;
import com.plantify.item.global.util.UserInfoProvider;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserInfoProvider userInfoProvider;
    private final CashServiceClient cashServiceClient;
    private final MyItemRepository myItemRepository;

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Override
    public List<ItemResponse> getItemsByCategory(Category category) {
        return itemRepository.findByCategory(category)
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public MyItemResponse purchaseItem(ItemPurchaseRequest request) {
        Long userId = userInfoProvider.getUserInfo().userId();
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        if (request.quantity() <= 0) {
            throw new ApplicationException(CashErrorCode.INVALID_REQUEST);
        }

        Long totalPrice = item.getPrice() * request.quantity();

        try {
            CashRequest cashRequest = new CashRequest(totalPrice);
            cashServiceClient.buyByCash(cashRequest).getData();
        } catch (ApplicationException ae) {
            throw new ApplicationException(CashErrorCode.INSUFFICIENT_BALANCE);
        }

        MyItem myItem = myItemRepository.findByUserIdAndItemItemId(userId, request.itemId())
                .map(existingMyItem -> {
                    existingMyItem.updateQuantity(existingMyItem.getQuantity() + request.quantity());
                    return existingMyItem;
                })
                .orElseGet(() -> request.toEntity(userId, item));

        myItem = myItemRepository.save(myItem);

        return MyItemResponse.from(myItem);
    }

    @Override
    public ItemResponse addItem(ItemRequest request) {
        Long adminId = userInfoProvider.getUserInfo().userId();
        Item item = request.toEntity(adminId);
        Item savedItem = itemRepository.save(item);

        return ItemResponse.from(savedItem);
    }

    @Override
    public ItemResponse updateItem(Long itemId, ItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        Item updatedItem = request.updatedItem(item);
        Item savedItem = itemRepository.save(updatedItem);

        return ItemResponse.from(savedItem);
    }

    @Override
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        itemRepository.delete(item);
    }
}
