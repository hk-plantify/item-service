package com.plantify.item;

import com.plantify.item.client.CashServiceClient;
import com.plantify.item.domain.dto.request.ItemPurchaseRequest;
import com.plantify.item.domain.dto.response.AuthUserResponse;
import com.plantify.item.domain.dto.response.CashResponse;
import com.plantify.item.domain.dto.response.MyItemResponse;
import com.plantify.item.domain.entity.Category;
import com.plantify.item.domain.entity.Item;
import com.plantify.item.global.exception.ApplicationException;
import com.plantify.item.global.exception.errorcode.CashErrorCode;
import com.plantify.item.global.response.ApiResponse;
import com.plantify.item.global.util.UserInfoProvider;
import com.plantify.item.repository.ItemRepository;
import com.plantify.item.repository.MyItemRepository;
import com.plantify.item.service.item.ItemService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemUserServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MyItemRepository myItemRepository;

    @MockBean
    UserInfoProvider userInfoProvider;

    @MockBean
    CashServiceClient cashServiceClient;

    @Autowired
    EntityManager em;

    Long itemId;

    @BeforeEach
    void setUp() {
        given(userInfoProvider.getUserInfo())
                .willReturn(new AuthUserResponse(1L, "USER"));

        Item item = Item.builder()
                .name("TREE")
                .price(100L)
                .imageUri("img")
                .category(Category.TREE)
                .userId(1L)
                .build();

        itemRepository.save(item);
        em.flush();
        em.clear();

        itemId = item.getItemId();
    }

    @Test
    void 캐시_차감_성공_시_아이템_지급() {
        // given
        given(cashServiceClient.buyByCash(any()))
                .willReturn(ApiResponse.ok(
                        new CashResponse(
                                1000L,
                                1000L,
                                0L,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        )
                ));

        ItemPurchaseRequest request = new ItemPurchaseRequest(itemId, 2L);

        // when
        MyItemResponse response = itemService.purchaseItem(request);

        // then
        assertThat(response.quantity()).isEqualTo(2L);
        assertThat(myItemRepository.findAll()).hasSize(1);
    }

    @Test
    void 캐시_부족_시_아이템_지급_실패() {
        // given
        given(cashServiceClient.buyByCash(any()))
                .willThrow(new ApplicationException(CashErrorCode.INSUFFICIENT_BALANCE));

        // when & then
        assertThatThrownBy(() ->
                itemService.purchaseItem(new ItemPurchaseRequest(itemId, 1L))
        ).isInstanceOf(ApplicationException.class);

        assertThat(myItemRepository.findAll()).isEmpty();
    }
}
