package com.shike.ordering.milestone;

import com.shike.ordering.dto.user.OrderCreateDTO;
import com.shike.ordering.service.impl.user.OrderCreationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class M6OrderingContractTest {
    @Test
    void orderRequest_shouldNotAcceptTrustedAmountOrIdentityFields() {
        assertThat(Arrays.stream(OrderCreateDTO.class.getRecordComponents()).map(RecordComponent::getName))
                .doesNotContain("userId", "shopId", "price", "totalAmount", "deliveryFee", "packageFee", "payAmount", "status");
    }

    @Test
    void createOrder_shouldRollbackAllDatabaseWritesOnAnyException() throws NoSuchMethodException {
        Transactional transactional = OrderCreationServiceImpl.class
                .getMethod("create", OrderCreateDTO.class, String.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    @Test
    void stockAndCartSql_shouldKeepServerSideSafetyConditions() throws Exception {
        String productMapper = Files.readString(Path.of("src/main/resources/mapper/ProductMapper.xml"),
                StandardCharsets.UTF_8);
        String cartMapper = Files.readString(Path.of("src/main/resources/mapper/ShoppingCartMapper.xml"),
                StandardCharsets.UTF_8);
        String shopMapper = Files.readString(Path.of("src/main/resources/mapper/ShopMapper.xml"),
                StandardCharsets.UTF_8);

        assertThat(productMapper).contains("stock &gt;= #{quantity}", "status = 1", "deleted = 0",
                "version = version + 1");
        assertThat(cartMapper).contains("WHERE c.user_id = #{userId}", "c.id IN", "NOT EXISTS", "FOR UPDATE",
                "p.price", "ps.price_delta");
        assertThat(shopMapper).contains("selectByIdForUpdate", "WHERE s.id = #{shopId}", "FOR UPDATE");
    }
}
