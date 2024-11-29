package com.plantify.item.domain.dto.response;

import com.plantify.item.domain.entity.UsingItem;

public record UsingItemAdminResponse(
        Long usingItemId,
        Long myItemId,
        Long userId,
        Double posX,
        Double posY
) {

    public static UsingItemAdminResponse from(UsingItem usingItem) {
        return new UsingItemAdminResponse(
                usingItem.getUsingItemId(),
                usingItem.getMyItem().getMyItemId(),
                usingItem.getMyItem().getUserId(),
                usingItem.getPosX(),
                usingItem.getPosY()
        );
    }
}