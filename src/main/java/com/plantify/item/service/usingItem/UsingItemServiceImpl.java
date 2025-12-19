package com.plantify.item.service.usingItem;

import com.plantify.item.domain.dto.request.UsingItemAdminRequest;
import com.plantify.item.domain.dto.response.UsingItemAdminResponse;
import com.plantify.item.domain.entity.UsingItem;
import com.plantify.item.global.exception.ApplicationException;
import com.plantify.item.global.exception.errorcode.ItemErrorCode;
import com.plantify.item.repository.UsingItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsingItemServiceImpl implements UsingItemService {

    private final UsingItemRepository usingItemRepository;

    @Override
    public List<UsingItemAdminResponse> getAllUsingItems() {
        return usingItemRepository.findAll()
                .stream()
                .map(UsingItemAdminResponse::from)
                .toList();
    }

    @Override
    public UsingItemAdminResponse updateUsingItemPos(Long usingItemId, UsingItemAdminRequest request) {
        UsingItem usingItem = usingItemRepository.findById(usingItemId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        UsingItem updatedUsingItem = usingItem.toBuilder()
                .posX(request.posX())
                .posY(request.posY())
                .build();

        UsingItem savedUsingItem = usingItemRepository.save(updatedUsingItem);

        return UsingItemAdminResponse.from(savedUsingItem);
    }

    @Override
    public void deleteUsingItem(Long usingItemId) {
        UsingItem usingItem = usingItemRepository.findById(usingItemId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        usingItemRepository.delete(usingItem);
    }

    @Override
    public List<UsingItemAdminResponse> getAllUsingItemsByUserId(Long userId) {
        List<UsingItem> usingItems = usingItemRepository.findByUserIdWithMyItem(userId);
        return usingItems
                .stream()
                .map(UsingItemAdminResponse::from)
                .toList();
    }
}
