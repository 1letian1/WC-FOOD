package com.shike.ordering.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shike.ordering.auth.model.PrincipalContext;
import com.shike.ordering.auth.model.PrincipalType;
import com.shike.ordering.common.exception.ErrorCode;
import com.shike.ordering.common.exception.ResourceNotFoundException;
import com.shike.ordering.dto.user.AddressSaveDTO;
import com.shike.ordering.entity.Address;
import com.shike.ordering.mapper.AddressMapper;
import com.shike.ordering.mapper.UserMapper;
import com.shike.ordering.service.user.AddressService;
import com.shike.ordering.vo.user.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    @Override
    public List<AddressVO> list() {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId())
                        .orderByDesc(Address::getIsDefault).orderByDesc(Address::getId))
                .stream().map(this::toView).toList();
    }

    @Override public AddressVO detail(Long id) { return toView(requireOwned(id, userId())); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO create(AddressSaveDTO request) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        if (request.isDefault()) clearDefaults(userId);
        Address address = new Address();
        address.setUserId(userId);
        apply(address, request);
        address.setDeleted(0);
        addressMapper.insert(address);
        return toView(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO update(Long id, AddressSaveDTO request) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        Address address = requireOwned(id, userId);
        if (request.isDefault()) clearDefaults(userId);
        apply(address, request);
        addressMapper.updateById(address);
        return toView(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO setDefault(Long id) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        Address address = requireOwned(id, userId);
        clearDefaults(userId);
        address.setIsDefault(true);
        addressMapper.updateById(address);
        return toView(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = userId();
        userMapper.selectIdForUpdate(userId);
        addressMapper.deleteById(requireOwned(id, userId));
    }

    private Address requireOwned(Long id, Long userId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id).eq(Address::getUserId, userId));
        if (address == null) throw new ResourceNotFoundException(ErrorCode.ADDRESS_NOT_FOUND);
        return address;
    }

    private void clearDefaults(Long userId) {
        Address update = new Address();
        update.setIsDefault(false);
        addressMapper.update(update, new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, true));
    }

    private void apply(Address address, AddressSaveDTO request) {
        address.setContactName(request.contactName().trim());
        address.setGender(request.gender());
        address.setPhone(request.phone().trim());
        address.setArea(request.area().trim());
        address.setDetail(request.detail().trim());
        address.setHouseNumber(trimToNull(request.houseNumber()));
        address.setTag(trimToNull(request.tag()));
        address.setIsDefault(request.isDefault());
    }

    private AddressVO toView(Address address) {
        return new AddressVO(address.getId(), address.getContactName(), address.getGender(), address.getPhone(),
                address.getArea(), address.getDetail(), address.getHouseNumber(), address.getTag(), address.getIsDefault());
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long userId() { return PrincipalContext.require(PrincipalType.USER).principalId(); }
}
