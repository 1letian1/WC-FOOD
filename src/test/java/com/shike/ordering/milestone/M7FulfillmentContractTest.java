package com.shike.ordering.milestone;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

class M7FulfillmentContractTest {
    @Test void orderMapper_shouldUseOwnedPaginatedQueriesAndVersionedTransitions() throws Exception {
        String xml=Files.readString(Path.of("src/main/resources/mapper/OrderMapper.xml"));
        assertThat(xml).contains("selectUserPage","selectMerchantPage","selectDashboard",
                "user_id=#{userId}","shop_id=#{shopId}","status=#{expectedStatus}","version=#{version}",
                "GROUP_CONCAT","ORDER BY o.create_time DESC");
        assertThat(xml).doesNotContain("${");
    }

    @Test void productMapper_shouldRestoreStockWithoutDependingOnCurrentSaleState() throws Exception {
        String xml=Files.readString(Path.of("src/main/resources/mapper/ProductMapper.xml"));
        String restore=xml.substring(xml.indexOf("<update id=\"restoreStock\">"));
        assertThat(restore).contains("stock=stock+#{quantity}","shop_id=#{shopId}");
        assertThat(restore).doesNotContain("status = 1","deleted = 0");
    }
}
