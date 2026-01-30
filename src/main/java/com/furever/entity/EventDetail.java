package com.furever.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class EventDetail {
    long id;
    String name;
    Long kickoff;
    List<Market> markets;
}
