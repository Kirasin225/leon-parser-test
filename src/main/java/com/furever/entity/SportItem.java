package com.furever.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@EqualsAndHashCode
public class SportItem {
    Long id;
    String name;
    List<Region> regionList;
}
