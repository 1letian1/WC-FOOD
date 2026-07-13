package com.shike.ordering.service.user;

import com.shike.ordering.dto.user.AddressSaveDTO;
import com.shike.ordering.vo.user.AddressVO;
import java.util.List;

public interface AddressService {
    List<AddressVO> list();
    AddressVO detail(Long id);
    AddressVO create(AddressSaveDTO request);
    AddressVO update(Long id, AddressSaveDTO request);
    AddressVO setDefault(Long id);
    void delete(Long id);
}
