package com.project.finances.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;


@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "user_code")
public class UserCode extends BasicEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private final User user;

    private boolean isValid;

    public UserCode disableCode(){
        this.isValid = false;
        return this;
    }

}