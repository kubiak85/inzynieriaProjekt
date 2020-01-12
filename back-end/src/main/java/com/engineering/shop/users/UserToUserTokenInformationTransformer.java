package com.engineering.shop.users;

import com.engineering.shop.common.Transformer;
import org.apache.tomcat.util.buf.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserToUserTokenInformationTransformer implements Transformer<User, UserTokenInformation> {
    @Override
    public UserTokenInformation transform(User user) {
        List<String> privileges = user.getPrivileges();
        Collections.sort(privileges);
        return UserTokenInformation.builder()
                .email(user.getEmail())
                .name(user.getFirstName() + " " + user.getLastName())
                .privileges(StringUtils.join(privileges, ','))
                .build();
    }


}
