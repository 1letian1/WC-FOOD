package com.shike.ordering.milestone;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.shike.ordering.common.entity.BaseEntity;
import com.shike.ordering.common.enums.OrderOperatorType;
import com.shike.ordering.common.enums.OrderStatus;
import com.shike.ordering.common.enums.OrderType;
import com.shike.ordering.common.enums.ProductStatus;
import com.shike.ordering.config.MybatisPlusConfig;
import com.shike.ordering.entity.Address;
import com.shike.ordering.entity.Category;
import com.shike.ordering.entity.MerchantAccount;
import com.shike.ordering.entity.Order;
import com.shike.ordering.entity.OrderItem;
import com.shike.ordering.entity.OrderStatusLog;
import com.shike.ordering.entity.Product;
import com.shike.ordering.entity.ProductSpecification;
import com.shike.ordering.entity.ProductTaste;
import com.shike.ordering.entity.Shop;
import com.shike.ordering.entity.ShoppingCart;
import com.shike.ordering.entity.User;
import com.shike.ordering.mapper.AddressMapper;
import com.shike.ordering.mapper.CategoryMapper;
import com.shike.ordering.mapper.MerchantAccountMapper;
import com.shike.ordering.mapper.OrderItemMapper;
import com.shike.ordering.mapper.OrderMapper;
import com.shike.ordering.mapper.OrderStatusLogMapper;
import com.shike.ordering.mapper.ProductMapper;
import com.shike.ordering.mapper.ProductSpecificationMapper;
import com.shike.ordering.mapper.ProductTasteMapper;
import com.shike.ordering.mapper.ShopMapper;
import com.shike.ordering.mapper.ShoppingCartMapper;
import com.shike.ordering.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class M2DatabaseFoundationTest {
    private static final Path SCHEMA_PATH = Path.of("src/main/resources/db/schema.sql");
    private static final Path DATA_PATH = Path.of("src/main/resources/db/data.sql");

    @Test
    void stableEnums_shouldKeepDocumentedDatabaseCodes() {
        assertThat(ProductStatus.values()).extracting(ProductStatus::getCode, ProductStatus::getDescription)
                .containsExactly(tuple(0, "已下架"), tuple(1, "销售中"), tuple(2, "已售罄"));
        assertThat(OrderType.values()).extracting(OrderType::getCode, OrderType::getDescription)
                .containsExactly(tuple(1, "堂食"), tuple(2, "配送"));
        assertThat(OrderStatus.values()).extracting(OrderStatus::getCode, OrderStatus::getDescription)
                .containsExactly(
                        tuple(1, "待接单"), tuple(2, "已接单"), tuple(3, "制作中"),
                        tuple(4, "待取餐"), tuple(5, "配送中"), tuple(6, "已送达"),
                        tuple(7, "已完成"), tuple(8, "已取消"), tuple(9, "已拒单"));
        assertThat(OrderOperatorType.values()).extracting(OrderOperatorType::getCode)
                .containsExactly("USER", "MERCHANT", "SYSTEM");
        assertThatThrownBy(() -> OrderStatus.fromCode(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void entities_shouldMapEveryCoreTableAndSharedFields() throws NoSuchFieldException {
        assertTable(Shop.class, "shop");
        assertTable(Category.class, "category");
        assertTable(User.class, "user");
        assertTable(MerchantAccount.class, "merchant_account");
        assertTable(Product.class, "product");
        assertTable(ProductSpecification.class, "product_specification");
        assertTable(ProductTaste.class, "product_taste");
        assertTable(ShoppingCart.class, "shopping_cart");
        assertTable(Address.class, "address");
        assertTable(Order.class, "orders");
        assertTable(OrderItem.class, "order_item");
        assertTable(OrderStatusLog.class, "order_status_log");

        TableField createTime = BaseEntity.class.getDeclaredField("createTime").getAnnotation(TableField.class);
        TableField updateTime = BaseEntity.class.getDeclaredField("updateTime").getAnnotation(TableField.class);
        assertThat(createTime.fill()).isEqualTo(FieldFill.INSERT);
        assertThat(updateTime.fill()).isEqualTo(FieldFill.INSERT_UPDATE);
    }

    @Test
    void deletionAndVersionFields_shouldFollowPersistenceRules() throws NoSuchFieldException {
        assertAnnotated(Category.class, "deleted", TableLogic.class);
        assertAnnotated(Product.class, "deleted", TableLogic.class);
        assertAnnotated(ProductSpecification.class, "deleted", TableLogic.class);
        assertAnnotated(ProductTaste.class, "deleted", TableLogic.class);
        assertAnnotated(Address.class, "deleted", TableLogic.class);
        assertThat(Order.class.getDeclaredFields()).noneMatch(field -> field.isAnnotationPresent(TableLogic.class));
        assertThat(OrderItem.class.getDeclaredFields()).noneMatch(field -> field.isAnnotationPresent(TableLogic.class));
        assertThat(OrderStatusLog.class.getDeclaredFields()).noneMatch(field -> field.isAnnotationPresent(TableLogic.class));
        assertAnnotated(Product.class, "version", Version.class);
        assertAnnotated(Order.class, "version", Version.class);
    }

    @Test
    void mappers_shouldProvideMybatisPlusSimpleCrudForEveryCoreTable() {
        List<Class<?>> mapperTypes = List.of(
                ShopMapper.class, CategoryMapper.class, UserMapper.class, MerchantAccountMapper.class,
                ProductMapper.class, ProductSpecificationMapper.class, ProductTasteMapper.class,
                ShoppingCartMapper.class, AddressMapper.class, OrderMapper.class,
                OrderItemMapper.class, OrderStatusLogMapper.class);
        assertThat(mapperTypes).allMatch(BaseMapper.class::isAssignableFrom);
    }

    @Test
    void schema_shouldContainAllCoreTablesAndRequiredConstraints() throws IOException {
        String schema = Files.readString(SCHEMA_PATH, StandardCharsets.UTF_8);
        String normalized = schema.toLowerCase(Locale.ROOT);

        assertThat(normalized).contains(
                "create table if not exists shop", "create table if not exists category",
                "create table if not exists `user`", "create table if not exists merchant_account",
                "create table if not exists product (", "create table if not exists product_specification",
                "create table if not exists product_taste", "create table if not exists shopping_cart",
                "create table if not exists address", "create table if not exists orders",
                "create table if not exists order_item", "create table if not exists order_status_log");
        assertThat(normalized).contains(
                "unique key uk_user_openid", "unique key uk_merchant_account_username",
                "unique key uk_cart_user_product_option", "unique key uk_orders_order_no",
                "unique key uk_orders_user_idempotency", "unique key uk_order_status_log_order_version",
                "key idx_product_shop_category_status_deleted", "key idx_orders_shop_type_status_create");
        assertThat(normalized).contains("decimal(10,2)", "datetime(3)", "default charset=utf8mb4");
        assertThat(normalized).doesNotContain("foreign key");
    }

    @Test
    void developmentData_shouldUseAValidNonProductionBcryptHash() throws IOException {
        String data = Files.readString(DATA_PATH, StandardCharsets.UTF_8);
        Matcher matcher = Pattern.compile("\\$2[aby]\\$12\\$[./A-Za-z0-9]{53}").matcher(data);

        assertThat(matcher.find()).isTrue();
        assertThat(new BCryptPasswordEncoder(12).matches("ShikeTest123", matcher.group())).isTrue();
        assertThat(data).contains("仅供本地开发", "password_algorithm", "'BCRYPT'");
    }

    @Test
    void pagination_shouldRejectUnboundedPageSizes() {
        PaginationInnerInterceptor pagination = (PaginationInnerInterceptor) new MybatisPlusConfig()
                .mybatisPlusInterceptor().getInterceptors().get(1);
        assertThat(pagination.getMaxLimit()).isEqualTo(100L);
    }

    private static void assertTable(Class<? extends BaseEntity> entityType, String tableName) {
        assertThat(entityType.getAnnotation(TableName.class)).isNotNull();
        assertThat(entityType.getAnnotation(TableName.class).value()).isEqualTo(tableName);
    }

    private static void assertAnnotated(Class<?> type, String fieldName,
                                        Class<? extends java.lang.annotation.Annotation> annotationType)
            throws NoSuchFieldException {
        assertThat(type.getDeclaredField(fieldName).isAnnotationPresent(annotationType)).isTrue();
    }
}
