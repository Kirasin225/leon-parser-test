package com.furever.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class League {
    Long id;
    String name;
    boolean top;
    Integer topOrder;
}
