package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.shike.ordering.auth.model.CurrentPrincipal;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.entity.Address;
import com.shike.ordering.mapper.AddressMapper;
import com.shike.ordering.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddressServiceImplTest {
    private final AddressMapper addressMapper = mock(AddressMapper.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final AddressServiceImpl service = new AddressServiceImpl(addressMapper, userMapper);
    private final ThreadLocal<Long> requestedAddressId = new ThreadLocal<>();

    @AfterEach void clearPrincipal() { PrincipalContext.clear(); }

    @Test
    void detailOtherUsersAddress_shouldReturnNotFound() {
        PrincipalContext.set(new CurrentPrincipal(99L, PrincipalType.USER, null, "token"));
        when(addressMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.detail(1L)).isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(50002);
        verify(addressMapper).selectOne(any(Wrapper.class));
    }

    @Test
    void concurrentDefaultChanges_shouldRemainSerializedPerUser() throws Exception {
        Address first = address(1L);
        Address second = address(2L);
        List<Address> addresses = List.of(first, second);
        ReentrantLock databaseRowLock = new ReentrantLock();
        AtomicInteger activeMutations = new AtomicInteger();
        AtomicInteger maximumActiveMutations = new AtomicInteger();
        when(userMapper.selectIdForUpdate(99L)).thenAnswer(invocation -> {
            databaseRowLock.lock();
            maximumActiveMutations.accumulateAndGet(activeMutations.incrementAndGet(), Math::max);
            return 99L;
        });
        when(addressMapper.selectOne(any(Wrapper.class))).thenAnswer(invocation ->
                requestedAddressId.get().equals(1L) ? first : second);
        when(addressMapper.update(any(Address.class), any(Wrapper.class))).thenAnswer(invocation -> {
            addresses.forEach(address -> address.setIsDefault(false));
            return 1;
        });
        when(addressMapper.updateById(any(Address.class))).thenAnswer(invocation -> {
            activeMutations.decrementAndGet();
            databaseRowLock.unlock();
            return 1;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstResult = executor.submit(() -> setDefaultAsUser(1L));
            Future<?> secondResult = executor.submit(() -> setDefaultAsUser(2L));
            firstResult.get(5, TimeUnit.SECONDS);
            secondResult.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertThat(maximumActiveMutations).hasValue(1);
        assertThat(addresses).filteredOn(Address::getIsDefault).hasSize(1);
        verify(userMapper, times(2)).selectIdForUpdate(99L);
    }

    private void setDefaultAsUser(Long addressId) {
        PrincipalContext.set(new CurrentPrincipal(99L, PrincipalType.USER, null, "token"));
        requestedAddressId.set(addressId);
        try { service.setDefault(addressId); } finally {
            requestedAddressId.remove();
            PrincipalContext.clear();
        }
    }

    private Address address(Long id) {
        Address address = new Address();
        address.setId(id);
        address.setUserId(99L);
        address.setIsDefault(false);
        return address;
    }

}
