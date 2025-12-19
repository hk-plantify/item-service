package com.plantify.item;

import com.plantify.item.domain.dto.UsingItemOutput;
import com.plantify.item.domain.dto.response.AuthUserResponse;
import com.plantify.item.domain.entity.Category;
import com.plantify.item.domain.entity.Item;
import com.plantify.item.domain.entity.MyItem;
import com.plantify.item.domain.entity.UsingItem;
import com.plantify.item.repository.ItemRepository;
import com.plantify.item.repository.MyItemRepository;
import com.plantify.item.repository.UsingItemRepository;
import com.plantify.item.service.usingItem.UsingItemUserService;
import com.plantify.item.global.util.UserInfoProvider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UsingItemUserServiceTest {

    @Autowired
    UsingItemUserService usingItemUserService;

    @Autowired
    UsingItemRepository usingItemRepository;

    @Autowired
    MyItemRepository myItemRepository;

    @Autowired
    ItemRepository itemRepository;

    @MockBean
    UserInfoProvider userInfoProvider;

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    @BeforeEach
    void setUp() {
        given(userInfoProvider.getUserInfo())
                .willReturn(new AuthUserResponse(1L, "USER"));
    }

    @Test
    void N_plus_1_해결_테스트_fetch_join_적용_후() {
        // given
        Item item = itemRepository.save(
                Item.builder()
                        .name("tree")
                        .price(100L)
                        .imageUri("tree.png")
                        .category(Category.TREE)
                        .userId(999L)
                        .build()
        );

        for (int i = 0; i < 30; i++) {
            MyItem myItem = myItemRepository.save(
                    MyItem.builder()
                            .userId(1L)
                            .item(item)
                            .quantity(1L)
                            .build()
            );

            usingItemRepository.save(
                    UsingItem.builder()
                            .myItem(myItem)
                            .posX((double) i)
                            .posY((double) i)
                            .build()
            );
        }


        em.flush();
        em.clear();

        var stats = emf.unwrap(org.hibernate.SessionFactory.class)
                .getStatistics();
        stats.clear();

        // when
        List<UsingItemOutput> result =
                usingItemUserService.getAllUsingItemsByUser();

        // then
        assertThat(result).hasSize(30);

        long selectCount = stats.getPrepareStatementCount();
        assertThat(selectCount)
                .as("Fetch Join 적용 시 SELECT는 1번만 발생해야 함")
                .isEqualTo(1);
    }
}

