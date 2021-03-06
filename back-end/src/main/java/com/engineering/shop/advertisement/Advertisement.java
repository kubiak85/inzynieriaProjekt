package com.engineering.shop.advertisement;


import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Builder(access = AccessLevel.PUBLIC)
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Advertisement extends RepresentationModel<Advertisement> {


    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Integer id;
    private String name;
    private String description;
    private Date date;
    private Integer mainImage;
}
