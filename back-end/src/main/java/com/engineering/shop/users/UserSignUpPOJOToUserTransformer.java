package com.engineering.shop.users;

import com.engineering.shop.common.Transformer;
import org.springframework.stereotype.Component;

@Component
public class UserSignUpPOJOToUserTransformer implements Transformer<UserSignUpPOJO, User> {
    @Override
    public User transform(UserSignUpPOJO pojo) {

        return User.builder()
                .email(pojo.getEmail())
                .firstName(pojo.getFirstName())
                .lastName(pojo.getLastName())
                .role(new Role("ROLE_USER"))
                .password(pojo.getPassword())
                .build();
    }
}
