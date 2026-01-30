package com.furever.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@EqualsAndHashCode
public class Market {
    String name;
    List<Runner> runners;
}
