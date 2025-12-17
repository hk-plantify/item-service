package com.plantify.item.service.usingItem;

import com.plantify.item.domain.dto.UsingItemOutput;
import com.plantify.item.domain.entity.MyItem;
import com.plantify.item.domain.entity.UsingItem;
import com.plantify.item.global.exception.ApplicationException;
import com.plantify.item.global.exception.errorcode.ItemErrorCode;
import com.plantify.item.domain.dto.UsingItemActionInput;
import com.plantify.item.repository.MyItemRepository;
import com.plantify.item.repository.UsingItemRepository;
import com.plantify.item.global.util.UserInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsingItemUserServiceImpl implements UsingItemUserService {

    private final UsingItemRepository usingItemRepository;
    private final MyItemRepository myItemRepository;
    private final UserInfoProvider userInfoProvider;

    @Override
    public List<UsingItemOutput> getAllUsingItemsByUser() {
        Long userId = userInfoProvider.getUserInfo().userId();
        return usingItemRepository.findByUserIdWithItem(userId)
                .stream()
                .map(UsingItemOutput::from)
                .toList();
    }

    @Override
    public List<UsingItemOutput> manageUsingItems(List<UsingItemActionInput> actions) {
        Long userId = userInfoProvider.getUserInfo().userId();
        return actions.stream()
                .map(action -> processAction(action, userId))
                .filter(Objects::nonNull)
                .toList();
    }

    private UsingItemOutput processAction(UsingItemActionInput action, Long userId) {
        switch (action.action().toUpperCase()) {
            case "CREATE":
                return createUsingItem(action, userId);
            case "UPDATE":
                return updateUsingItem(action, userId);
            case "DELETE":
                deleteUsingItem(action, userId);
                return null;
            default:
                throw new IllegalArgumentException("Invalid action type: " + action.action());
        }
    }

    private UsingItemOutput createUsingItem(UsingItemActionInput action, Long userId) {
        MyItem myItem = myItemRepository.findMyItemByMyItemIdAndUserId(action.myItemId(), userId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        long availableQuantity = myItem.getQuantity() - usingItemRepository.countByMyItem_MyItemId(myItem.getMyItemId());
        if (availableQuantity <= 0) {
            throw new ApplicationException(ItemErrorCode.INVALID_ITEM_DATA);
        }

        UsingItem newUsingItem = action.CreateUsingItem(myItem);
        myItem.updateQuantity(myItem.getQuantity() - 1);
        myItemRepository.save(myItem);
        return UsingItemOutput.from(usingItemRepository.save(newUsingItem));
    }

    private UsingItemOutput updateUsingItem(UsingItemActionInput action, Long userId) {
        UsingItem usingItem = usingItemRepository.findByUsingItemIdAndUserId(action.usingItemId(), userId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        UsingItem updatedUsingItem = action.UpdateUsingItem(usingItem);
        return UsingItemOutput.from(usingItemRepository.save(updatedUsingItem));
    }

    private void deleteUsingItem(UsingItemActionInput action, Long userId) {
        UsingItem usingItem = usingItemRepository.findByUsingItemIdAndUserId(action.usingItemId(), userId)
                .orElseThrow(() -> new ApplicationException(ItemErrorCode.ITEM_NOT_FOUND));

        MyItem myItem = usingItem.getMyItem();
        myItem.updateQuantity(myItem.getQuantity() + 1);
        myItemRepository.save(myItem);

        usingItemRepository.deleteById(action.usingItemId());
    }
}