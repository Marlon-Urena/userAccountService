package com.chatapp.service.util;

import com.chatapp.dto.UserResponseDTO;
import com.chatapp.model.UserAccount;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityToDTOMapper {
    private static ObjectMapper mapper;

    public EntityToDTOMapper(ObjectMapper mapper) {
        EntityToDTOMapper.mapper = mapper;
    }

    public static UserResponseDTO mapToDTO(UserAccount userEntity) {
        return mapper.convertValue(userEntity, UserResponseDTO.class);
    }
}
